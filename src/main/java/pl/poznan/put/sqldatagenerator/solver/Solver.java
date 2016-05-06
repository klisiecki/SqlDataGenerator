package pl.poznan.put.sqldatagenerator.solver;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.log4j.Logger;
import pl.poznan.put.sqldatagenerator.Utils;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.RandomGenerator;
import pl.poznan.put.sqldatagenerator.restriction.Restrictions;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class Solver {
    private static final Logger logger = Logger.getLogger(Solver.class);

//    private Restrictions restrictions;

    private HashMultimap<Attribute, Restriction> restrictionsByAttribute;

    public Solver(Restrictions restrictions) {
//        this.restrictions = restrictions;
        this.restrictionsByAttribute = HashMultimap.create();
        for (Restriction restriction : restrictions.getCollection()) {
            for (Attribute attribute : restriction.getAttributes()) {
                restrictionsByAttribute.put(attribute, restriction);
            }
        }
    }

    public void solve() {
        logger.info("Solving " + restrictionsByAttribute.values());
        prepare();
        for (Map.Entry<Attribute, Collection<Restriction>> restrictionEntry : restrictionsByAttribute.asMap().entrySet()) {
            Attribute attribute = restrictionEntry.getKey();
            Collection<Restriction> restrictions = restrictionEntry.getValue();
            if (!attribute.isClear()) {
                continue;
            }
            if (restrictions.size() == 1) {
                switch (attribute.getType()) {
                    case INTEGER:
                        attribute.setValue("" + RandomGenerator.getLong(((RangeRestriction) restrictions.toArray()[0]).getRangeSet()));
                        break;
                    default:
                        throw new NotImplementedException();
                }
            } else {
                throw new NotImplementedException();
            }
        }

    }

    private void prepare() {
        HashMultimap<Attribute, Restriction> toRemoveRestrictions = HashMultimap.create();
        for (Map.Entry<Attribute, Collection<Restriction>> restrictionEntry : restrictionsByAttribute.asMap().entrySet()) {
            Attribute attribute = restrictionEntry.getKey();
            Collection<Restriction> restrictions = restrictionEntry.getValue();
            mergeRestrictions(attribute, restrictions, toRemoveRestrictions);
        }

        for (Map.Entry<Attribute, Restriction> attributeRestrictionEntry : toRemoveRestrictions.entries()) {
            Attribute attribute = attributeRestrictionEntry.getKey();
            Restriction restriction = attributeRestrictionEntry.getValue();
            restrictionsByAttribute.remove(attribute, restriction);
        }

        for (Map.Entry<Attribute, Collection<Restriction>> restrictionEntry : restrictionsByAttribute.asMap().entrySet()) {
            Attribute attribute = restrictionEntry.getKey();
            Collection<Restriction> restrictions = restrictionEntry.getValue();
            logger.info("Restrictions for " + attribute + ": " + restrictions);
        }
    }

    private void mergeRestrictions(Attribute attribute, Collection<Restriction> restrictions,
                                   HashMultimap<Attribute, Restriction> toRemoveRestrictions) {
        List<RangeRestriction> rangeRestrictions = restrictions.stream()
                .filter(r -> r instanceof RangeRestriction).map(r -> (RangeRestriction) r).collect(toList());
        if (rangeRestrictions.size() > 1) {
            RangeSet rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.all());
            rangeRestrictions.forEach(restriction -> {
                Utils.intersectRangeSets(rangeSet, restriction.getRangeSet());
                toRemoveRestrictions.put(attribute, restriction);

            });
            restrictionsByAttribute.put(attribute, new RangeRestriction(attribute, rangeSet));
        }
    }


}
