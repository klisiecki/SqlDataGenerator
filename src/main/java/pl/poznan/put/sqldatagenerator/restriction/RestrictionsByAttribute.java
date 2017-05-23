package pl.poznan.put.sqldatagenerator.restriction;

import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.exception.InvalidInternalStateException;
import pl.poznan.put.sqldatagenerator.exception.NotImplementedException;
import pl.poznan.put.sqldatagenerator.exception.UnsatisfiableRestrictionException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;
import pl.poznan.put.sqldatagenerator.restriction.types.*;
import pl.poznan.put.sqldatagenerator.util.RangeUtils;

import java.util.*;
import java.util.Map.Entry;

import static com.google.common.collect.BoundType.OPEN;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;
import static pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType.*;
import static pl.poznan.put.sqldatagenerator.restriction.types.StringRestriction.mergeAllowedValues;
import static pl.poznan.put.sqldatagenerator.restriction.types.StringRestriction.mergeLikeProperties;
import static pl.poznan.put.sqldatagenerator.restriction.types.StringRestriction.mergeNotAllowedValues;
import static pl.poznan.put.sqldatagenerator.restriction.types.SignType.*;
import static pl.poznan.put.sqldatagenerator.util.RangeUtils.EPS;
import static pl.poznan.put.sqldatagenerator.util.RangeUtils.intersectRangeSets;

public class RestrictionsByAttribute {

    private static final Logger logger = LoggerFactory.getLogger(RestrictionsByAttribute.class);

    private final HashMultimap<Attribute, Restriction> restrictionsByAttribute = HashMultimap.create();

    public void put(Attribute attribute, Restriction restriction) {
        restrictionsByAttribute.put(attribute, restriction);
    }

    public boolean remove(Attribute attribute, Restriction restriction) {
        return restrictionsByAttribute.remove(attribute, restriction);
    }

    public Set<Entry<Attribute, Restriction>> entries() {
        return restrictionsByAttribute.entries();
    }

    public Set<Entry<Attribute, Collection<Restriction>>> groupedEntries() {
        return restrictionsByAttribute.asMap().entrySet();
    }

    public List<Entry<Attribute, Collection<Restriction>>> randomizedGroupedEntries() {
        List<Entry<Attribute, Collection<Restriction>>> result = new ArrayList<>(groupedEntries());
        Collections.shuffle(result);
        return result;
    }

    public RestrictionsByAttribute clone() {
        RestrictionsByAttribute copy = new RestrictionsByAttribute();
        for (Entry<Attribute, Restriction> e : entries()) {
            copy.put(e.getKey(), e.getValue().copy());
        }
        return copy;
    }

    public Collection<Restriction> values() {
        return restrictionsByAttribute.values();
    }

    /**
     * @return false if combining attributes would return unsatisfiable/invalid restrictions
     */
    public boolean combineAll() {
        Set<TwoAttributesRestriction> restrictions = entries().stream().map(Entry::getValue)
                                                              .filter(r -> r instanceof TwoAttributesRestriction)
                                                              .map(r -> (TwoAttributesRestriction) r).collect(toSet());

        boolean anyChanged;
        do {
            anyChanged = false;
            for (TwoAttributesRestriction restriction : restrictions) {
                try {
                    anyChanged = combineTwoAttributes(restriction) || anyChanged;
                } catch (UnsatisfiableRestrictionException e) {
                    logger.info("Combining of " + restriction + " not possible");
                    return false;
                }
            }
        } while (anyChanged);

        return true;
    }

