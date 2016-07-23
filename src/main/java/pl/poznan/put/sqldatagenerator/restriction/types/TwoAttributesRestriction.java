package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.generator.Attribute;

import java.util.Arrays;

public abstract class TwoAttributesRestriction extends MultipleAttributesRestriction {

    protected TwoAttributesRestriction(Expression expression, Column firstColumn, Column secondColumn) {
        super(expression, Arrays.asList(firstColumn, secondColumn));
    }

    public Attribute getFirstAttribute() {
        return attributes.get(0);
    }

    public Attribute getSecondAttribute() {
        return attributes.get(1);
    }
}
