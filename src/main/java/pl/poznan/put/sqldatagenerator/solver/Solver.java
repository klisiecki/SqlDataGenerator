package pl.poznan.put.sqldatagenerator.solver;

import com.google.common.collect.HashMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.exception.NotImplementedException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.restriction.types.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static pl.poznan.put.sqldatagenerator.generator.RandomGenerator.*;

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

            //TODO better handling NullRestrictions
            Optional<Restriction> nullRestrictionOptional = restrictions.stream().filter(r -> r instanceof NullRestriction).findFirst();
            if (nullRestrictionOptional.isPresent()) {
                NullRestriction nullRestriction = (NullRestriction) nullRestrictionOptional.get();
                if (nullRestriction.isNegated()) {
                    restrictions.remove(nullRestriction);
                } else {
                    restrictions = singletonList(nullRestriction);
                }
            }

            if (restrictions.size() == 1) {
                Restriction restriction = (Restriction) restrictions.toArray()[0];
                if (restriction instanceof RangeRestriction) {
                    generateFromRangeRestriction(attribute, (RangeRestriction) restriction);
                } else if (restriction instanceof PrimaryKeyRestriction) {
                    PrimaryKeyRestriction primaryKeyRestriction = (PrimaryKeyRestriction) restriction;
                    attribute.setValue(primaryKeyRestriction.getNextValue().toString());
                } else if (restriction instanceof StringRestriction) {
                    generateFromStringRestriction(attribute, (StringRestriction) restriction);
                } else if (restriction instanceof NullRestriction) {
                    attribute.setValue(null);
                } else {
                    throw new NotImplementedException();
                }
            } else {
                throw new NotImplementedException();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void generateFromRangeRestriction(Attribute attribute, RangeRestriction rangeRestriction) {
        switch (attribute.getInternalType()) {
            case LONG:
                attribute.setValue(randomLong(rangeRestriction.getRangeSet()).toString());
                break;
            case DOUBLE:
                attribute.setValue(randomDouble(rangeRestriction.getRangeSet()).toString());
                break;
            default:
                throw new NotImplementedException();
        }
    }

    private void generateFromStringRestriction(Attribute attribute, StringRestriction stringRestriction) {
        //TODO handle LIKE expression, better handling of negated restrictions
        List<String> allowedValues = stringRestriction.getAllowedValues();
        if (allowedValues != null && allowedValues.size() > 0 && !stringRestriction.isNegated()) {
            attribute.setValue(allowedValues.get(randomIndex(allowedValues)));
        } else {
            attribute.setValue(randomString(stringRestriction.getMinLength(), stringRestriction.getMaxLength()));
        }
    }
}
