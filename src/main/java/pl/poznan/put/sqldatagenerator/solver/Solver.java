package pl.poznan.put.sqldatagenerator.solver;

import com.google.common.collect.HashMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.RandomGenerator;
import pl.poznan.put.sqldatagenerator.restriction.types.PrimaryKeyRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.restriction.types.StringRestriction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static pl.poznan.put.sqldatagenerator.generator.RandomGenerator.randomIndex;
import static pl.poznan.put.sqldatagenerator.generator.RandomGenerator.randomString;

@SuppressWarnings("unchecked")
public class Solver {
    private static final Logger logger = LoggerFactory.getLogger(Solver.class);

    private final HashMultimap<Attribute, Restriction> restrictionsByAttribute;

    public Solver(HashMultimap<Attribute, Restriction> restrictionsByAttribute) {
        this.restrictionsByAttribute = restrictionsByAttribute;
    }

    public void solve() {
        logger.debug("Solving {}", restrictionsByAttribute.values());
        for (Map.Entry<Attribute, Collection<Restriction>> restrictionEntry : restrictionsByAttribute.asMap().entrySet()) {
            Attribute attribute = restrictionEntry.getKey();
            Collection<Restriction> restrictions = restrictionEntry.getValue();
            if (!attribute.canBeGenerated()) {
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
                } else if (restriction instanceof StringRestriction) {
                    //TODO handle LIKE expression, better handling of negated restrictions
                    StringRestriction stringRestriction = (StringRestriction) restriction;
                    List<String> allowedValues = stringRestriction.getAllowedValues();
                    if (allowedValues != null && allowedValues.size() > 0 && !stringRestriction.isNegated()) {
                        attribute.setValue(allowedValues.get(randomIndex(allowedValues)));
                    } else {
                        attribute.setValue(randomString(stringRestriction.getMinLength(), stringRestriction.getMaxLength()));
                    }
                }
            } else {
                throw new NotImplementedException();
            }
        }

    }
}
