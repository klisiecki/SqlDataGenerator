package pl.poznan.put.sqldatagenerator.sql.model;

import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;

public class AttributesPair {
    private final Attribute attribute1;
    private final Attribute attribute2;

    public AttributesPair(Column leftColumn, Column rightColumn) {
        this.attribute1 = AttributesMap.get(leftColumn);
        this.attribute2 = AttributesMap.get(rightColumn);
    }

    public Attribute getAttribute1() {
        return attribute1;
    }

    public Attribute getAttribute2() {
        return attribute2;
    }
}
