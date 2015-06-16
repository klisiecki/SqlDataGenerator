package pl.poznan.put.SqlDataGenerator.generator;


import com.google.common.collect.TreeRangeSet;
import net.sf.jsqlparser.schema.Table;
import pl.poznan.put.SqlDataGenerator.readers.SQLData;
import pl.poznan.put.SqlDataGenerator.readers.XMLData;
import pl.poznan.put.SqlDataGenerator.restriction.IntegerRestriction;
import pl.poznan.put.SqlDataGenerator.restriction.StringRestriction;
import pl.poznan.put.SqlDataGenerator.sql.AttributeRestriction;
import pl.poznan.put.SqlDataGenerator.sql.RestrictionEquals;

import java.util.*;

public class DataController {
    private Map<String, DataTable> tableMap;
    private long maxDataRows = 0;

    public DataController() {
        this.tableMap = new HashMap<>();
    }

    public void initTables(XMLData xmlData, SQLData sqlData) {
        List<String> xmlTables = xmlData.getTables();


        for (Table table : sqlData.getTables()) {
            String originalName = table.getName();
            if (!xmlTables.contains(originalName)) {
                throw new RuntimeException("Table " + originalName + " not found in xml file");
            }
            long dataRows = xmlData.getRows(originalName);
            if (dataRows > maxDataRows) {
                maxDataRows = dataRows;
            }
            DataTable dataTable = new DataTable(table.getAlias().getName(), originalName, dataRows);
            List<String> xmlAttributes = xmlData.getAttributes(originalName);
            for (String attributeName : sqlData.getAttributes(table)) {
                if (!xmlAttributes.contains(attributeName)) {
                    throw new RuntimeException("Attribute " + originalName + "." + attributeName + " not found in xml file");
                }
                String attributeType = xmlData.getType(originalName, attributeName);
                Attribute attribute = initializeAttribute(attributeType, attributeName);
                addXMLRestrictions(originalName, attribute, xmlData);
                dataTable.addAttribute(attribute);
            }
            tableMap.put(dataTable.getName(), dataTable);
            dataTable.initTableFile();
        }


        for (Map.Entry<String, DataTable> e: tableMap.entrySet()) {
            DataTable table = e.getValue();
            table.calculateResetFactor(maxDataRows);
        }

        addSQLJoinEquals(sqlData);
        addSQLRestrictions(sqlData);
        propagateEquals();
    }

    public void generate() {
        for (long iteration = 0; iteration < maxDataRows; iteration++) {
            for (Map.Entry<String, DataTable> e: tableMap.entrySet()) {
                DataTable table = e.getValue();
                if (table.checkIteration(iteration)) {
                    table.clear();
                }
            }
            generateRow();
            for (Map.Entry<String, DataTable> e: tableMap.entrySet()) {
                DataTable table = e.getValue();
                if (table.checkIteration(iteration)) {
                    table.print();
                    table.save();
                }
            }
            System.out.println();
        }

        for (Map.Entry<String, DataTable> e: tableMap.entrySet()) {
            DataTable table = e.getValue();
            table.closeTableFile();
        }
    }

    private void generateRow() {
        for (Map.Entry<String, DataTable> e: tableMap.entrySet()) {
            DataTable table = e.getValue();
            for (Map.Entry<String, Attribute> e2: table.getAttributeMap().entrySet()) {
                Attribute attribute = e2.getValue();
                attribute.generateValue();
            }
        }
    }

    private void propagateEquals() {
        Set<Attribute> processed = new HashSet<>();
        for (Map.Entry<String, DataTable> e: tableMap.entrySet()) {
            DataTable table = e.getValue();
            for (Map.Entry<String, Attribute> e2: table.getAttributeMap().entrySet()) {
                Attribute attribute = e2.getValue();
                if (!processed.contains(attribute)) {
                    Set<Attribute> clique = new HashSet<>();
                    attribute.collectEquals(clique);
                    for (Attribute a: clique) {
                        a.addEquals(clique);
                    }
                    processed.addAll(clique);
                }
            }
        }
    }

    private void addSQLRestrictions(SQLData sqlData) {
        List<AttributeRestriction> attributeRestrictions = sqlData.getRestrictions();
        for (AttributeRestriction a: attributeRestrictions) {
            Attribute attribute = tableMap.get(a.getTableName()).getAttribute(a.getAttributeName());
            //TODO obsługa ORów
            attribute.getRestriction().addAndRangeSet(a.getRestriction().getRangeSet());
        }
    }

    private void addSQLJoinEquals(SQLData sqlData) {
        List<RestrictionEquals> equalsList = sqlData.getJoinEquals();
        for (RestrictionEquals c : equalsList) {
            DataTable tableA = tableMap.get(c.getLeftColumn().getTable().getName());
            DataTable tableB = tableMap.get(c.getRightColumn().getTable().getName());
            Attribute attributeA = tableA.getAttribute(c.getLeftColumn().getColumnName());
            Attribute attributeB = tableB.getAttribute(c.getRightColumn().getColumnName());
            if (attributeA != null && attributeB != null) {
                attributeA.addEquals(attributeB);
                attributeB.addEquals(attributeA);
            } else {
                throw new RuntimeException(attributeA + " or " + attributeB + " not found");
            }

        }
    }

    private void addXMLRestrictions(String tableName, Attribute attribute, XMLData xmlData) {
        String minValue = xmlData.getMinValue(tableName, attribute.getName());
        String maxValue = xmlData.getMaxValue(tableName, attribute.getName());
        List<String> values = xmlData.getValues(tableName, attribute.getName());
        if (attribute instanceof IntegerAttribute) {
            IntegerRestriction restriction = (IntegerRestriction) attribute.getRestriction();
            IntegerRestriction negativeRestriction = (IntegerRestriction) attribute.getNegativeRestriction();

//            restriction.setMinValue(minValue == null ? null: Integer.parseInt(minValue));
//            negativeRestriction.setMinValue(minValue == null ? null: Integer.parseInt(minValue));
//            restriction.setMaxValue(maxValue == null ? null : Integer.parseInt(maxValue));
//            negativeRestriction.setMaxValue(maxValue == null ? null: Integer.parseInt(maxValue));

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

            if (values != null) {
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
