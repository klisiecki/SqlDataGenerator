package pl.poznan.put.SqlDataGenerator.sql.model;

import net.sf.jsqlparser.schema.Column;

public class RestrictionEquals implements SQLRestriction {
    private Column leftColumn;
    private Column rightColumn;

    public RestrictionEquals(Column leftColumn, Column rightColumn) {
        this.leftColumn = leftColumn;
        this.rightColumn = rightColumn;
    }

    public Column getLeftColumn() {
        return leftColumn;
    }

    public Column getRightColumn() {
        return rightColumn;
    }
}
