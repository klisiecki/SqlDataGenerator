package pl.poznan.put.sqldatagenerator.restriction;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DataTypesConverter;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;

import java.text.ParseException;

public class SQLExpressionsUtils {

    public static boolean isTwoAttributesRelationExpression(Expression expression) {
        return expression instanceof BinaryExpression &&
                isColumn(((BinaryExpression) expression).getLeftExpression()) &&
                isColumn(((BinaryExpression) expression).getRightExpression());
    }

    public static boolean isColumnAndValueExpression(Expression expression) {
        if (expression instanceof BinaryExpression) {
            return isColumnAndValueBinaryExpression((BinaryExpression) expression);
        } else if (expression instanceof InExpression) {
            return isColumnAndValueInExpression((InExpression) expression);
        } else if (expression instanceof Between) {
            return isColumnAndValueBetween((Between) expression);
        }
        return false;
    }

    private static boolean isColumnAndValueBetween(Between between) {
        return isColumn(between.getLeftExpression()) && isNumberOrDateValue(between.getBetweenExpressionStart())
                && isNumberOrDateValue(between.getBetweenExpressionEnd());
    }

    private static boolean isColumnAndValueInExpression(InExpression inExpression) {
        if (isColumn(inExpression.getLeftExpression()) && inExpression.getRightItemsList() instanceof ExpressionList) {
            ExpressionList expressionList = (ExpressionList) inExpression.getRightItemsList();
            for (Expression exp : expressionList.getExpressions()) {
                if (!isSimpleValue(exp)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isColumnAndValueBinaryExpression(BinaryExpression binaryExpression) {
        Expression left = binaryExpression.getLeftExpression();
        Expression right = binaryExpression.getRightExpression();
        return isColumn(left) && isSimpleValue(right) || isColumn(right) && isSimpleValue(left);
    }

    public static boolean isNullExpression(Expression expression) {
        return expression instanceof IsNullExpression;
    }

    public static boolean isStringExpression(Expression expression) {
        Column column;
        if (expression instanceof BinaryExpression) {
            column = getColumn((BinaryExpression) expression);
        } else if (expression instanceof InExpression) {
            column = (Column) ((InExpression) expression).getLeftExpression();
        } else {
            return false;
        }
        return AttributesMap.get(column).getInternalType() == InternalType.STRING;
    }

    public static boolean isColumn(Expression left) {
        return left instanceof Column;
    }

    public static boolean isSimpleValue(Expression expression) {
        return isNumberValue(expression) || isStringValue(expression);
    }

    public static boolean isNumberOrDateValue(Expression expression) {
        return isDoubleValue(expression) || isIntegerValue(expression)|| isDateValue(expression);
    }

    public static boolean isNumberValue(Expression expression) {
        return isDoubleValue(expression) || isIntegerValue(expression);
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
        if(!isStringValue(expression)) {
            return false;
        } else {
            try {
                DataTypesConverter.getLongFromDatetime(((StringValue) expression).getValue());
            } catch (ParseException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDoubleValue(Expression expression) {
        if (expression instanceof DoubleValue) {
            return true;
        } else if (expression instanceof SignedExpression) {
            return isDoubleValue(((SignedExpression) expression).getExpression());
        }
        return false;
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

    public static Double getDouble(Expression expression) {
        if (expression instanceof DoubleValue) {
            return ((DoubleValue) expression).getValue();
        } else if (expression instanceof SignedExpression) {
            SignedExpression se = (SignedExpression) expression;
            Expression e = se.getExpression();
            return se.getSign() == '-' ? -getDouble(e) : getDouble(e);
        } else {
            return getLong(expression).doubleValue();
        }
    }

    public static String getString(Expression expression) {
        if (expression instanceof StringValue) {
            return ((StringValue) expression).getValue();
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
