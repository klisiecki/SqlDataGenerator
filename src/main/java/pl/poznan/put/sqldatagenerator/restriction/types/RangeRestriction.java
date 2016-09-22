package pl.poznan.put.sqldatagenerator.restriction.types;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.exception.NotImplementedException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DataTypesConverter;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DatabaseType;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;
import pl.poznan.put.sqldatagenerator.util.RangeUtils;

import static com.google.common.collect.BoundType.CLOSED;
import static com.google.common.collect.BoundType.OPEN;
import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.*;
import static pl.poznan.put.sqldatagenerator.restriction.types.SignType.GREATER_THAN;
import static pl.poznan.put.sqldatagenerator.restriction.types.SignType.MINOR_THAN;

public class RangeRestriction extends OneAttributeRestriction {

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

    public void setRange(Range range) {
        this.rangeSet = TreeRangeSet.create();
        rangeSet.add(range);
    }

    public void setRangeSet(RangeSet rangeSet) {
        this.rangeSet = rangeSet;
    }

    public static RangeRestriction fromGreaterThan(GreaterThan greaterThan) {
        SignType signType = isInverted(greaterThan) ? MINOR_THAN : GREATER_THAN;
        Column column = getColumn(greaterThan);
        RangeSet rangeSet = createMaxOrMinRangeSet(column, getValueExpression(greaterThan), signType, OPEN);
        return new RangeRestriction(greaterThan, column, rangeSet);
    }

    public static RangeRestriction fromGreaterThanEquals(GreaterThanEquals greaterThanEquals) {
        SignType signType = isInverted(greaterThanEquals) ? MINOR_THAN : GREATER_THAN;
        Column column = getColumn(greaterThanEquals);
        RangeSet rangeSet = createMaxOrMinRangeSet(column, getValueExpression(greaterThanEquals), signType, CLOSED);
        return new RangeRestriction(greaterThanEquals, column, rangeSet);
    }

    public static RangeRestriction fromMinorThan(MinorThan minorThan) {
        SignType signType = isInverted(minorThan) ? GREATER_THAN : MINOR_THAN;
        Column column = getColumn(minorThan);
        RangeSet rangeSet = createMaxOrMinRangeSet(column, getValueExpression(minorThan), signType, OPEN);
        return new RangeRestriction(minorThan, column, rangeSet);
    }

    public static RangeRestriction fromMinorThanEquals(MinorThanEquals minorThanEquals) {
        SignType signType = isInverted(minorThanEquals) ? GREATER_THAN : MINOR_THAN;
        Column column = getColumn(minorThanEquals);
        RangeSet rangeSet = createMaxOrMinRangeSet(column, getValueExpression(minorThanEquals), signType, CLOSED);
        return new RangeRestriction(minorThanEquals, column, rangeSet);
    }

    public static RangeRestriction fromBetween(Between between) {
        Column column = (Column) between.getLeftExpression();
        RangeSet left = createMaxOrMinRangeSet(column, between.getBetweenExpressionStart(), GREATER_THAN, CLOSED);
        RangeSet right = createMaxOrMinRangeSet(column, between.getBetweenExpressionEnd(), MINOR_THAN, CLOSED);
        RangeSet result = RangeUtils.intersectRangeSets(left, right);
        return new RangeRestriction(between, column, result);
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
                    Double value = DataTypesConverter.getInternalDouble(e, databaseType);
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
            Double value = DataTypesConverter.getInternalDouble(valueExpression, databaseType);
            rangeSet.add(Range.closed(value, value));
            return new RangeRestriction(expression, column, rangeSet);
        } else {
            throw new NotImplementedException();
        }
    }

    private static RangeSet createMaxOrMinRangeSet(Column column, Expression expression, SignType signType,
                                                   BoundType boundType) {
        InternalType type = AttributesMap.get(column).getInternalType();
        DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();
        if (type == InternalType.LONG) {
            RangeSet<Long> rangeSet = TreeRangeSet.create();
            Long value = DataTypesConverter.getInternalLong(expression, databaseType);
            if (signType == GREATER_THAN) {
                rangeSet.add(Range.downTo(value, boundType));
            } else if (signType == MINOR_THAN) {
                rangeSet.add(Range.upTo(value, boundType));
            }
            return rangeSet;
        } else if (type == InternalType.DOUBLE) {
            RangeSet<Double> rangeSet = TreeRangeSet.create();
            Double value = DataTypesConverter.getInternalDouble(expression, databaseType);
            if (signType == GREATER_THAN) {
                rangeSet.add(Range.downTo(value, boundType));
            } else if (signType == MINOR_THAN) {
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
        return "RangeRestriction[" + getAttribute() + ": " +
                (expression == null ? "" : expression + ", ") + rangeSet + "]";
    }
}
