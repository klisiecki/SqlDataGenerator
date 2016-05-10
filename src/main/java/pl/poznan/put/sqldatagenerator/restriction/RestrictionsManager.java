package pl.poznan.put.sqldatagenerator.restriction;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.NExpression;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.Utils;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class RestrictionsManager {
    private static final Logger logger = LoggerFactory.getLogger(RestrictionsManager.class);

    private List<HashMultimap<Attribute, Restriction>> positiveRestrictionsByAttributeList;
    private List<HashMultimap<Attribute, Restriction>> negativeRestrictionsByAttributeList;

    private final Random random;

    public RestrictionsManager() {
        random = new Random();
    }

    public HashMultimap<Attribute, Restriction> getRandom(boolean positive) {
        if (positive) {
            return positiveRestrictionsByAttributeList.get(random.nextInt(positiveRestrictionsByAttributeList.size()));
        } else {
            return negativeRestrictionsByAttributeList.get(random.nextInt(negativeRestrictionsByAttributeList.size()));
        }
    }

    public void initialize(Expression<Restriction> criteria, Restrictions constraints) {
        List<Restrictions> positiveRestrictionsList = new ArrayList<>();
        List<Restrictions> negativeRestrictionsList = new ArrayList<>();

        setSQLCriteria(criteria, positiveRestrictionsList, negativeRestrictionsList);
        setXMLConstraints(constraints, positiveRestrictionsList);
        setXMLConstraints(constraints, negativeRestrictionsList);
        logger.info("Preparing positive restrictions");
        positiveRestrictionsByAttributeList = prepareRestrictions(positiveRestrictionsList);
        logger.info("Preparing negative restrictions");
        negativeRestrictionsByAttributeList = prepareRestrictions(negativeRestrictionsList);
    }

    private void setSQLCriteria(Expression<Restriction> criteria, List<Restrictions> positiveRestrictionsList,
                                List<Restrictions> negativeRestrictionsList) {
        if (!positiveRestrictionsList.isEmpty() || !negativeRestrictionsList.isEmpty()) {
            throw new RuntimeException("Restrictions already initialized!");
        }

        Expression<Restriction> dnfForm = RuleSet.toDNF(criteria);
        Expression<Restriction> cnfForm = RuleSet.toCNF(criteria);
        if (dnfForm instanceof Or) {
            positiveRestrictionsList.addAll(((NExpression<Restriction>) dnfForm).getChildren().stream()
                    .map(Restrictions::fromExpression).collect(toList()));
        } else {
            positiveRestrictionsList.add(Restrictions.fromExpression(dnfForm));
        }
        if (cnfForm instanceof And) {
            negativeRestrictionsList.addAll(((NExpression<Restriction>) cnfForm).getChildren().stream()
                    .map(Restrictions::fromExpression).collect(toList()));
        } else {
            negativeRestrictionsList.add(Restrictions.fromExpression(cnfForm));
        }
        negativeRestrictionsList.forEach(Restrictions::reverserAll);

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
            logger.info("Preparing restrictions set:");
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
                logger.info("Restrictions for {}: {}", attribute, restrictionEntry.getValue());
            }
            result.add(restrictionsByAttribute);
        }
        return result;
    }

    private void mergeRestrictions(Attribute attribute, Collection<Restriction> restrictions,
                                   HashMultimap<Attribute, Restriction> toRemoveRestrictions, HashMultimap<Attribute, Restriction> restrictionsByAttribute) {
        List<RangeRestriction> rangeRestrictions = restrictions.stream()
                .filter(r -> r instanceof RangeRestriction).map(r -> (RangeRestriction) r).collect(toList());
        if (rangeRestrictions.size() > 1) {
            RangeSet rangeSet = TreeRangeSet.create();
            //noinspection unchecked
            rangeSet.add(Range.all());
            rangeRestrictions.forEach(restriction -> {
                Utils.intersectRangeSets(rangeSet, restriction.getRangeSet());
                toRemoveRestrictions.put(attribute, restriction);

            });
            if (rangeSet.isEmpty()) {
                throw new RuntimeException("Range for attribute " + attribute.getName() + " is empty");
            }
            restrictionsByAttribute.put(attribute, new RangeRestriction(attribute, rangeSet));
        }
    }

}
