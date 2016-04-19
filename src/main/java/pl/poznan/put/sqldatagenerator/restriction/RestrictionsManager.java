package pl.poznan.put.sqldatagenerator.restriction;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.NExpression;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        for (Expression<Restriction> exp : dnfForm.getChildren()) {
            restrictionsList.add(Restrictions.fromExpression(exp));
        }
    }

    public void setXMLConstraints(Restrictions constraints) {
        for (Restrictions restrictions : restrictionsList) {
            restrictions.add(constraints);
        }
    }

}
