package pl.poznan.put.sqldatagenerator.restriction;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.NExpression;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.Utils;
import pl.poznan.put.sqldatagenerator.exception.InvalidInternalStateException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.restriction.types.StringRestriction;

import java.util.*;
import java.util.function.Predicate;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

public class RestrictionsManager {
    private static final Logger logger = LoggerFactory.getLogger(RestrictionsManager.class);

    private List<HashMultimap<Attribute, Restriction>> positiveRestrictionsByAttributeList;
    private List<HashMultimap<Attribute, Restriction>> negativeRestrictionsByAttributeList;

    public HashMultimap<Attribute, Restriction> get(boolean positive, int index) {
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
                restriction.getAttributes().stream().forEach(a -> set.add(a.getTableAliasName()));
                list.add(set);
            }

            result.add(list);
        }
        return result;
    }

    public void initialize(Expression<Restriction> criteria, Restrictions constraints) {
        List<Restrictions> positiveRestrictionsList = new ArrayList<>();
        List<Restrictions> negativeRestrictionsList = new ArrayList<>();

        setSQLCriteria(criteria, positiveRestrictionsList, negativeRestrictionsList);
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
            restrictionsList.add(constraints);
        } else {
            for (Restrictions restrictions : restrictionsList) {
                restrictions.add(constraints);
            }
        }
    }

    private List<HashMultimap<Attribute, Restriction>> prepareRestrictions(List<Restrictions> restrictionsList) {
        List<HashMultimap<Attribute, Restriction>> result = new ArrayList<>();
        for (Restrictions restrictions : restrictionsList) {
            logger.debug("Preparing restrictions set:");
            HashMultimap<Attribute, Restriction> restrictionsByAttribute = HashMultimap.create();
            for (Restriction restriction : restrictions.getCollection()) {
                for (Attribute attribute : restriction.getAttributes()) {
                    restrictionsByAttribute.put(attribute, restriction);
                }
            }
            HashMultimap<Attribute, Restriction> toRemoveRestrictions = HashMultimap.create();
            for (Map.Entry<Attribute, Collection<Restriction>> restrictionEntry : restrictionsByAttribute.asMap().entrySet()) {
                Attribute attribute = restrictionEntry.getKey();
                mergeRestrictions(attribute, restrictionEntry.getValue(), toRemoveRestrictions, restrictionsByAttribute);
            }

            for (Map.Entry<Attribute, Restriction> attributeRestrictionEntry : toRemoveRestrictions.entries()) {
                Attribute attribute = attributeRestrictionEntry.getKey();
                Restriction restriction = attributeRestrictionEntry.getValue();
                restrictionsByAttribute.remove(attribute, restriction);
            }

            for (Map.Entry<Attribute, Collection<Restriction>> restrictionEntry : restrictionsByAttribute.asMap().entrySet()) {
                Attribute attribute = restrictionEntry.getKey();
                logger.debug("Restrictions for {}: {}", attribute, restrictionEntry.getValue());
            }
            result.add(restrictionsByAttribute);
        }
        return result;
    }

    //TODO to consider: move merging logic to restrictions classes
    private void mergeRestrictions(Attribute attribute, Collection<Restriction> restrictions,
                                   HashMultimap<Attribute, Restriction> toRemoveRestrictions, HashMultimap<Attribute, Restriction> restrictionsByAttribute) {
        List<RangeRestriction> rangeRestrictions = restrictions.stream()
                .filter(r -> r instanceof RangeRestriction).map(r -> (RangeRestriction) r).collect(toList());
        mergeRangeRestrictions(attribute, toRemoveRestrictions, restrictionsByAttribute, rangeRestrictions);

        List<StringRestriction> stringRestrictions = restrictions.stream()
                .filter(r -> r instanceof StringRestriction).map(r -> (StringRestriction) r).collect(toList());
        mergeStringRestrictions(attribute, toRemoveRestrictions, restrictionsByAttribute, stringRestrictions);
    }

    private void mergeRangeRestrictions(Attribute attribute, HashMultimap<Attribute, Restriction> toRemoveRestrictions,
                                        HashMultimap<Attribute, Restriction> restrictionsByAttribute, List<RangeRestriction> rangeRestrictions) {
        if (rangeRestrictions.size() > 1) {
            RangeSet rangeSet = TreeRangeSet.create();
            //noinspection unchecked
            rangeSet.add(Range.all());
            rangeRestrictions.forEach(restriction -> {
                Utils.intersectRangeSets(rangeSet, restriction.getRangeSet());
                toRemoveRestrictions.put(attribute, restriction);
            });
            if (rangeSet.isEmpty()) {
                //TODO Are we sure to throw exception? Maybe just skip this restriction?
                throw new RuntimeException("Range for attribute " + attribute.getName() + " is empty");
            }
            restrictionsByAttribute.put(attribute, new RangeRestriction(attribute, rangeSet));
        }
    }

    //TODO better merging for StringRestrictions
    private void mergeStringRestrictions(Attribute attribute, HashMultimap<Attribute, Restriction> toRemoveRestrictions,
                                         HashMultimap<Attribute, Restriction> restrictionsByAttribute, List<StringRestriction> stringRestrictions) {
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
                    new StringRestriction(attribute, minLength, maxLength, likeExpressionProperties, allowedValues, isNegated);
            restrictionsByAttribute.put(attribute, mergedRestriction);
        }
    }

    private Predicate<String> containsValuesFrom(StringRestriction restriction) {
        return value -> restriction.getAllowedValues() == null || restriction.getAllowedValues().contains(value);
    }

}
