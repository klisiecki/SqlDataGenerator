package pl.poznan.put.SqlDataGenerator.generator;


import net.sf.jsqlparser.schema.Table;
import pl.poznan.put.SqlDataGenerator.readers.SQLData;
import pl.poznan.put.SqlDataGenerator.readers.XMLData;
import pl.poznan.put.SqlDataGenerator.restriction.NumberRestriction;
import pl.poznan.put.SqlDataGenerator.restriction.StringRestriction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataController {
    private Map<String, DataTable> tableMap;

    public DataController() {
        this.tableMap = new HashMap<>();
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
                addRestrictions(tableName, attribute, xmlData);
                dataTable.addAttribute(attribute);
            }
            tableMap.put(dataTable.getName(), dataTable);
        }
    }

    public void generate() {
        for (Map.Entry<String, DataTable> e: tableMap.entrySet()) {
            DataTable table = e.getValue();
            for (Map.Entry<String, Attribute> e2: table.getAttributeMap().entrySet()) {
                Attribute attribute = e2.getValue();
                attribute.generateValue();
            }
        }
    }

    public void print() {
        for (Map.Entry<String, DataTable> e: tableMap.entrySet()) {
            DataTable table = e.getValue();
            System.out.print(table.getName() + ": ");
            for (Map.Entry<String, Attribute> e2: table.getAttributeMap().entrySet()) {
                Attribute attribute = e2.getValue();
                System.out.print(attribute + ", ");
            }
            System.out.println();
        }
    }

    private void addRestrictions(String tableName, Attribute attribute, XMLData xmlData) {
        String minValue = xmlData.getMinValue(tableName, attribute.getName());
        String maxValue = xmlData.getMaxValue(tableName, attribute.getName());
        List<String> values = xmlData.getValues(tableName, attribute.getName());
        if (attribute instanceof IntegerAttribute) {
            NumberRestriction<Integer> restriction = (NumberRestriction<Integer>) attribute.getRestriction();
            NumberRestriction<Integer> negativeRestriction = (NumberRestriction<Integer>) attribute.getNegativeRestriction();

            restriction.setMinValue(minValue == null ? null: Integer.parseInt(minValue));
            negativeRestriction.setMinValue(minValue == null ? null: Integer.parseInt(minValue));
            restriction.setMinValue(maxValue == null ? null: Integer.parseInt(maxValue));
            negativeRestriction.setMinValue(maxValue == null ? null: Integer.parseInt(maxValue));

            if (values != null) {
                List<Integer> integerValues = new ArrayList<>();
                for (String value : values) {
                    integerValues.add(Integer.parseInt(value));
                }
                restriction.setValues(integerValues);
                negativeRestriction.setValues(integerValues);
            }
        } else if (attribute instanceof StringAttribute) {
            StringRestriction restriction = (StringRestriction) attribute.getRestriction();
            StringRestriction negativeRestriction = (StringRestriction) attribute.getNegativeRestriction();

            restriction.setMinLength(minValue == null ? null : Integer.parseInt(minValue));
            negativeRestriction.setMinLength(minValue == null ? null : Integer.parseInt(minValue));
            restriction.setMaxLength(maxValue == null ? null : Integer.parseInt(maxValue));
            negativeRestriction.setMaxLength(maxValue == null ? null: Integer.parseInt(maxValue));

            if (values == null) {
                restriction.setValues(values);
                negativeRestriction.setValues(values);
            }
        }
    }

    private Attribute initializeAttribute(String type, String name) {
        if (type.equals("INTEGER")) {
            return new IntegerAttribute(name);
        } else if (type.equals("STRING")) {
            return new StringAttribute(name);
        } else {
//        if (type.equals("FLOAT")) {
//            return new Attribute<Float>(name);
//        } else if (type.equals("DATE")) {
//            return new Attribute<Date>(name);
//        } else {
            throw new RuntimeException("type " + type + " not defined");
        }
    }

    @Override
    public String toString() {
        return "DataController{" +
                "tables=" + tableMap +
                '}';
    }
}
