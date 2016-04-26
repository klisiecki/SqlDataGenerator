package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import pl.poznan.put.sqldatagenerator.generator.Attribute;

public abstract class OneAttributeRestriction extends Restriction {
    private Attribute attribute;

    public OneAttributeRestriction(Expression expression) {
        super(expression);
    }
}
