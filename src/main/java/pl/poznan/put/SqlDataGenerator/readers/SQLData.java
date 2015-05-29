package pl.poznan.put.SqlDataGenerator.readers;


import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import pl.poznan.put.SqlDataGenerator.AttributesNamesFinder;

import java.util.List;

public class SQLData {

    private Select select;

    public SQLData(Select select) {
        this.select = select;
    }

    public List<String> getTables() {
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        return tablesNamesFinder.getTableList(select);
    }

    public List<String> getAttributes(String table) {
        AttributesNamesFinder attributesNamesFinder = new AttributesNamesFinder();
        return attributesNamesFinder.getAttributesList(select);
    }
}
