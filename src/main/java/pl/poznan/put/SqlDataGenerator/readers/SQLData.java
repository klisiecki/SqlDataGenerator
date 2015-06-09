package pl.poznan.put.SqlDataGenerator.readers;


import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;
import pl.poznan.put.SqlDataGenerator.sql.AttributesNamesFinder;
import pl.poznan.put.SqlDataGenerator.sql.ConditionEquals;
import pl.poznan.put.SqlDataGenerator.sql.RestrictionsFinder;
import pl.poznan.put.SqlDataGenerator.sql.TablesFinder;

import java.util.List;

public class SQLData {

    private Select select;
    private RestrictionsFinder restrictionsFinder;

    public SQLData(Select select) {
        this.select = select;
        this.restrictionsFinder = new RestrictionsFinder();
    }

    public List<Table> getTables() {
        TablesFinder tablesFinder = new TablesFinder();
        return tablesFinder.getTableList(select);
    }

    public List<String> getAttributes(Table table) {
        AttributesNamesFinder attributesNamesFinder = new AttributesNamesFinder();
        return attributesNamesFinder.getAttributesList(select, table);
    }

    public List<ConditionEquals> getEquals() {
        return restrictionsFinder.findEquals(select);
    }
}
