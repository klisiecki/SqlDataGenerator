package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.generator.Attribute;

public class NullRestriction extends OneAttributeRestriction {

    private boolean isNegated;

    public NullRestriction(Attribute attribute) {
        super(attribute);
    }

    public NullRestriction(Expression expression, Column column) {
        super(expression, column);
    }

    private NullRestriction(Attribute attribute, boolean isNegated) {
        super(attribute);
        this.isNegated = isNegated;
    }

    public boolean isNegated() {
        return isNegated;
    }

    @Override
    public Restriction reverse() {
        this.isNegated = !isNegated;
        return this;
    }

    @Override
    public Restriction clone() {
        return new NullRestriction(getAttribute(), isNegated);
    }

    public static NullRestriction fromIsNullExpression(IsNullExpression isNullExpression) {
        return new NullRestriction(isNullExpression, (Column) isNullExpression.getLeftExpression());
    }

    @Override
    public String toString() {
        return "NullRestriction{" + getAttribute() + ", " +
                "isNegated=" + isNegated +
                '}';
    }
}
