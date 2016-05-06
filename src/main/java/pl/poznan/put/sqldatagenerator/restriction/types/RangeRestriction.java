package pl.poznan.put.sqldatagenerator.restriction.types;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.Utils;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.*;

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

    private RangeSet rangeSet;

    public RangeRestriction(Expression expression, Column column, RangeSet rangeSet) {
        super(expression, column);
        this.rangeSet = rangeSet;
    }

    public RangeRestriction(Attribute attribute, RangeSet rangeSet) {
        super(attribute);
        this.rangeSet = rangeSet;
    }

    public void setRangeSet(RangeSet rangeSet) {
        this.rangeSet = rangeSet;
    }

    public RangeSet getRangeSet() {
        return rangeSet;
    }

    public static RangeRestriction fromGreaterThan(GreaterThan greaterThan) {
        SignType signType = SQLExpressionsUtils.isInverted(greaterThan) ? SignType.MINOR_THAN : SignType.GREATER_THAN;
        RangeSet rangeSet = createMaxOrMinRangeSet(getValueExpression(greaterThan), signType, BoundType.OPEN);
        return new RangeRestriction(greaterThan, getColumn(greaterThan), rangeSet);
    }

    public static RangeRestriction fromGreaterThanEquals(GreaterThanEquals greaterThanEquals) {
        SignType signType = SQLExpressionsUtils.isInverted(greaterThanEquals) ? SignType.MINOR_THAN : SignType.GREATER_THAN;
        RangeSet rangeSet = createMaxOrMinRangeSet(getValueExpression(greaterThanEquals), signType, BoundType.CLOSED);
        return new RangeRestriction(greaterThanEquals, getColumn(greaterThanEquals), rangeSet);
    }

    public static RangeRestriction fromMinorThan(MinorThan minorThan) {
        SignType signType = SQLExpressionsUtils.isInverted(minorThan) ? SignType.GREATER_THAN : SignType.MINOR_THAN;
        RangeSet rangeSet = createMaxOrMinRangeSet(getValueExpression(minorThan), signType, BoundType.OPEN);
        return new RangeRestriction(minorThan, getColumn(minorThan), rangeSet);
    }

    public static RangeRestriction fromMinorThanEquals(MinorThanEquals minorThanEquals) {
        SignType signType = SQLExpressionsUtils.isInverted(minorThanEquals) ? SignType.GREATER_THAN : SignType.MINOR_THAN;
        RangeSet rangeSet = createMaxOrMinRangeSet(getValueExpression(minorThanEquals), signType, BoundType.CLOSED);
        return new RangeRestriction(minorThanEquals, getColumn(minorThanEquals), rangeSet);
    }

    public static RangeRestriction fromBetween(Between between) {
        Column column = (Column) between.getLeftExpression();
        RangeSet left = createMaxOrMinRangeSet(between.getBetweenExpressionStart(), SignType.GREATER_THAN, BoundType.CLOSED);
        RangeSet right = createMaxOrMinRangeSet(between.getBetweenExpressionEnd(), SignType.MINOR_THAN, BoundType.CLOSED);
        Utils.intersectRangeSets(left, right);
        return new RangeRestriction(between, column, left);
    }

    public static RangeRestriction fromIn(InExpression in) {
        Column column = (Column) in.getLeftExpression();
        if (in.getRightItemsList() instanceof ExpressionList) {
            ExpressionList list = (ExpressionList) in.getRightItemsList();
            Expression first = list.getExpressions().get(0);
            if (isIntegerValue(first)) {
                RangeSet<Long> rangeSet = TreeRangeSet.create();
                for (Expression e : list.getExpressions()) {
                    Long value = getLong(e);
                    rangeSet.add(Range.closed(value, value));
                }
                return new RangeRestriction(in, column, rangeSet);
            } else {
                throw new NotImplementedException();
            }
        } else {
            throw new NotImplementedException();
        }
    }

    public static RangeRestriction fromEquals(EqualsTo equalsTo) {
        return getEqualsRangeRestriction(equalsTo);
    }

    private static RangeRestriction getEqualsRangeRestriction(BinaryExpression expression) {
        Column column = getColumn(expression);
        Expression valueExpression = getValueExpression(expression);
        if (isIntegerValue(valueExpression)) {
            RangeSet<Long> rangeSet = TreeRangeSet.create();
            Long value = getLong(valueExpression);
            rangeSet.add(Range.closed(value, value));
            return new RangeRestriction(expression, column, rangeSet);
        } else {
            throw new NotImplementedException();
        }
    }

    public static RangeRestriction fromNotEquals(NotEqualsTo notEqualsTo) {
        return (RangeRestriction) getEqualsRangeRestriction(notEqualsTo).reverse();
    }

    private static RangeSet createMaxOrMinRangeSet(Expression expression, SignType signType, BoundType boundType) {
        if (expression instanceof LongValue || expression instanceof SignedExpression) {
            RangeSet<Long> rangeSet = TreeRangeSet.create();
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

    @Override
    public Restriction reverse() {
        return new RangeRestriction(attributes.get(0), rangeSet.complement());
    }

    @Override
    public String toString() {
        return "RangeRestriction[" + attributes.get(0) + ": " + (expression == null ? "" : expression + ", ") + rangeSet + "]";
    }
}
