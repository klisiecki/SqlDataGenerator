package pl.poznan.put.sqldatagenerator.restriction;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.InExpression;

public class RestrictionFactory {
    public static Restriction createRestriction(Expression expression) {
        if (expression instanceof GreaterThan) {
            return new Restriction(expression);
        } else if (expression instanceof Between) {
            return new Restriction(expression);
        } else if (expression instanceof EqualsTo) {
            return new Restriction(expression);
        } else if (expression instanceof InExpression) {
            return new Restriction(expression);
        } else {
            throw new RuntimeException("Instruction " + expression.toString() + " not implemented");
        }
    }
}
