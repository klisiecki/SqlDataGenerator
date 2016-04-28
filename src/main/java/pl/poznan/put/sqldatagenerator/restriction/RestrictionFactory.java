package pl.poznan.put.sqldatagenerator.restriction;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;

import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.isColumnAndValueExpression;

public class RestrictionFactory {
    public static Restriction createRestriction(Expression expression) {
        if (isColumnAndValueExpression(expression)) {
            if (expression instanceof GreaterThan) {
                return RangeRestriction.fromGreaterThan((GreaterThan) expression);
            } else if (expression instanceof Between) {
                return RangeRestriction.fromBetween((Between) expression);
            } else if (expression instanceof InExpression) {
                return RangeRestriction.fromIn((InExpression) expression);
            } else if (expression instanceof EqualsTo) {
                return RangeRestriction.fromEquals((EqualsTo) expression);
            }
        }
        throw new RuntimeException("Instruction " + expression.toString() + " not implemented");
    }

}
