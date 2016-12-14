package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.exception.SQLSyntaxNotSupportedException;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;

import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class MultipleAttributesRestriction extends Restriction {

    protected MultipleAttributesRestriction(Expression expression, List<Column> columns) {
        super(expression, columns.stream().map(AttributesMap::get).collect(toList()));
        InternalType firstType = attributes.get(0).getInternalType();
        if (!attributes.stream().allMatch(a -> a.getInternalType() == firstType)) {
            throw new SQLSyntaxNotSupportedException("Attributes must have the same internal type");
        }
    }
}
