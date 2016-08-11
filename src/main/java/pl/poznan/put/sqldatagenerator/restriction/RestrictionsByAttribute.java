package pl.poznan.put.sqldatagenerator.restriction;

import com.google.common.collect.BoundType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.exception.NotImplementedException;
import pl.poznan.put.sqldatagenerator.exception.UnsatisfiableRestrictionException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;
import pl.poznan.put.sqldatagenerator.restriction.types.*;
import pl.poznan.put.sqldatagenerator.util.RangeUtils;

import java.util.*;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toSet;
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
            copy.put(e.getKey(), e.getValue().clone());
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

        boolean anyChanged = false;
        do {
            for (TwoAttributesRestriction restriction : restrictions) {
                try {
                    anyChanged = anyChanged || combineTwoAttributes(restriction);
                } catch (UnsatisfiableRestrictionException e) {
                    logger.info("Combining of " + restriction + " not possible");
                    return false;
                }
            }
        } while (anyChanged);

        return true;
    }

    private boolean combineTwoAttributes(TwoAttributesRestriction twoAttributesRestriction) throws UnsatisfiableRestrictionException {
        if (twoAttributesRestriction instanceof TwoAttributesRelationRestriction) {
            TwoAttributesRelationRestriction relationRestriction = (TwoAttributesRelationRestriction) twoAttributesRestriction;

            Attribute firstAttribute = relationRestriction.getFirstAttribute();
            Attribute secondAttribute = relationRestriction.getSecondAttribute();
            InternalType internalType = firstAttribute.getInternalType();
            if (internalType == InternalType.STRING) {
                //TODO implement strings equals
                throw new NotImplementedException();
            } else if (internalType == InternalType.LONG || internalType == InternalType.DOUBLE) {
                if (relationRestriction.getSignType() == SignType.EQUALS) {
                    combineNumericEquality(firstAttribute, secondAttribute);
                } else if (relationRestriction.getSignType() == SignType.MINOR_THAN) {
                    combineNumericInequality(firstAttribute, secondAttribute, relationRestriction.getBoundType());
                } else if (relationRestriction.getSignType() == SignType.GREATER_THAN) {
                    combineNumericInequality(secondAttribute, firstAttribute, relationRestriction.getBoundType());
                }
            } else {
                throw new NotImplementedException();
            }
        } else {
            throw new NotImplementedException();
        }

        return false;
    }

    private boolean combineNumericInequality(Attribute lessAttribute, Attribute greaterAttribute, BoundType boundType)
            throws UnsatisfiableRestrictionException {
        RangeRestriction lessRangeRestriction = getRangeRestriction(lessAttribute);
        RangeRestriction greaterRangeRestriction = getRangeRestriction(greaterAttribute);

        RangeSet lessRangeSet = lessRangeRestriction.getRangeSet();
        RangeSet greaterRangeSet = greaterRangeRestriction.getRangeSet();

        if (lessAttribute.getInternalType() == InternalType.LONG) {
            long lowerBound = RangeUtils.getMinLong(lessRangeRestriction.getRangeSet());
            long upperBound = RangeUtils.getMaxLong(greaterRangeRestriction.getRangeSet());
            if (boundType == BoundType.OPEN) {
                lowerBound += 1;
                upperBound -= 1;
            }
            if (lowerBound > upperBound) {
                throw new UnsatisfiableRestrictionException("Combining " + lessAttribute + " with " + greaterAttribute
                        + " would produce empty range. " + lowerBound + " is greater than " + upperBound);
            }
            RangeSet<Long> newLessRangeSet = lessRangeSet.subRangeSet(Range.atMost(upperBound));
            lessRangeRestriction.setRangeSet(newLessRangeSet);

            RangeSet<Long> newGreaterRangeSet = greaterRangeSet.subRangeSet(Range.atLeast(lowerBound));
            greaterRangeRestriction.setRangeSet(newGreaterRangeSet);
            return !newLessRangeSet.equals(lessRangeSet) || newGreaterRangeSet.equals(greaterRangeSet);
        } else if (lessAttribute.getInternalType() == InternalType.DOUBLE) {
            double lowerBound = RangeUtils.getMinDouble(lessRangeRestriction.getRangeSet());
            double upperBound = RangeUtils.getMaxDouble(greaterRangeRestriction.getRangeSet());
            if (boundType == BoundType.OPEN) {
                lowerBound += RangeUtils.EPS;
                upperBound -= RangeUtils.EPS;
            }
            if (lowerBound > upperBound) {
                throw new UnsatisfiableRestrictionException("Combining " + lessAttribute + " with " + greaterAttribute
                        + " would produce empty range. " + lowerBound + " is greater than " + upperBound);
            }
            RangeSet<Double> newLessRangeSet = lessRangeSet.subRangeSet(Range.atMost(upperBound));
            lessRangeRestriction.setRangeSet(newLessRangeSet);

            RangeSet<Double> newGreaterRangeSet = greaterRangeSet.subRangeSet(Range.atLeast(lowerBound));
            greaterRangeRestriction.setRangeSet(newGreaterRangeSet);
        }
        return false;
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

    private RangeRestriction getRangeRestriction(Attribute attribute) {
        return restrictionsByAttribute.get(attribute).stream()
                .filter(r -> r instanceof RangeRestriction).map(r -> (RangeRestriction) r).findFirst().get();
    }

}
