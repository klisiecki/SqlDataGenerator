package pl.poznan.put.sqldatagenerator.sql.model;

import net.sf.jsqlparser.schema.Column;

public class RestrictionEquals implements SQLRestriction {
    private final Column leftColumn;
    private final Column rightColumn;

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
