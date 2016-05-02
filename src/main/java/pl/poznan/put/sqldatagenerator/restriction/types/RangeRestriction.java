package pl.poznan.put.sqldatagenerator.restriction.types;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class RangeRestriction extends OneAttributeRestriction {

    private enum RangeBoundType {
        INCLUSIVE,
        EXCLUSIVE
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

    private static TreeRangeSet createMinRestriction(Column a, Expression b, RangeBoundType boundType) {
        TreeRangeSet rangeSet = TreeRangeSet.create();
        if (b instanceof LongValue || b instanceof SignedExpression) {
            Long value = getLong(b);
            if (boundType == RangeBoundType.INCLUSIVE) {
                rangeSet.add(Range.closedOpen(value, Long.MAX_VALUE));
            } else {
                rangeSet.add(Range.open(value, Long.MAX_VALUE));
            }
        } else {
            throw new NotImplementedException();
        }
        return rangeSet;
    }

    private static TreeRangeSet createMaxRestriction(Column a, Expression b, RangeBoundType boundType) {
        TreeRangeSet rangeSet = TreeRangeSet.create();
        if (b instanceof LongValue || b instanceof SignedExpression) {
            Long value = getLong(b);
            if (boundType == RangeBoundType.INCLUSIVE) {
                rangeSet.add(Range.closedOpen(Long.MIN_VALUE, value));
            } else {
                rangeSet.add(Range.open(Long.MIN_VALUE, value));
            }
        } else {
            throw new NotImplementedException();
        }
        return rangeSet;
    }

    public static RangeRestriction fromGreaterThan(GreaterThan greaterThan) {
        Expression left = greaterThan.getLeftExpression();
        Expression right = greaterThan.getRightExpression();

        TreeRangeSet treeRangeSet;
        Column column;
        if (left instanceof Column) {
            column = (Column) left;
            treeRangeSet = createMinRestriction(column, right, RangeBoundType.EXCLUSIVE);
        } else {
            column = (Column) right;
            treeRangeSet = createMaxRestriction(column, left, RangeBoundType.EXCLUSIVE);
        }

        return new RangeRestriction(greaterThan, column, treeRangeSet);
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
