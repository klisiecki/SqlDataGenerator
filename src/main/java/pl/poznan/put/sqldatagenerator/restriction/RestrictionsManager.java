package pl.poznan.put.sqldatagenerator.restriction;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.NExpression;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;

public class RestrictionsManager {
    private final List<Restrictions> restrictionsList;
    private final Random random;

    public RestrictionsManager() {
        restrictionsList = new ArrayList<>();
        random = new Random();
    }

    public Restrictions getRandom() {
        return restrictionsList.get(random.nextInt(restrictionsList.size()));
    }

    public void setSQLCriteria(Expression<Restriction> criteria) {
        if (!restrictionsList.isEmpty()) {
            throw new RuntimeException("Already initialized!");
        }

        NExpression<Restriction> dnfForm = (NExpression<Restriction>) RuleSet.toDNF(criteria);
        if (dnfForm instanceof Or) {
            restrictionsList.addAll(dnfForm.getChildren().stream().map(Restrictions::fromExpression).collect(toList()));
        } else {
            restrictionsList.add(Restrictions.fromExpression(dnfForm));
        }
    }

    public void setXMLConstraints(Restrictions constraints) {
        if (restrictionsList.isEmpty()) {
            restrictionsList.add(constraints);
        } else {
            for (Restrictions restrictions : restrictionsList) {
                restrictions.add(constraints);
            }
        }
    }

}