    private boolean combineTwoAttributes(TwoAttributesRestriction twoAttributesRestriction)
            throws UnsatisfiableRestrictionException {
        if (twoAttributesRestriction instanceof TwoAttributesRelationRestriction) {
            TwoAttributesRelationRestriction relationRestriction = (TwoAttributesRelationRestriction) twoAttributesRestriction;

            Attribute firstAttribute = relationRestriction.getFirstAttribute();
            Attribute secondAttribute = relationRestriction.getSecondAttribute();
            InternalType internalType = firstAttribute.getInternalType();
            SignType signType = relationRestriction.getSignType();
            if (internalType == STRING) {
                if (signType == EQUALS) {
                    return combineStringEquality(firstAttribute, secondAttribute);
                } else {
                    return combineStringsNotEquals(firstAttribute, secondAttribute);
                }
            } else if (internalType == LONG || internalType == DOUBLE) {
                if (signType == EQUALS) {
                    return combineNumericEquality(firstAttribute, secondAttribute);
                } else if (signType == NOT_EQUALS) {
                    return combineNumbersNotEquals(firstAttribute, secondAttribute);
                } else if (signType == MINOR_THAN) {
                    return combineNumericInequality(firstAttribute, secondAttribute, relationRestriction.getBoundType());
                } else if (signType == GREATER_THAN) {
                    return combineNumericInequality(secondAttribute, firstAttribute, relationRestriction.getBoundType());
                } else {
                    throw new InvalidInternalStateException("This shouldn't happen");
                }
            } else {
                throw new NotImplementedException();
            }
        } else {
            throw new NotImplementedException();
        }
    }

    private boolean combineNumericInequality(Attribute lessAttribute, Attribute greaterAttribute, BoundType boundType)
            throws UnsatisfiableRestrictionException {
        RangeRestriction lessRangeRestriction = getRangeRestriction(lessAttribute);
        RangeRestriction greaterRangeRestriction = getRangeRestriction(greaterAttribute);

        RangeSet lessRangeSet = lessRangeRestriction.getRangeSet();
        RangeSet greaterRangeSet = greaterRangeRestriction.getRangeSet();
        RangeSet newLessRangeSet, newGreaterRangeSet;

        InternalType internalType = lessAttribute.getInternalType();
        if (internalType == LONG) {
            long lowerBound = RangeUtils.getMinLong(lessRangeRestriction.getRangeSet());
            long upperBound = RangeUtils.getMaxLong(greaterRangeRestriction.getRangeSet());
            if (boundType == OPEN) {
                lowerBound += 1;
                upperBound -= 1;
            }
            newLessRangeSet = lessRangeSet.subRangeSet(Range.atMost(upperBound));
            newGreaterRangeSet = greaterRangeSet.subRangeSet(Range.atLeast(lowerBound));
        } else if (internalType == DOUBLE) {
            double lowerBound = RangeUtils.getMinDouble(lessRangeRestriction.getRangeSet());
            double upperBound = RangeUtils.getMaxDouble(greaterRangeRestriction.getRangeSet());
            if (boundType == OPEN) {
                lowerBound += EPS;
                upperBound -= EPS;
            }
            newLessRangeSet = lessRangeSet.subRangeSet(Range.atMost(upperBound));
            newGreaterRangeSet = greaterRangeSet.subRangeSet(Range.atLeast(lowerBound));
        } else {
            throw new InvalidInternalStateException("Handling " + internalType + " internal type not implemented");
        }
        if (newLessRangeSet.isEmpty() || newGreaterRangeSet.isEmpty()) {
            throw new UnsatisfiableRestrictionException("Combining " + lessAttribute + " with " + greaterAttribute
                    + " would produce empty range");
        }
        lessRangeRestriction.setRangeSet(newLessRangeSet);
        greaterRangeRestriction.setRangeSet(newGreaterRangeSet);
        return !newLessRangeSet.equals(lessRangeSet) || !newGreaterRangeSet.equals(greaterRangeSet);
    }

    private boolean combineNumericEquality(Attribute firstAttribute, Attribute secondAttribute) {
        RangeRestriction firstRangeRestriction = getRangeRestriction(firstAttribute);
        RangeRestriction secondRangeRestriction = getRangeRestriction(secondAttribute);

        RangeSet firstRangeSet = firstRangeRestriction.getRangeSet();
        RangeSet secondRangeSet = secondRangeRestriction.getRangeSet();
        if (firstRangeSet.equals(secondRangeSet)) {
            return false;
        }

        RangeSet result = intersectRangeSets(firstRangeSet, secondRangeSet);
        firstRangeRestriction.setRangeSet(result);
        secondRangeRestriction.setRangeSet(result);
        return true;
    }

