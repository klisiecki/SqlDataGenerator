package pl.poznan.put.SqlDataGenerator.generator;


import net.sf.jsqlparser.schema.Table;
import pl.poznan.put.SqlDataGenerator.readers.SQLData;
import pl.poznan.put.SqlDataGenerator.readers.XMLData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataController {
    private List<DataTable> tables;

    public DataController() {
        this.tables = new ArrayList<>();
    }

    public void initTables(XMLData xmlData, SQLData sqlData) {
        List<String> xmlTables = xmlData.getTables();

        for (Table table : sqlData.getTables()) {
            String tableName = table.getName();
            if (!xmlTables.contains(tableName)) {
                throw new RuntimeException("Table " + tableName + " not found in xml file");
            }
            DataTable dataTable = new DataTable(tableName, xmlData.getRows(tableName));
            List<String> xmlAttributes = xmlData.getAttributes(tableName);
            for (String attributeName : sqlData.getAttributes(table)) {
                if (!xmlAttributes.contains(attributeName)) {
                    throw new RuntimeException("Attribute " + tableName + "." + attributeName + " not found in xml file");
                }
                String attributeType = xmlData.getType(tableName, attributeName);
                Attribute attribute = initializeAttribute(attributeType, attributeName);
                dataTable.addAttribute(attribute);
            }
            tables.add(dataTable);
        }
    }

    private Attribute initializeAttribute(String type, String name) {
        if (type.equals("INTEGER")) {
            return new Attribute<Integer>(name);
        } else if (type.equals("STRING")) {
            return new Attribute<String>(name);
        } else if (type.equals("FLOAT")) {
            return new Attribute<Float>(name);
        } else if (type.equals("DATE")) {
            return new Attribute<Date>(name);
        } else {
            throw new RuntimeException("type " + type + " not defined");
        }
    }

    @Override
    public String toString() {
        return "DataController{" +
                "tables=" + tables +
                '}';
    }
}
