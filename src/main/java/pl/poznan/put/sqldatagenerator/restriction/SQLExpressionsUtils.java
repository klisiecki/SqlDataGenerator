package pl.poznan.put.sqldatagenerator.restriction;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;

public class SQLExpressionsUtils {
    public static boolean isColumnAndValueExpression(Expression expression) {
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

    public static boolean isColumn(Expression left) {
        return left instanceof Column;
    }

    public static boolean isSimpleValue(Expression expression) {
        return isNumberValue(expression) || isStringValue(expression);
    }

    public static boolean isNumberValue(Expression expression) {
        if (expression instanceof LongValue) {
            return true;
        } else if (expression instanceof SignedExpression) {
            return isNumberValue(((SignedExpression) expression).getExpression());
        }
        return false;
    }

    public static boolean isStringValue(Expression expression) {
        return expression instanceof StringValue;
    }

    public static boolean isDateValue(Expression expression) {
        return expression instanceof DateValue;
    }

    public static boolean isDoubleValue(Expression expression) {
        return expression instanceof DoubleValue;
    }

}
