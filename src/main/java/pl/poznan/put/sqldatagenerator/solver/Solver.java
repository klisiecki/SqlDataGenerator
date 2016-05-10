package pl.poznan.put.sqldatagenerator.solver;

import com.google.common.collect.HashMultimap;
import org.apache.log4j.Logger;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.RandomGenerator;
import pl.poznan.put.sqldatagenerator.restriction.types.PrimaryKeyRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.Map;

public class Solver {
    private static final Logger logger = Logger.getLogger(Solver.class);

    private HashMultimap<Attribute, Restriction> restrictionsByAttribute;

    public Solver(HashMultimap<Attribute, Restriction> restrictionsByAttribute) {
        this.restrictionsByAttribute = restrictionsByAttribute;
    }

    public void solve() {
        logger.info("Solving " + restrictionsByAttribute.values());
        for (Map.Entry<Attribute, Collection<Restriction>> restrictionEntry : restrictionsByAttribute.asMap().entrySet()) {
            Attribute attribute = restrictionEntry.getKey();
            Collection<Restriction> restrictions = restrictionEntry.getValue();
            if (!attribute.isClear()) {
                continue;
            }
            if (restrictions.size() == 1) {
                Restriction restriction = (Restriction) restrictions.toArray()[0];
                if (restriction instanceof RangeRestriction) {
                    RangeRestriction rangeRestriction = (RangeRestriction) restriction;
                    switch (attribute.getType()) {
                        case INTEGER:
                            attribute.setValue(RandomGenerator.getLong(rangeRestriction.getRangeSet()).toString());
                            break;
                        case FLOAT:
                            attribute.setValue(RandomGenerator.getDouble(rangeRestriction.getRangeSet()).toString());
                            break;
                        default:
                            throw new NotImplementedException();
                    }
                } else if (restriction instanceof PrimaryKeyRestriction) {
                    PrimaryKeyRestriction primaryKeyRestriction = (PrimaryKeyRestriction) restriction;
                    attribute.setValue(primaryKeyRestriction.getNextValue().toString());
                }
            } else {
                throw new NotImplementedException();
            }
        }

    }
}
