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
            if (isColumn(between.getLeftExpression()) && isIntegerValue(between.getBetweenExpressionStart())
                    && isIntegerValue(between.getBetweenExpressionEnd())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isColumn(Expression left) {
        return left instanceof Column;
    }

    public static boolean isSimpleValue(Expression expression) {
        return isIntegerValue(expression) || isStringValue(expression);
    }

    public static boolean isIntegerValue(Expression expression) {
        if (expression instanceof LongValue) {
            return true;
        } else if (expression instanceof SignedExpression) {
            return isIntegerValue(((SignedExpression) expression).getExpression());
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

    public static Long getLong(Expression expression) {
        if (expression instanceof LongValue) {
            return ((LongValue) expression).getValue();
        } else if (expression instanceof SignedExpression) {
            SignedExpression se = (SignedExpression) expression;
            Expression e = se.getExpression();
            return se.getSign() == '-' ? -getLong(e) : getLong(e);
        } else {
            return null;
        }
    }

    /**
     * @param expression must be positively checked by {@link SQLExpressionsUtils#isColumnAndValueExpression(Expression)}
     * @return true if expression have form like [column][sign][value], false otherwise
     */
    public static boolean isInverted(BinaryExpression expression) {
        return expression.getRightExpression() instanceof Column;
    }

    /**
     * Returns {@link Column} from {@link BinaryExpression} containing {@link Expression} and {@link Column}
     *
     * @param expression must be positively checked by {@link SQLExpressionsUtils#isColumnAndValueExpression(Expression)}
     * @return left or right {@link Expression} casted to {@link Column}
     */
    public static Column getColumn(BinaryExpression expression) {
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();
        if (left instanceof Column) {
            return (Column) left;
        } else {
            return (Column) right;
        }
    }

    /**
     * Returns {@link Expression} from {@link BinaryExpression} containing {@link Expression} and {@link Column}
     *
     * @param expression must be positively checked by {@link SQLExpressionsUtils#isColumnAndValueExpression(Expression)}
     * @return left or right {@link Expression}
     */
    public static Expression getValueExpression(BinaryExpression expression) {
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();
        if (left instanceof Column) {
            return right;
        } else {
            return left;
        }
    }
}
