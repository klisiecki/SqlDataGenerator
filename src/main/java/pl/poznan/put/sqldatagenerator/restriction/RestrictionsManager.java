package pl.poznan.put.sqldatagenerator.restriction;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.NExpression;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.log4j.Logger;
import pl.poznan.put.sqldatagenerator.Utils;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class RestrictionsManager {
    private static final Logger logger = Logger.getLogger(RestrictionsManager.class);

    private List<HashMultimap<Attribute, Restriction>> restrictionsByAttributeList;
    private final Random random;

    public RestrictionsManager() {
        restrictionsByAttributeList = new ArrayList<>();
        random = new Random();
    }

    public HashMultimap<Attribute, Restriction> getRandom() {
        return restrictionsByAttributeList.get(random.nextInt(restrictionsByAttributeList.size()));
    }

    public void initialize(Expression<Restriction> criteria, Restrictions constraints) {
        List<Restrictions> restrictionsList = new ArrayList<>();
        setSQLCriteria(criteria, restrictionsList);
        setXMLConstraints(constraints, restrictionsList);
        prepareRestrictions(restrictionsList);
    }

    private void setSQLCriteria(Expression<Restriction> criteria, List<Restrictions> restrictionsList) {
        if (!restrictionsList.isEmpty()) {
            throw new RuntimeException("Already initialized!");
        }

        Expression<Restriction> dnfForm = RuleSet.toDNF(criteria);
        if (dnfForm instanceof Or) {
            restrictionsList.addAll(((NExpression<Restriction>) dnfForm).getChildren().stream()
                    .map(Restrictions::fromExpression).collect(toList()));
        } else {
            restrictionsList.add(Restrictions.fromExpression(dnfForm));
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

    private void prepareRestrictions(List<Restrictions> restrictionsList) {
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
                logger.info("Restrictions for " + attribute + ": " + restrictionEntry.getValue());
            }
            restrictionsByAttributeList.add(restrictionsByAttribute);
        }
    }

    private void mergeRestrictions(Attribute attribute, Collection<Restriction> restrictions,
                                   HashMultimap<Attribute, Restriction> toRemoveRestrictions, HashMultimap<Attribute, Restriction> restrictionsByAttribute) {
        List<RangeRestriction> rangeRestrictions = restrictions.stream()
                .filter(r -> r instanceof RangeRestriction).map(r -> (RangeRestriction) r).collect(toList());
        if (rangeRestrictions.size() > 1) {
            RangeSet rangeSet = TreeRangeSet.create();
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
