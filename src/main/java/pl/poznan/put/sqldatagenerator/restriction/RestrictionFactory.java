package pl.poznan.put.sqldatagenerator.restriction;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;

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

    private static boolean isColumnAndValueExpression(Expression expression) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            Expression left = binaryExpression.getLeftExpression();
            Expression right = binaryExpression.getRightExpression();
            if (isColumn(left) && isSimpleValue(right) || isColumn(right) && isSimpleValue(left)) {
                return true;
            }
        } else if (expression instanceof InExpression) {
            InExpression inExpression = (InExpression) expression;
            if (isColumn(inExpression.getLeftExpression()) && inExpression.getRightItemsList() instanceof ExpressionList) {
                ExpressionList expressionList = (ExpressionList) inExpression.getRightItemsList();
                for (Expression exp : expressionList.getExpressions()) {
                    if (!isSimpleValue(exp)) {
                        return false;
                    }
                }
                return true;
            }
        } else if (expression instanceof Between) {
            Between between = (Between) expression;
            if (isColumn(between.getLeftExpression()) && isNumberValue(between.getBetweenExpressionStart())
                    && isNumberValue(between.getBetweenExpressionEnd())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isColumn(Expression left) {
        return left instanceof Column;
    }

    private static boolean isSimpleValue(Expression expression) {
        return isNumberValue(expression) || isStringValue(expression);
    }

    private static boolean isNumberValue(Expression expression) {
        if (expression instanceof LongValue) {
            return true;
        } else if (expression instanceof SignedExpression) {
            return isNumberValue(((SignedExpression) expression).getExpression());
        }
        return false;
    }

    private static boolean isStringValue(Expression expression) {
        return expression instanceof StringValue;
    }

}
