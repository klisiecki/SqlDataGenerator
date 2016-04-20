package pl.poznan.put.sqldatagenerator.generator;


import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import net.sf.jsqlparser.schema.Table;
import org.apache.log4j.Logger;
import pl.poznan.put.sqldatagenerator.Configuration;
import pl.poznan.put.sqldatagenerator.readers.SQLData;
import pl.poznan.put.sqldatagenerator.readers.XMLData;
import pl.poznan.put.sqldatagenerator.restriction.CustomString;
import pl.poznan.put.sqldatagenerator.restriction.IntegerOldRestriction;
import pl.poznan.put.sqldatagenerator.restriction.RestrictionsManager;
import pl.poznan.put.sqldatagenerator.restriction.StringOldRestriction;
import pl.poznan.put.sqldatagenerator.sql.model.OldAttributeRestriction;
import pl.poznan.put.sqldatagenerator.sql.model.RestrictionEquals;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class DataController {
    private static final Configuration configuration = Configuration.getInstance();
    private static final Logger logger = Logger.getLogger(DataController.class);

    @Deprecated
    private final Map<String, OldDataTable> tableMap;

    private final Map<String, TableBase> tableBaseMap;
    private final Map<String, TableInstance> tableInstanceMap;

    private long maxDataRows = 0;

    private RestrictionsManager restrictionsManager;

    public DataController() {
        this.tableMap = new HashMap<>();
        this.tableBaseMap = new HashMap<>();
        this.tableInstanceMap = new HashMap<>();
        this.restrictionsManager = new RestrictionsManager();
    }

    public void initTables(XMLData xmlData, SQLData sqlData) {
        List<String> xmlTables = xmlData.getTables();

        int m = xmlData.getM();
        int t = xmlData.getT();
        logger.info("t = " + t);
        logger.info("m = " + m);

        // może przyjąć, że w XMLu są zawsze opisane dokładnie te same tabele co w SQL i uprościć pętle?
        for (Table table : sqlData.getTables()) {
            String tableName = table.getName();
            String aliasName = table.getAlias().getName();
            if (!xmlTables.contains(tableName)) {
                throw new RuntimeException("Table " + tableName + " not found in xml file");
            }
            long dataRows = xmlData.getRowsNum(tableName);
            if (dataRows > m) {
                dataRows = dataRows * t / 100;
            }
            if (dataRows > maxDataRows) {
                maxDataRows = dataRows;
            }

            TableBase tableBase =  tableBaseMap.getOrDefault(tableName, new TableBase(tableName, dataRows));
            tableBaseMap.computeIfAbsent(tableName, x -> new TableBase(tableName,dataRows));
            TableInstance tableInstance = new TableInstance(tableBase, aliasName);

            List<String> xmlAttributes = xmlData.getAttributes(tableName);
            for (String attributeName : sqlData.getAttributes(table)) {
                if (!xmlAttributes.contains(attributeName)) {
                    throw new RuntimeException("Attribute " + tableName + "." + attributeName + " not found in xml file");
                }
                String attributeType = xmlData.getType(tableName, attributeName);
                Attribute attribute = initializeAttribute(attributeType, attributeName, xmlData.isPrimaryKey(tableName, attributeName), dataRows);
                //addXMLRestrictions(tableName, attribute, xmlData);
                tableInstance.addAttribute(attribute);
            }
//            oldDataTable.initTableFile();
            tableInstanceMap.put(aliasName,tableInstance);
        }

        for (Map.Entry<String, OldDataTable> e : tableMap.entrySet()) {
            OldDataTable table = e.getValue();
            table.calculateResetFactor(maxDataRows);
        }

        restrictionsManager.setSQLCriteria(sqlData.getCriteria());
        restrictionsManager.setXMLConstraints(xmlData.getConstraints());

//        addSQLJoinEquals(sqlData);
//        addSQLRestrictions(sqlData);
//        propagateEquals();
    }

    public void generate() {
        int positiveRows = (int) (configuration.getSelectivity() * maxDataRows);
        for (long iteration = 0; iteration < maxDataRows; iteration++) {
            if ((iteration + 1) % 100000 == 0) {
                //TODO consider logging progress in time periods
                logger.debug((int) ((double) iteration / maxDataRows * 100) + "%");
            }
            clearTables(iteration);
            generatePrimaryKeys();
            generateRow(iteration >= positiveRows);
            saveTables(iteration);
        }
        closeTableFiles();
    }

    private void clearTables(long iteration) {
        for (Map.Entry<String, OldDataTable> e : tableMap.entrySet()) {
            OldDataTable table = e.getValue();
            if (table.shouldBeGenerated(iteration)) {
                table.clear();
            }
        }
    }

    private void generatePrimaryKeys() {
        for (Map.Entry<String, OldDataTable> e : tableMap.entrySet()) {
            OldDataTable table = e.getValue();
            if (table.getPrimaryKey() != null) {
                table.getPrimaryKey().generateValue(false);
            }
        }
    }

    private void generateRow(boolean isNegative) {
        for (Map.Entry<String, OldDataTable> e : tableMap.entrySet()) {
            OldDataTable table = e.getValue();
            for (Map.Entry<String, Attribute> e2 : table.getAttributeMap().entrySet()) {
                Attribute attribute = e2.getValue();
                attribute.generateValue(isNegative);
            }
        }
    }

    private void saveTables(long iteration) {
        for (Map.Entry<String, OldDataTable> e : tableMap.entrySet()) {
            OldDataTable table = e.getValue();
            if (table.shouldBeGenerated(iteration)) {
                table.save();
            }
        }
    }

    private void closeTableFiles() {
        for (Map.Entry<String, OldDataTable> e : tableMap.entrySet()) {
            OldDataTable table = e.getValue();
            table.closeTableFile();
        }
    }

    private void propagateEquals() {
        Set<Attribute> processed = new HashSet<>();
        for (Map.Entry<String, OldDataTable> e : tableMap.entrySet()) {
            OldDataTable table = e.getValue();
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

    @Deprecated
    private void addSQLRestrictions(SQLData sqlData) {
        List<OldAttributeRestriction> attributeRestrictions = sqlData.getOldRestrictions();
        for (OldAttributeRestriction a : attributeRestrictions) {
            Attribute attribute = tableMap.get(a.getTableName()).getAttribute(a.getAttributeName());
            attribute.getRestriction().addAndRangeSet(a.getRestriction().getRangeSet());
            // ranges complementary to restriction's range, for generating rows non-matching sql query
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

    @Deprecated
    private void addSQLJoinEquals(SQLData sqlData) {
        List<RestrictionEquals> equalsList = sqlData.getJoinEquals();
        for (RestrictionEquals c : equalsList) {
            OldDataTable tableA = tableMap.get(c.getLeftColumn().getTable().getName());
            OldDataTable tableB = tableMap.get(c.getRightColumn().getTable().getName());
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

    @Deprecated
    private void addXMLRestrictions(String tableName, Attribute attribute, XMLData xmlData) {
        String minValue = xmlData.getMinValue(tableName, attribute.getName());
        String maxValue = xmlData.getMaxValue(tableName, attribute.getName());
        List<String> values = xmlData.getValues(tableName, attribute.getName());
        if (attribute instanceof IntegerAttribute) {
            IntegerOldRestriction restriction = (IntegerOldRestriction) attribute.getRestriction();
            IntegerOldRestriction negativeRestriction = (IntegerOldRestriction) attribute.getNegativeRestriction();
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
            StringOldRestriction restriction = (StringOldRestriction) attribute.getRestriction();
            StringOldRestriction negativeRestriction = (StringOldRestriction) attribute.getNegativeRestriction();

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
