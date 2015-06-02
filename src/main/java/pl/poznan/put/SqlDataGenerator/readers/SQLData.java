package pl.poznan.put.SqlDataGenerator.readers;


import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import pl.poznan.put.SqlDataGenerator.AttributesNamesFinder;
import pl.poznan.put.SqlDataGenerator.TablesFinder;

import java.util.List;

public class SQLData {

    private Select select;

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
}
