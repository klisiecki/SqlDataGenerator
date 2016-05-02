package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;

import static java.util.Collections.singletonList;

public abstract class OneAttributeRestriction extends Restriction {

    public OneAttributeRestriction(Expression expression, Column column) {
        super(expression);
        this.attributes = singletonList(AttributesMap.get(column.getTable().getName(), column.getColumnName()));
    }

    public OneAttributeRestriction(Attribute attribute) {
        super(null);
        this.attributes = singletonList(attribute);
    }
}
