package pl.poznan.put.SqlDataGenerator.generator;


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
        for (String tableName: xmlData.getTables()) {
            DataTable table = new DataTable(tableName, xmlData.getRows(tableName));
            for (String attributeName: xmlData.getAttributes(tableName)) {
                String attributeType = xmlData.getType(tableName, attributeName);
                Attribute attribute = initializeAttribute(attributeType, attributeName);
                table.addAttribute(attribute);
            }
            tables.add(table);
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
