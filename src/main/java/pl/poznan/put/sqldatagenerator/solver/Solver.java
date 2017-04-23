package pl.poznan.put.sqldatagenerator.solver;

import com.google.common.collect.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.exception.InvalidInternalStateException;
import pl.poznan.put.sqldatagenerator.exception.NotImplementedException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.restriction.RestrictionsByAttribute;
import pl.poznan.put.sqldatagenerator.restriction.types.*;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static pl.poznan.put.sqldatagenerator.generators.RandomGenerator.*;

public class Solver {
    private static final Logger logger = LoggerFactory.getLogger(Solver.class);

    private final RestrictionsByAttribute restrictionsByAttribute;

    public Solver(RestrictionsByAttribute restrictionsByAttribute) {
        this.restrictionsByAttribute = restrictionsByAttribute;
    }

    public void solve() {
        logger.debug("Solving {}", restrictionsByAttribute.values());
        for (Entry<Attribute, Collection<Restriction>> restrictionEntry : restrictionsByAttribute.randomizedGroupedEntries()) {
            Attribute attribute = restrictionEntry.getKey();
            if (!attribute.canBeGenerated()) {
                continue;
            }

            Collection<Restriction> allRestrictions = restrictionEntry.getValue();
            Collection<Restriction> oneAttributeRestrictions = allRestrictions.stream()
                    .filter(r -> r instanceof OneAttributeRestriction).collect(toList());
            Collection<Restriction> twoAttributeRestrictions = allRestrictions.stream()
                    .filter(r -> r instanceof TwoAttributesRestriction).collect(toList());

            Optional<NullRestriction> nullRestrictionOpt = oneAttributeRestrictions.stream()
                    .filter(r -> r instanceof NullRestriction).map(r -> (NullRestriction) r).findFirst();

            if (nullRestrictionOpt.isPresent()) {
                NullRestriction nullRestriction = nullRestrictionOpt.get();
                if (nullRestriction.isNegated()) {
                    oneAttributeRestrictions.removeIf(r -> r instanceof NullRestriction);
                } else {
                    oneAttributeRestrictions = singletonList(nullRestriction);
                }
            }

            if (oneAttributeRestrictions.size() == 1) {
                Restriction restriction = (Restriction) oneAttributeRestrictions.toArray()[0];
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
                throw new InvalidInternalStateException("Attribute can have only one OneAttributeRestriction");
            }

            if (!twoAttributeRestrictions.isEmpty()) {
                restrictionsByAttribute.combineAll();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void generateFromRangeRestriction(Attribute attribute, RangeRestriction rangeRestriction) {
        switch (attribute.getInternalType()) {
            case LONG:
                Long randomLong = randomLong(rangeRestriction.getRangeSet());
                attribute.setValue(randomLong.toString());
                rangeRestriction.setRange(Range.closed(randomLong, randomLong));
                break;
            case DOUBLE:
                Double randomDouble = randomDouble(rangeRestriction.getRangeSet());
                attribute.setValue(randomDouble.toString());
                rangeRestriction.setRange(Range.closed(randomDouble, randomDouble));
                break;
            default:
                throw new NotImplementedException();
        }
    }

    private void generateFromStringRestriction(Attribute attribute, StringRestriction stringRestriction) {
        String randomValue = "";

//        TODO REFACTOR
//        if(!stringRestriction.isNegated()) {
            if (stringRestriction.containsAllowedValues()) {
                List<String> allowedValues = stringRestriction.getAllowedValues()
                        .stream().filter(a -> !a.isNegated()).map(a -> a.getValue()).collect(Collectors.toList());
                //List<String> notAllowedValues = stringRestriction.getAllowedValues()
                  //      .stream().filter(a -> a.isNegated()).map(a -> a.getValue()).collect(Collectors.toList());
                //allowedValues = allowedValues.stream().filter(a -> !notAllowedValues.contains(a)).collect(Collectors.toList());
                randomValue = allowedValues.get(randomIndex(allowedValues));
            } else if(stringRestriction.containsLikeProperties()) {
                randomValue = stringRestriction.getGenerex().random();
            } else {
                randomValue = randomString(stringRestriction.getMinLength(), stringRestriction.getMaxLength());
            }
//        } else {
//            //TODO better handling of negated restrictions ?
//            randomValue = randomString(stringRestriction.getMinLength(), stringRestriction.getMaxLength());
//        }

        attribute.setValue(randomValue);
        stringRestriction.setAllowedValues(singletonList(randomValue));
    }
}
