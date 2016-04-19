package pl.poznan.put.sqldatagenerator.sql.model;

import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.restriction.Restriction;

public class AttributeRestriction implements SQLRestriction {
    private final Column column;
    private final Restriction restriction;

    public AttributeRestriction(Column column, Restriction restriction) {
        this.column = column;
        this.restriction = restriction;
    }

    public String getTableName() {
        return column.getTable().getName();
    }

    public String getAttributeName() {
        return column.getColumnName();
    }

    public Restriction getRestriction() {
        return restriction;
    }
}
