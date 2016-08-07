package pl.poznan.put.sqldatagenerator.restriction;

import com.google.common.collect.BoundType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.RangeSet;
import pl.poznan.put.sqldatagenerator.exception.NotImplementedException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;
import pl.poznan.put.sqldatagenerator.restriction.types.*;
import pl.poznan.put.sqldatagenerator.util.RangeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static pl.poznan.put.sqldatagenerator.util.RangeUtils.intersectRangeSets;

public class RestrictionsByAttribute {
    private final HashMultimap<Attribute, Restriction> restrictionsByAttribute = HashMultimap.create();

    public void put(Attribute attribute, Restriction restriction) {
        restrictionsByAttribute.put(attribute, restriction);
    }

    public boolean remove(Attribute attribute, Restriction restriction) {
        return restrictionsByAttribute.remove(attribute, restriction);
    }

    public Set<Map.Entry<Attribute, Restriction>> entries() {
        return restrictionsByAttribute.entries();
    }

    public Set<Map.Entry<Attribute, Collection<Restriction>>> groupedEntries() {
        return restrictionsByAttribute.asMap().entrySet();
    }

    public Collection<Restriction> values() {
        return restrictionsByAttribute.values();
    }

    public boolean combineRangeRestrictions() {
        List<TwoAttributesRestriction> restrictions = entries().stream().map(Map.Entry::getValue)
                .filter(r -> r instanceof TwoAttributesRestriction)
                .map(r -> (TwoAttributesRestriction) r).collect(toList());

        return false;
    }

    private boolean combineRelation(TwoAttributesRestriction twoAttributesRestriction) {
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

    private boolean combineNumericInequality(Attribute lessAttribute, Attribute greaterAttribute, BoundType boundType) {
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
//            RangeSet<Long> //TODO creating result range sets
        } else if (lessAttribute.getInternalType() == InternalType.DOUBLE) {

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
