package pl.poznan.put.SqlDataGenerator.sql;

import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.SqlDataGenerator.restriction.Restriction;

public class AttributeRestriction implements SQLRestriction {
    private Column column;
    private Restriction restriction;

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
