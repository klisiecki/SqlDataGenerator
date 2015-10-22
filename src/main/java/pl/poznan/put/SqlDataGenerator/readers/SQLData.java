package pl.poznan.put.SqlDataGenerator.readers;


import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;
import pl.poznan.put.SqlDataGenerator.sql.*;
import pl.poznan.put.SqlDataGenerator.sql.model.AttributeRestriction;
import pl.poznan.put.SqlDataGenerator.sql.model.RestrictionEquals;

import java.util.List;

public class SQLData {

    private final Select select;

    public SQLData(Select select) {
        this.select = select;
    }

    public List<Table> getTables() {
        TablesFinder tablesFinder = new TablesFinder();
        return tablesFinder.getTableList(select);
    }

    public List<String> getAttributes(Table table) {
        AttributesNamesFinder attributesNamesFinder = new AttributesNamesFinder();
        return attributesNamesFinder.getAttributesList(select, table);
    }

    public List<RestrictionEquals> getJoinEquals() {
        EqualsFinder equalsFinder = new EqualsFinder();
        return equalsFinder.findEquals(select);
    }

    public List<AttributeRestriction> getRestrictions() {
        SimpleRestrictionFinder restrictionFinder = new SimpleRestrictionFinder();
        return restrictionFinder.findRestrictions(select);
    }
}
