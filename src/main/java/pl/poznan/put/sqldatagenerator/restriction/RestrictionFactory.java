package pl.poznan.put.sqldatagenerator.restriction;

import net.sf.jsqlparser.expression.Expression;

public class RestrictionFactory {
    public static Restriction createRestriction(Expression expression) {
        return new Restriction(expression);
    }
}
