package pl.poznan.put.sqldatagenerator.restriction.types;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.Utils;
import pl.poznan.put.sqldatagenerator.exception.NotImplementedException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DataTypesConverter;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DatabaseType;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;

import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.*;

public class RangeRestriction extends OneAttributeRestriction {

    private enum SignType {
        GREATER_THAN,
        MINOR_THAN
    }

    private RangeSet rangeSet;

    private RangeRestriction(Expression expression, Column column, RangeSet rangeSet) {
        super(expression, column);
        this.rangeSet = rangeSet;
    }

    public RangeRestriction(Attribute attribute, RangeSet rangeSet) {
        super(attribute);
        this.rangeSet = rangeSet;
    }

    public RangeSet getRangeSet() {
        return rangeSet;
    }

    public static RangeRestriction fromGreaterThan(GreaterThan greaterThan) {
        SignType signType = isInverted(greaterThan) ? SignType.MINOR_THAN : SignType.GREATER_THAN;
        Column column = getColumn(greaterThan);
        RangeSet rangeSet = createMaxOrMinRangeSet(column, getValueExpression(greaterThan), signType, BoundType.OPEN);
        return new RangeRestriction(greaterThan, column, rangeSet);
    }

    public static RangeRestriction fromGreaterThanEquals(GreaterThanEquals greaterThanEquals) {
        SignType signType = isInverted(greaterThanEquals) ? SignType.MINOR_THAN : SignType.GREATER_THAN;
        Column column = getColumn(greaterThanEquals);
        RangeSet rangeSet = createMaxOrMinRangeSet(column, getValueExpression(greaterThanEquals), signType, BoundType.CLOSED);
        return new RangeRestriction(greaterThanEquals, column, rangeSet);
    }

    public static RangeRestriction fromMinorThan(MinorThan minorThan) {
        SignType signType = isInverted(minorThan) ? SignType.GREATER_THAN : SignType.MINOR_THAN;
        Column column = getColumn(minorThan);
        RangeSet rangeSet = createMaxOrMinRangeSet(column, getValueExpression(minorThan), signType, BoundType.OPEN);
        return new RangeRestriction(minorThan, column, rangeSet);
    }

    public static RangeRestriction fromMinorThanEquals(MinorThanEquals minorThanEquals) {
        SignType signType = isInverted(minorThanEquals) ? SignType.GREATER_THAN : SignType.MINOR_THAN;
        Column column = getColumn(minorThanEquals);
        RangeSet rangeSet = createMaxOrMinRangeSet(column, getValueExpression(minorThanEquals), signType, BoundType.CLOSED);
        return new RangeRestriction(minorThanEquals, column, rangeSet);
    }

    public static RangeRestriction fromBetween(Between between) {
        Column column = (Column) between.getLeftExpression();
        RangeSet left = createMaxOrMinRangeSet(column, between.getBetweenExpressionStart(), SignType.GREATER_THAN, BoundType.CLOSED);
        RangeSet right = createMaxOrMinRangeSet(column, between.getBetweenExpressionEnd(), SignType.MINOR_THAN, BoundType.CLOSED);
        Utils.intersectRangeSets(left, right);
        return new RangeRestriction(between, column, left);
    }

    public static RangeRestriction fromIn(InExpression in) {
        Column column = (Column) in.getLeftExpression();
        if (in.getRightItemsList() instanceof ExpressionList) {
            ExpressionList list = (ExpressionList) in.getRightItemsList();
            Expression first = list.getExpressions().get(0);
            DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();
            if (isIntegerValue(first)) {
                RangeSet<Long> rangeSet = TreeRangeSet.create();
                for (Expression e : list.getExpressions()) {
                    Long value = DataTypesConverter.getInternalLong(e, databaseType);
                    rangeSet.add(Range.closed(value, value));
                }
                return new RangeRestriction(in, column, rangeSet);
            } else if (isDoubleValue(first)) {
                RangeSet<Double> rangeSet = TreeRangeSet.create();
                for (Expression e : list.getExpressions()) {
                    Double value = getDouble(e);
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

    public static RangeRestriction fromNotEquals(NotEqualsTo notEqualsTo) {
        return (RangeRestriction) getEqualsRangeRestriction(notEqualsTo).reverse();
    }

    private static RangeRestriction getEqualsRangeRestriction(BinaryExpression expression) {
        Column column = getColumn(expression);
        Expression valueExpression = getValueExpression(expression);
        InternalType internalType = AttributesMap.get(column).getInternalType();
        DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();
        if (internalType == InternalType.LONG) {
            RangeSet<Long> rangeSet = TreeRangeSet.create();
            Long value = DataTypesConverter.getInternalLong(valueExpression, databaseType);
            rangeSet.add(Range.closed(value, value));
            return new RangeRestriction(expression, column, rangeSet);
        } else if (internalType == InternalType.DOUBLE) {
            RangeSet<Double> rangeSet = TreeRangeSet.create();
            Double value = getDouble(valueExpression);
            rangeSet.add(Range.closed(value, value));
            return new RangeRestriction(expression, column, rangeSet);
        } else {
            throw new NotImplementedException();
        }
    }

    private static RangeSet createMaxOrMinRangeSet(Column column, Expression expression, SignType signType, BoundType boundType) {
        InternalType type = AttributesMap.get(column).getInternalType();
        DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();
        if (type == InternalType.LONG) {
            RangeSet<Long> rangeSet = TreeRangeSet.create();
            Long value = DataTypesConverter.getInternalLong(expression, databaseType);
            if (signType == SignType.GREATER_THAN) {
                rangeSet.add(Range.downTo(value, boundType));
            } else if (signType == SignType.MINOR_THAN) {
                rangeSet.add(Range.upTo(value, boundType));
            }
            return rangeSet;
        } else if (type == InternalType.DOUBLE) {
            RangeSet<Double> rangeSet = TreeRangeSet.create();
            Double value = getDouble(expression);
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
        rangeSet = rangeSet.complement();
        return this;
    }

    @Override
    public Restriction clone() {
        return new RangeRestriction(getAttribute(), TreeRangeSet.create(rangeSet));
    }

    @Override
    public String toString() {
        return "RangeRestriction[" + getAttribute() + ": " + (expression == null ? "" : expression + ", ") + rangeSet + "]";
    }
}
