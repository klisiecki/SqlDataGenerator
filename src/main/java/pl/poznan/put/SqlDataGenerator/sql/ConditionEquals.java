package pl.poznan.put.SqlDataGenerator.sql;

import net.sf.jsqlparser.schema.Column;

public class ConditionEquals implements SQLCondition {
    private Column leftColumn;
    private Column rightColumn;

    public ConditionEquals(Column leftColumn, Column rightColumn) {
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
