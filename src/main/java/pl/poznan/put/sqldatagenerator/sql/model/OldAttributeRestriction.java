package pl.poznan.put.sqldatagenerator.sql.model;

import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.restriction.OldRestriction;

@Deprecated
public class OldAttributeRestriction implements SQLRestriction {
    private final Column column;
    private final OldRestriction restriction;

    public OldAttributeRestriction(Column column, OldRestriction restriction) {
        this.column = column;
        this.restriction = restriction;
    }

    public String getTableName() {
        return column.getTable().getName();
    }

    public String getAttributeName() {
        return column.getColumnName();
    }

    public OldRestriction getRestriction() {
        return restriction;
    }
}
