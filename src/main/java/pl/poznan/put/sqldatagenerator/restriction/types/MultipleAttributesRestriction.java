package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;

import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class MultipleAttributesRestriction extends Restriction {
    protected MultipleAttributesRestriction(Expression expression, List<Column> columns) {
        super(expression);
        this.attributes = columns.stream().map(AttributesMap::get).collect(toList());
    }
}