    private boolean combineNumbersNotEquals(Attribute firstAttribute, Attribute secondAttribute) {
        final String firstValue = firstAttribute.getValue();
        final String secondValue = secondAttribute.getValue();
        final RangeRestriction firstRestriction = getRangeRestriction(firstAttribute);
        final RangeRestriction secondRestriction = getRangeRestriction(secondAttribute);

        if (firstValue != null) {
            return combineNumericRestrictions(firstRestriction, secondRestriction);
        }
        if (secondValue != null) {
            return combineNumericRestrictions(secondRestriction, firstRestriction);
        }
        return false;
    }

    private boolean combineNumericRestrictions(RangeRestriction firstRestriction, RangeRestriction secondRestriction) {
        final RangeSet originalRangeSet = secondRestriction.getRangeSet();
        final RangeSet rangeSetCopy = TreeRangeSet.create(originalRangeSet);
        rangeSetCopy.remove((Range) firstRestriction.getRangeSet().asRanges().iterator().next());
        secondRestriction.setRangeSet(rangeSetCopy);
        return !rangeSetCopy.equals(originalRangeSet);
    }

    private boolean combineStringEquality(Attribute firstAttribute, Attribute secondAttribute) {
        final String firstValue = firstAttribute.getValue();
        final String secondValue = secondAttribute.getValue();

        if (firstValue == null && secondValue == null) {
            StringRestriction firstRestriction = getStringRestriction(firstAttribute);
            StringRestriction secondRestriction = getStringRestriction(secondAttribute);
            final int minLength = max(firstRestriction.getMinLength(), secondRestriction.getMinLength());
            final int maxLength = min(firstRestriction.getMaxLength(), secondRestriction.getMaxLength());
            final List<String> allowedValues =
                    mergeAllowedValues(firstRestriction.getAllowedValues(), secondRestriction.getAllowedValues());
            final List<String> notAllowedValues =
                    mergeNotAllowedValues(firstRestriction.getNotAllowedValues(), secondRestriction.getNotAllowedValues());
            final List<StringRestriction.LikeProperty> likeProperties = mergeLikeProperties(firstRestriction
                    .getLikeProperties(), secondRestriction.getLikeProperties());

            asList(firstRestriction, secondRestriction).forEach(r -> {
                r.setMinLength(minLength);
                r.setMaxLength(maxLength);
                r.setAllowedValues(allowedValues);
                r.setNotAllowedValues(notAllowedValues);
                r.setLikeProperties(likeProperties);
            });
            return false;
        }
        if (firstValue != null && secondValue != null) {
            return false;
        }
        if (firstValue != null) {
            return getStringRestriction(secondAttribute).setAllowedValues(singletonList(firstValue));
        } else {
            return getStringRestriction(firstAttribute).setAllowedValues(singletonList(secondValue));
        }
    }

    private boolean combineStringsNotEquals(Attribute firstAttribute, Attribute secondAttribute) {
        final String firstValue = firstAttribute.getValue();
        final String secondValue = secondAttribute.getValue();
        if (isNull(firstValue) == isNull(secondValue)) {
            return false;
        }
        if (firstValue != null) {
            return getStringRestriction(secondAttribute).addNotAllowedValue(firstValue);
        } else {
            return getStringRestriction(firstAttribute).addNotAllowedValue(secondValue);
        }
    }

    private RangeRestriction getRangeRestriction(Attribute attribute) {
        Set<Restriction> restrictions = restrictionsByAttribute.get(attribute);
        return restrictions.stream()
                           .filter(r -> r instanceof RangeRestriction).map(r -> (RangeRestriction) r)
                           .findFirst().orElseThrow(InvalidInternalStateException::new);
    }

    private StringRestriction getStringRestriction(Attribute attribute) {
        Set<Restriction> restrictions = restrictionsByAttribute.get(attribute);
        return restrictions.stream()
                           .filter(r -> r instanceof StringRestriction).map(r -> (StringRestriction) r)
                           .findFirst().orElseThrow(InvalidInternalStateException::new);
    }


}
