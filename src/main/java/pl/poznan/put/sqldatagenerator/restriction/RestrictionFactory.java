package pl.poznan.put.sqldatagenerator.restriction;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;
import pl.poznan.put.sqldatagenerator.exception.NotImplementedException;
import pl.poznan.put.sqldatagenerator.restriction.types.*;

import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.*;

public class RestrictionFactory {
    public static Restriction createRestriction(Expression expression) {
        if (isColumnAndValueExpression(expression)) {
            if (isStringExpression(expression)) {
                if (expression instanceof EqualsTo) {
                    return StringRestriction.fromEquals((EqualsTo) expression);
                } else if (expression instanceof NotEqualsTo) {
                    return StringRestriction.fromNotEquals((NotEqualsTo) expression);
                } else if (expression instanceof InExpression) {
                    return StringRestriction.fromIn((InExpression) expression);
                } else if (expression instanceof LikeExpression) {
                    return StringRestriction.fromLikeExpression((LikeExpression) expression);
                }
            } else {
                if (expression instanceof GreaterThan) {
                    return RangeRestriction.fromGreaterThan((GreaterThan) expression);
                } else if (expression instanceof GreaterThanEquals) {
                    return RangeRestriction.fromGreaterThanEquals((GreaterThanEquals) expression);
                } else if (expression instanceof MinorThan) {
                    return RangeRestriction.fromMinorThan((MinorThan) expression);
                } else if (expression instanceof MinorThanEquals) {
                    return RangeRestriction.fromMinorThanEquals((MinorThanEquals) expression);
                } else if (expression instanceof Between) {
                    return RangeRestriction.fromBetween((Between) expression);
                } else if (expression instanceof InExpression) {
                    return RangeRestriction.fromIn((InExpression) expression);
                } else if (expression instanceof EqualsTo) {
                    return RangeRestriction.fromEquals((EqualsTo) expression);
                } else if (expression instanceof NotEqualsTo) {
                    return RangeRestriction.fromNotEquals((NotEqualsTo) expression);
                }
            }
        } else if (isNullExpression(expression)) {
            return NullRestriction.fromIsNullExpression((IsNullExpression) expression);
        } else if (isTwoAttributesRelationExpression(expression)) {
            if (expression instanceof GreaterThan) {
                return TwoAttributesRelationRestriction.fromGreaterThan((GreaterThan) expression);
            } else if (expression instanceof GreaterThanEquals) {
                return TwoAttributesRelationRestriction.fromGreaterThanEquals((GreaterThanEquals) expression);
            } else if (expression instanceof MinorThan) {
                return TwoAttributesRelationRestriction.fromMinorThan((MinorThan) expression);
            } else if (expression instanceof MinorThanEquals) {
                return TwoAttributesRelationRestriction.fromMinorThanEquals((MinorThanEquals) expression);
            } else if (expression instanceof EqualsTo) {
                return TwoAttributesRelationRestriction.fromEquals((EqualsTo) expression);
            } else if (expression instanceof NotEqualsTo) {
                return TwoAttributesRelationRestriction.fromNotEquals((NotEqualsTo) expression);
            }
        }
        throw new NotImplementedException("Instruction " + expression.toString() + " not implemented");
    }

}
