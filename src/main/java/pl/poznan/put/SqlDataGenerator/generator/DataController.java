package pl.poznan.put.SqlDataGenerator.generator;


import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import net.sf.jsqlparser.schema.Table;
import pl.poznan.put.SqlDataGenerator.readers.SQLData;
import pl.poznan.put.SqlDataGenerator.readers.XMLData;
import pl.poznan.put.SqlDataGenerator.restriction.CustomString;
import pl.poznan.put.SqlDataGenerator.restriction.IntegerRestriction;
import pl.poznan.put.SqlDataGenerator.restriction.StringRestriction;
import pl.poznan.put.SqlDataGenerator.sql.model.AttributeRestriction;
import pl.poznan.put.SqlDataGenerator.sql.model.RestrictionEquals;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class DataController {
    private final Map<String, DataTable> tableMap;
    private long maxDataRows = 0;

    public DataController() {
        this.tableMap = new HashMap<>();
    }

    public void initTables(XMLData xmlData, SQLData sqlData) {
        List<String> xmlTables = xmlData.getTables();

        int m = xmlData.getM();
        int t = xmlData.getT();
        System.out.println("t = " + t);
        System.out.println("m = " + m);

        for (Table table : sqlData.getTables()) {
            String originalName = table.getName();
            if (!xmlTables.contains(originalName)) {
                throw new RuntimeException("Table " + originalName + " not found in xml file");
            }
            long dataRows = xmlData.getRows(originalName);
            if (dataRows > m) {
                dataRows = dataRows * t / 100;
            }
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
                Attribute attribute = initializeAttribute(attributeType, attributeName, xmlData.isPrimaryKey(originalName, attributeName), dataRows);
                addXMLRestrictions(originalName, attribute, xmlData);
                dataTable.addAttribute(attribute);
            }
            tableMap.put(dataTable.getName(), dataTable);
        }

        for (Map.Entry<String, DataTable> e : tableMap.entrySet()) {
            DataTable table = e.getValue();
            table.calculateResetFactor(maxDataRows);
        }

        addSQLJoinEquals(sqlData);
        addSQLRestrictions(sqlData);
        propagateEquals();
    }

    public void generate() {
        for (long iteration = 0; iteration < maxDataRows; iteration++) {
            clearTables(iteration);
            generatePrimaryKeys();
            generateRow(iteration > maxDataRows / 2); //TODO współczynnik
            saveTables(iteration);
        }

        closeTableFiles();
    }

    private void clearTables(long iteration) {
        for (Map.Entry<String, DataTable> e : tableMap.entrySet()) {
            DataTable table = e.getValue();
            if (table.shouldBeGenerated(iteration)) {
                table.clear();
            }
        }
    }

    private void generatePrimaryKeys() {
        for (Map.Entry<String, DataTable> e : tableMap.entrySet()) {
            DataTable table = e.getValue();
            if (table.getPrimaryKey() != null) {
                table.getPrimaryKey().generateValue(false);
            }
        }
    }

    private void generateRow(boolean isNegative) {
        for (Map.Entry<String, DataTable> e : tableMap.entrySet()) {
            DataTable table = e.getValue();
            for (Map.Entry<String, Attribute> e2 : table.getAttributeMap().entrySet()) {
                Attribute attribute = e2.getValue();
                attribute.generateValue(isNegative);
            }
        }
    }

    private void saveTables(long iteration) {
        for (Map.Entry<String, DataTable> e : tableMap.entrySet()) {
            DataTable table = e.getValue();
            if (table.shouldBeGenerated(iteration)) {
                table.save();
            }
        }
    }

    private void closeTableFiles() {
        for (Map.Entry<String, DataTable> e : tableMap.entrySet()) {
            DataTable table = e.getValue();
            table.closeTableFile();
        }
    }

    private void propagateEquals() {
        Set<Attribute> processed = new HashSet<>();
        for (Map.Entry<String, DataTable> e : tableMap.entrySet()) {
            DataTable table = e.getValue();
            for (Map.Entry<String, Attribute> e2 : table.getAttributeMap().entrySet()) {
                Attribute attribute = e2.getValue();
                if (!processed.contains(attribute)) {
                    Set<Attribute> clique = new HashSet<>();
                    attribute.collectEquals(clique);
                    for (Attribute a : clique) {
                        a.addEquals(clique);
                    }
                    processed.addAll(clique);
                }
            }
        }
    }

    private void addSQLRestrictions(SQLData sqlData) {
        List<AttributeRestriction> attributeRestrictions = sqlData.getRestrictions();
        for (AttributeRestriction a : attributeRestrictions) {
            Attribute attribute = tableMap.get(a.getTableName()).getAttribute(a.getAttributeName());
            attribute.getRestriction().addAndRangeSet(a.getRestriction().getRangeSet());
            // wartości niepoprawne dla danego arumentu, do generowania danych nie spełniających warunków zapytania
            TreeRangeSet complementSet;
            if (attribute instanceof IntegerAttribute) {
                complementSet = (TreeRangeSet) a.getRestriction().getRangeSet().complement().subRangeSet(Range.closed(Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2));
            } else if (attribute instanceof StringAttribute) {
                complementSet = (TreeRangeSet) a.getRestriction().getRangeSet().complement().subRangeSet(Range.closed(CustomString.MIN_VALUE, CustomString.MAX_VALUE));
            } else {
                throw new NotImplementedException();
            }

            if (!complementSet.isEmpty()) {
                attribute.getNegativeRestriction().addAndRangeSet(complementSet);
            }
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
                throw new RuntimeException("Attribute " + attributeA + " or " + attributeB + " not found");
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

            if (minValue != null) {
                restriction.addAndRange(Range.closed(Integer.parseInt(minValue), Integer.MAX_VALUE));
                negativeRestriction.addAndRange(Range.closed(Integer.parseInt(minValue), Integer.MAX_VALUE));
            }
            if (maxValue != null) {
                restriction.addAndRange(Range.closed(Integer.MIN_VALUE, Integer.parseInt(maxValue)));
                negativeRestriction.addAndRange(Range.closed(Integer.MIN_VALUE, Integer.parseInt(maxValue)));
            }

            if (values != null) {
                TreeRangeSet rangeSet = TreeRangeSet.create();
                for (String value : values) {
                    int v = Integer.parseInt(value);
                    rangeSet.add(Range.closed(v, v));
                }

                restriction.addAndRangeSet(rangeSet);
                negativeRestriction.addAndRangeSet(rangeSet);
            }

        } else if (attribute instanceof StringAttribute) {
            StringRestriction restriction = (StringRestriction) attribute.getRestriction();
            StringRestriction negativeRestriction = (StringRestriction) attribute.getNegativeRestriction();

            if (minValue != null) {
                int minIntValue = Integer.parseInt(minValue);
                restriction.addAndRange(Range.closed(new CustomString('A', minIntValue), CustomString.MAX_VALUE));
                negativeRestriction.addAndRange(Range.closed(new CustomString('A', minIntValue), CustomString.MAX_VALUE));
            }
            if (maxValue != null) {
                int maxIntValue = Integer.parseInt(maxValue);
                restriction.addAndRange(Range.closed(CustomString.MIN_VALUE, new CustomString('z', maxIntValue)));
                negativeRestriction.addAndRange(Range.closed(CustomString.MIN_VALUE, new CustomString('z', maxIntValue)));
            }

            if (values != null) {
                TreeRangeSet rangeSet = TreeRangeSet.create();
                for (String value : values) {
                    rangeSet.add(Range.closed(new CustomString(value), new CustomString(value)));
                }

                restriction.addAndRangeSet(rangeSet);
                negativeRestriction.addAndRangeSet(rangeSet);
            }
        } else {
            throw new NotImplementedException();
        }
    }

    private Attribute initializeAttribute(String type, String name, boolean isPrimaryKey, long dataRows) {
        switch (type) {
            case "INTEGER":
                return new IntegerAttribute(name, isPrimaryKey, dataRows);
            case "STRING":
                return new StringAttribute(name);
            default:
                throw new NotImplementedException();
        }
    }

    @Override
    public String toString() {
        return "DataController{" +
                "tables=" + tableMap +
                '}';
    }
}
