package pl.poznan.put.sqldatagenerator.readers;


import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;
import pl.poznan.put.sqldatagenerator.sql.AttributesNamesFinder;
import pl.poznan.put.sqldatagenerator.sql.EqualsFinder;
import pl.poznan.put.sqldatagenerator.sql.SimpleRestrictionFinder;
import pl.poznan.put.sqldatagenerator.sql.TablesFinder;
import pl.poznan.put.sqldatagenerator.sql.model.AttributeRestriction;
import pl.poznan.put.sqldatagenerator.sql.model.RestrictionEquals;

import java.util.List;

public class SQLData {

    private final Select selectStatement;

    public SQLData(Select selectStatement) {
        this.selectStatement = selectStatement;
    }

    public List<Table> getTables() {
        TablesFinder tablesFinder = new TablesFinder();
        return tablesFinder.getTableList(selectStatement);
    }

    public List<String> getAttributes(Table table) {
        AttributesNamesFinder attributesNamesFinder = new AttributesNamesFinder();
        return attributesNamesFinder.getAttributesList(selectStatement, table);
    }

    public List<RestrictionEquals> getJoinEquals() {
        EqualsFinder equalsFinder = new EqualsFinder();
        return equalsFinder.findEquals(selectStatement);
    }

    public List<AttributeRestriction> getRestrictions() {
        SimpleRestrictionFinder restrictionFinder = new SimpleRestrictionFinder();
        return restrictionFinder.findRestrictions(selectStatement);
    }
}
