package pl.poznan.put.sqldatagenerator.restriction.types;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class RangeRestriction extends OneAttributeRestriction {

    private enum SignType {
        GREATER_THAN {
            @Override
            SignType flip() {
                return MINOR_THAN;
            }
        },
        MINOR_THAN {
            @Override
            SignType flip() {
                return GREATER_THAN;
            }
        };

        abstract SignType flip();
    }

    private TreeRangeSet treeRangeSet;

    public RangeRestriction(Expression expression, Column column, TreeRangeSet treeRangeSet) {
        super(expression, column);
        this.treeRangeSet = treeRangeSet;
    }

    public RangeRestriction(Attribute attribute, TreeRangeSet treeRangeSet) {
        super(attribute);
        this.treeRangeSet = treeRangeSet;
    }

    public void setTreeRangeSet(TreeRangeSet treeRangeSet) {
        this.treeRangeSet = treeRangeSet;
    }

    public TreeRangeSet getTreeRangeSet() {
        return treeRangeSet;
    }

    private static Long getLong(Expression expression) {
        if (expression instanceof LongValue) {
            return ((LongValue) expression).getValue();
        } else if (expression instanceof SignedExpression) {
            SignedExpression se = (SignedExpression) expression;
            Expression e = se.getExpression();
            return -getLong(e);
        } else {
            return null;
        }
    }

    public static RangeRestriction fromGreaterThan(GreaterThan greaterThan) {
        return fromBinaryExpression(greaterThan, SignType.GREATER_THAN, BoundType.OPEN);
    }

    public static RangeRestriction fromGreaterThanEquals(GreaterThanEquals greaterThanEquals) {
        return fromBinaryExpression(greaterThanEquals, SignType.GREATER_THAN, BoundType.CLOSED);
    }

    public static RangeRestriction fromMinorThan(MinorThan minorThan) {
        return fromBinaryExpression(minorThan, SignType.MINOR_THAN, BoundType.OPEN);
    }

    public static RangeRestriction fromMinorThanEquals(MinorThanEquals minorThanEquals) {
        return fromBinaryExpression(minorThanEquals, SignType.MINOR_THAN, BoundType.CLOSED);
    }

    private static RangeRestriction fromBinaryExpression(BinaryExpression binaryExpression, SignType signType, BoundType boundType) {
        TreeRangeSet treeRangeSet;
        Expression expression = getExpression(binaryExpression);
        treeRangeSet = createMaxOrMinRestriction(expression, isInverted(binaryExpression) ? signType.flip() : signType, boundType);
        return new RangeRestriction(expression, getColumn(binaryExpression), treeRangeSet);
    }

    private static TreeRangeSet createMaxOrMinRestriction(Expression expression, SignType signType, BoundType boundType) {
        if (expression instanceof LongValue || expression instanceof SignedExpression) {
            TreeRangeSet<Long> rangeSet = TreeRangeSet.create();
            Long value = getLong(expression);
            if (signType == SignType.GREATER_THAN) {
                rangeSet.add(Range.downTo(value, boundType));
            } else if (signType == SignType.MINOR_THAN) {
                rangeSet.add(Range.upTo(value, boundType));
            }
            return rangeSet;
        } else {
            throw new NotImplementedException();
        }
    }

    private static boolean isInverted(BinaryExpression expression) {
        return expression.getRightExpression() instanceof Column;
    }

    /**
     * Returns {@link Column} from {@link BinaryExpression} containing {@link Expression} and {@link Column}
     *
     * @param expression must be positively checked by {@link SQLExpressionsUtils#isColumnAndValueExpression(Expression)}
     * @return left or right Expression casted to {@link Column}
     */
    private static Column getColumn(BinaryExpression expression) {
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
     * @return left or right Expression
     */
    private static Expression getExpression(BinaryExpression expression) {
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();
        if (left instanceof Column) {
            return right;
        } else {
            return left;
        }
    }

    public static RangeRestriction fromBetween(Between between) {
        return new RangeRestriction(between, null, null);
    }

    public static RangeRestriction fromIn(InExpression in) {
        return new RangeRestriction(in, null, null);
    }

    public static RangeRestriction fromEquals(EqualsTo equalsTo) {
        return new RangeRestriction(equalsTo, null, null);
    }

    @Override
    public String toString() {
        return "RangeRestriction[" + attributes.get(0) + ": " + (expression == null ? "" : expression + ", ") + treeRangeSet + "]";
    }
}
