package pl.poznan.put.sqldatagenerator.restriction;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.NExpression;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.exception.InvalidInternalStateException;
import pl.poznan.put.sqldatagenerator.exception.UnsatisfiableSQLException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;
import pl.poznan.put.sqldatagenerator.restriction.types.NullRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.restriction.types.StringRestriction;
import pl.poznan.put.sqldatagenerator.util.RangeUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class RestrictionsManager {
    private static final Logger logger = LoggerFactory.getLogger(RestrictionsManager.class);

    private List<RestrictionsByAttribute> positiveRestrictionsByAttributeList;
    private List<RestrictionsByAttribute> negativeRestrictionsByAttributeList;

    public RestrictionsByAttribute get(boolean positive, int index) {
        if (positive) {
            return positiveRestrictionsByAttributeList.get(index);
        } else {
            return negativeRestrictionsByAttributeList.get(index);
        }
    }

    public int getListSize(boolean positive) {
        if (positive) {
            return positiveRestrictionsByAttributeList.size();
        } else {
            return negativeRestrictionsByAttributeList.size();
        }
    }

    public List<List<Set<String>>> getConnectedTablesAliases(boolean positive) {
        List<List<Set<String>>> result = new ArrayList<>();
        for (int index = 0; index < getListSize(positive); index++) {
            List<Set<String>> list = new ArrayList<>();

            for (Restriction restriction : get(positive, index).values()) {
                Set<String> set = new HashSet<>();
                restriction.getAttributes().forEach(a -> set.add(a.getTableAliasName()));
                list.add(set);
            }

            result.add(list);
        }
        return result;
    }

    public void initialize(Expression<Restriction> criteria, Restrictions constraints) {
        List<Restrictions> positiveRestrictionsList = new ArrayList<>();
        List<Restrictions> negativeRestrictionsList = new ArrayList<>();

        if (criteria != null) {
            setSQLCriteria(criteria, positiveRestrictionsList, negativeRestrictionsList);
        }
        positiveRestrictionsList.removeIf(r -> !verifySQLSatisfiability(r));
        negativeRestrictionsList.removeIf(r -> !verifySQLSatisfiability(r));

        setXMLConstraints(constraints, positiveRestrictionsList);
        setXMLConstraints(constraints, negativeRestrictionsList);
        logger.debug("Preparing positive restrictions");
        positiveRestrictionsByAttributeList = prepareRestrictions(positiveRestrictionsList);
        logger.debug("Preparing negative restrictions");
        negativeRestrictionsByAttributeList = prepareRestrictions(negativeRestrictionsList);
    }

    private void setSQLCriteria(Expression<Restriction> criteria, List<Restrictions> positiveRestrictionsList,
                                List<Restrictions> negativeRestrictionsList) {
        if (!positiveRestrictionsList.isEmpty() || !negativeRestrictionsList.isEmpty()) {
            throw new InvalidInternalStateException("Restrictions already initialized!");
        }

        Expression<Restriction> positiveDNF = RuleSet.toDNF(criteria);
        Expression<Restriction> negativeDNF = RuleSet.toDNF(Not.of(criteria));

        addRestrictions(positiveRestrictionsList, positiveDNF);
        addRestrictions(negativeRestrictionsList, negativeDNF);
    }

    private boolean verifySQLSatisfiability(Restrictions restrictions) {
        boolean containsNullRestriction = restrictions.getCollection().stream()
                .filter(r -> r instanceof NullRestriction)
                .anyMatch(r -> ((NullRestriction) r).isNegated());
        boolean containsOtherRestriction = restrictions.getCollection().stream().anyMatch(r -> !(r instanceof NullRestriction));

        if (containsNullRestriction && containsOtherRestriction) {
            logger.info("Unsatisfiable set of restrictions: " + restrictions);
            return false;
        }
        return true;
    }

    private void addRestrictions(List<Restrictions> restrictionsList, Expression<Restriction> expression) {
        if (expression instanceof Or) {
            restrictionsList.addAll(((NExpression<Restriction>) expression).getChildren().stream()
                    .map(Restrictions::fromExpression).collect(toList()));
        } else {
            restrictionsList.add(Restrictions.fromExpression(expression));
        }
    }

    private void setXMLConstraints(Restrictions constraints, List<Restrictions> restrictionsList) {
        if (restrictionsList.isEmpty()) {
            restrictionsList.add(constraints.clone());
        } else {
            for (Restrictions restrictions : restrictionsList) {
                restrictions.add(constraints.clone());
            }
        }
    }

    private List<RestrictionsByAttribute> prepareRestrictions(List<Restrictions> restrictionsList) {
        List<RestrictionsByAttribute> mergedRestrictions = new ArrayList<>();
        for (Restrictions restrictions : restrictionsList) {
            boolean restrictionsOk = true;
            logger.debug("Preparing restrictions set:");
            RestrictionsByAttribute restrictionsByAttribute = new RestrictionsByAttribute();
            for (Restriction restriction : restrictions.getCollection()) {
                for (Attribute attribute : restriction.getAttributes()) {
                    restrictionsByAttribute.put(attribute, restriction);
                }
            }
            RestrictionsByAttribute toRemoveRestrictions = new RestrictionsByAttribute();
            for (Entry<Attribute, Collection<Restriction>> restrictionEntry : restrictionsByAttribute.groupedEntries()) {
                Attribute attribute = restrictionEntry.getKey();
                restrictionsOk = restrictionsOk &&
                        mergeRestrictions(attribute, restrictionEntry.getValue(), toRemoveRestrictions, restrictionsByAttribute);
            }

            for (Entry<Attribute, Restriction> attributeRestrictionEntry : toRemoveRestrictions.entries()) {
                Attribute attribute = attributeRestrictionEntry.getKey();
                Restriction restriction = attributeRestrictionEntry.getValue();
                restrictionsByAttribute.remove(attribute, restriction);
            }

            for (Entry<Attribute, Collection<Restriction>> restrictionEntry : restrictionsByAttribute.groupedEntries()) {
                Attribute attribute = restrictionEntry.getKey();
                logger.debug("Restrictions for {}: {}", attribute, restrictionEntry.getValue());
            }
            if (restrictionsOk) {
                mergedRestrictions.add(restrictionsByAttribute);
            }
        }

        List<RestrictionsByAttribute> combinedRestrictions = new ArrayList<>();
        for (RestrictionsByAttribute restrictionsByAttribute : mergedRestrictions) {
            if (restrictionsByAttribute.combineAll()) {
                combinedRestrictions.add(restrictionsByAttribute);
            }
        }
        if (combinedRestrictions.isEmpty()) {
            throw new UnsatisfiableSQLException();
        }
        return combinedRestrictions;
    }

    //TODO to consider: move merging logic to restrictions classes
    private boolean mergeRestrictions(Attribute attribute, Collection<Restriction> restrictions,
                                      RestrictionsByAttribute toRemoveRestrictions,
                                      RestrictionsByAttribute restrictionsByAttribute) {

        List<RangeRestriction> rangeRestrictions = restrictions.stream()
                .filter(r -> r instanceof RangeRestriction).map(r -> (RangeRestriction) r).collect(toList());
        List<StringRestriction> stringRestrictions = restrictions.stream()
                .filter(r -> r instanceof StringRestriction).map(r -> (StringRestriction) r).collect(toList());
        List<NullRestriction> nullRestrictions = restrictions.stream()
                .filter(r -> r instanceof NullRestriction).map(r -> (NullRestriction) r).collect(toList());

        return mergeRangeRestrictions(attribute, toRemoveRestrictions, restrictionsByAttribute, rangeRestrictions) &&
                mergeStringRestrictions(attribute, toRemoveRestrictions, restrictionsByAttribute, stringRestrictions) &&
                mergeNullRestrictions(attribute, toRemoveRestrictions, restrictionsByAttribute, nullRestrictions);

    }

    private boolean mergeRangeRestrictions(Attribute attribute, RestrictionsByAttribute toRemoveRestrictions,
                                           RestrictionsByAttribute restrictionsByAttribute, List<RangeRestriction> rangeRestrictions) {
        if (rangeRestrictions.size() > 1) {
            RangeSet rangeSet = TreeRangeSet.create();
            //noinspection unchecked
            rangeSet.add(Range.all());
            for (RangeRestriction restriction : rangeRestrictions) {
                rangeSet = RangeUtils.intersectRangeSets(rangeSet, restriction.getRangeSet());
                toRemoveRestrictions.put(attribute, restriction);
            }
            if (rangeSet.isEmpty()) {
                return false;
            }
            if (attribute.getInternalType() == InternalType.LONG) {
                rangeSet = RangeUtils.removeEmptyRanges((RangeSet<Long>) rangeSet);
            }
            restrictionsByAttribute.put(attribute, new RangeRestriction(attribute, rangeSet));
        }
        return true;
    }

    //TODO better merging for StringRestrictions
    private boolean mergeStringRestrictions(Attribute attribute, RestrictionsByAttribute toRemoveRestrictions,
                                            RestrictionsByAttribute restrictionsByAttribute, List<StringRestriction> stringRestrictions) {
        if (stringRestrictions.size() > 1) {
            StringRestriction first = stringRestrictions.get(0);
            int minLength = first.getMinLength();
            int maxLength = first.getMaxLength();
            StringRestriction.LikeExpressionProperties likeExpressionProperties = first.getLikeExpressionProperties();
            List<String> allowedValues = first.getAllowedValues();
            boolean isNegated = first.isNegated();
            for (int i = 1; i < stringRestrictions.size(); i++) {
                StringRestriction restriction = stringRestrictions.get(i);
                minLength = max(minLength, restriction.getMinLength());
                maxLength = min(maxLength, restriction.getMaxLength());
                toRemoveRestrictions.put(attribute, restriction);
                if (allowedValues != null) {
                    allowedValues = allowedValues.stream().filter(containsValuesFrom(restriction)).collect(toList());
                } else {
                    allowedValues = restriction.getAllowedValues();
                }
                if (restriction.getLikeExpressionProperties() != null) {
                    likeExpressionProperties = restriction.getLikeExpressionProperties();
                }
                if (restriction.isNegated()) {
                    isNegated = true;
                }
            }
            toRemoveRestrictions.put(attribute, first);
            StringRestriction mergedRestriction =
                    new StringRestriction(attribute, Range.closed(minLength, maxLength), likeExpressionProperties, allowedValues, isNegated);
            restrictionsByAttribute.put(attribute, mergedRestriction);
        }
        return true;
    }

    private Predicate<String> containsValuesFrom(StringRestriction restriction) {
        return value -> restriction.getAllowedValues() == null || restriction.getAllowedValues().contains(value);
    }

    private boolean mergeNullRestrictions(Attribute attribute, RestrictionsByAttribute toRemoveRestrictions, RestrictionsByAttribute restrictionsByAttribute, List<NullRestriction> nullRestrictions) {
        if (nullRestrictions.stream().map(NullRestriction::isNegated).collect(toSet()).size() == 2) {
            return false;
        }
        if (nullRestrictions.size() > 1) {
            nullRestrictions.forEach(restriction -> toRemoveRestrictions.put(attribute, restriction));
            NullRestriction restriction = new NullRestriction(attribute);
            if (nullRestrictions.get(0).isNegated()) {
                restriction.reverse();
            }
            restrictionsByAttribute.put(attribute, restriction);
        }
        return true;
    }

}
