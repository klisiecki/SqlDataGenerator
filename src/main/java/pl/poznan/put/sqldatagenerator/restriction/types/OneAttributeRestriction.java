package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;

public abstract class OneAttributeRestriction extends Restriction {
    protected Attribute attribute;

    public OneAttributeRestriction(Expression expression, Column column) {
        super(expression);
        this.attribute = AttributesMap.get(column.getTable().getName(), column.getColumnName());
    }
}
