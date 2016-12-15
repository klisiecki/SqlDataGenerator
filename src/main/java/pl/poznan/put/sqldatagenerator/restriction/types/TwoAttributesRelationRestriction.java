package pl.poznan.put.sqldatagenerator.restriction.types;

import com.google.common.collect.BoundType;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

public class TwoAttributesRelationRestriction extends TwoAttributesRestriction {

    private SignType signType;
    private BoundType boundType;

    private TwoAttributesRelationRestriction(BinaryExpression expression, SignType signType, BoundType boundType) {
        super(expression, (Column) expression.getLeftExpression(), (Column) expression.getRightExpression());
        this.signType = signType;
        this.boundType = boundType;
    }

    public SignType getSignType() {
        return signType;
    }

    public BoundType getBoundType() {
        return boundType;
    }

    public static TwoAttributesRelationRestriction fromGreaterThan(GreaterThan greaterThan) {
        return new TwoAttributesRelationRestriction(greaterThan, SignType.GREATER_THAN, BoundType.OPEN);
    }

    public static TwoAttributesRelationRestriction fromGreaterThanEquals(GreaterThanEquals greaterThanEquals) {
        return new TwoAttributesRelationRestriction(greaterThanEquals, SignType.GREATER_THAN, BoundType.CLOSED);
    }

    public static TwoAttributesRelationRestriction fromMinorThan(MinorThan minorThan) {
        return new TwoAttributesRelationRestriction(minorThan, SignType.MINOR_THAN, BoundType.OPEN);
    }

    public static TwoAttributesRelationRestriction fromMinorThanEquals(MinorThanEquals minorThanEquals) {
        return new TwoAttributesRelationRestriction(minorThanEquals, SignType.MINOR_THAN, BoundType.CLOSED);
    }

    public static TwoAttributesRelationRestriction fromEquals(EqualsTo equalsTo) {
        return new TwoAttributesRelationRestriction(equalsTo, SignType.EQUALS, null);
    }

    public static TwoAttributesRelationRestriction fromNotEquals(NotEqualsTo notEqualsTo) {
        return new TwoAttributesRelationRestriction(notEqualsTo, SignType.NOT_EQUALS, null);
    }

    @Override
    public Restriction reverse() {
        this.signType = signType.reversed();
        return this;
    }

    @Override
    public Restriction copy() {
        return new TwoAttributesRelationRestriction((BinaryExpression) expression, signType, boundType);
    }

}
