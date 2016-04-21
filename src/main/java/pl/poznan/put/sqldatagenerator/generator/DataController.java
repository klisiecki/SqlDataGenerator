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
        initTableBase(xmlData);

        for (Table table : sqlData.getTables()) {
            String tableName = table.getName();
            String aliasName = table.getAlias().getName();
            if (!tableBaseMap.containsKey(tableName)) {
                throw new RuntimeException("Table " + tableName + " not found in xml file");
            }

            TableInstance tableInstance = new TableInstance(tableBaseMap.get(tableName), aliasName);
            initAttributes(xmlData, sqlData, table, tableInstance);
            tableInstanceMap.put(aliasName, tableInstance);
        }

        maxDataRows = xmlData.getMaxRowsNum();
        for (Map.Entry<String, TableBase> table : tableBaseMap.entrySet()) {
            table.getValue().calculateResetFactor(maxDataRows);
        }

        restrictionsManager.setSQLCriteria(sqlData.getCriteria());
        restrictionsManager.setXMLConstraints(xmlData.getConstraints());
    }

    private void initTableBase(XMLData xmlData) {
        int m = xmlData.getM();
        int t = xmlData.getT();
        logger.info("m = " + m + ", t = " + t);

        for (String tableName : xmlData.getTables()) {
            long count = xmlData.getRowsNum(tableName);
            if (count > m) {
                count = count * t / 100;
            }
            tableBaseMap.put(tableName, new TableBase(tableName, xmlData.getAttributes(tableName), count));
        }
    }

    private void initAttributes(XMLData xmlData, SQLData sqlData, Table table, TableInstance tableInstance) {
        String tableName = table.getName();
        List<String> xmlAttributes = xmlData.getAttributes(tableName);
        List<String> sqlAttributes = sqlData.getAttributes(table);

        List<String> missingAttributes = new ArrayList<>(sqlAttributes);
        missingAttributes.removeAll(xmlAttributes);
        if (!missingAttributes.isEmpty()) {
            throw new RuntimeException("Attributes " + Arrays.toString(missingAttributes.toArray()) + " not found in " + tableName + " definition");
        }

        for (String attributeName : xmlAttributes) {
            //TODO consider adding only attributes present in SQL (could be configurable)
            AttributeType attributeType = AttributeType.valueOf(xmlData.getType(tableName, attributeName));
            AttributeBase attributeBase = new AttributeBase(attributeType);

            AttributeInstance attributeInstance = new AttributeInstance(attributeBase, attributeName);
            tableInstance.addAttribute(attributeInstance);
        }
    }

    @Deprecated
    public void generate() {
        for (long iteration = 0; iteration < maxDataRows; iteration++) {
            if ((iteration + 1) % 100000 == 0) {
                //TODO consider logging progress in time periods
                logger.debug((int) ((double) iteration / maxDataRows * 100) + "%");
            }
            clearTables(iteration);
            generateRow();
            saveTables(iteration);
        }
        closeTableFiles();
    }

    private void clearTables(long iteration) {
        for (Map.Entry<String, TableInstance> e : tableInstanceMap.entrySet()) {
            TableInstance table = e.getValue();
            if (table.getBase().shouldBeGenerated(iteration)) {
                table.clear();
            }
        }
    }

    private void generateRow() {
        int positiveRows = (int) (configuration.getSelectivity() * maxDataRows);
        tableBaseMap.entrySet().forEach(e -> e.getValue().saveAll());
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
        for (Map.Entry<String, TableBase> e : tableBaseMap.entrySet()) {
            e.getValue().closeTableFile();
        }
    }

    @Deprecated
    private void propagateEquals() {
        Set<OldAttribute> processed = new HashSet<>();
        for (Map.Entry<String, OldDataTable> e : tableMap.entrySet()) {
            OldDataTable table = e.getValue();
            for (Map.Entry<String, OldAttribute> e2 : table.getAttributeMap().entrySet()) {
                OldAttribute oldAttribute = e2.getValue();
                if (!processed.contains(oldAttribute)) {
                    Set<OldAttribute> clique = new HashSet<>();
                    oldAttribute.collectEquals(clique);
                    for (OldAttribute a : clique) {
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
            OldAttribute oldAttribute = tableMap.get(a.getTableName()).getAttribute(a.getAttributeName());
            oldAttribute.getRestriction().addAndRangeSet(a.getRestriction().getRangeSet());
            // ranges complementary to restriction's range, for generating rows non-matching sql query
            TreeRangeSet complementSet;
            if (oldAttribute instanceof OldIntegerAttribute) {
                complementSet = (TreeRangeSet) a.getRestriction().getRangeSet().complement().subRangeSet(Range.closed(Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2));
            } else if (oldAttribute instanceof OldStringAttribute) {
                complementSet = (TreeRangeSet) a.getRestriction().getRangeSet().complement().subRangeSet(Range.closed(CustomString.MIN_VALUE, CustomString.MAX_VALUE));
            } else {
                throw new NotImplementedException();
            }

            if (!complementSet.isEmpty()) {
                oldAttribute.getNegativeRestriction().addAndRangeSet(complementSet);
            }
        }
    }

    @Deprecated
    private void addSQLJoinEquals(SQLData sqlData) {
        List<RestrictionEquals> equalsList = sqlData.getJoinEquals();
        for (RestrictionEquals c : equalsList) {
            OldDataTable tableA = tableMap.get(c.getLeftColumn().getTable().getName());
            OldDataTable tableB = tableMap.get(c.getRightColumn().getTable().getName());
            OldAttribute oldAttributeA = tableA.getAttribute(c.getLeftColumn().getColumnName());
            OldAttribute oldAttributeB = tableB.getAttribute(c.getRightColumn().getColumnName());
            if (oldAttributeA != null && oldAttributeB != null) {
                oldAttributeA.addEquals(oldAttributeB);
                oldAttributeB.addEquals(oldAttributeA);
            } else {
                throw new RuntimeException("OldAttribute " + oldAttributeA + " or " + oldAttributeB + " not found");
            }
        }
    }

    @Deprecated
    private void addXMLRestrictions(String tableName, OldAttribute oldAttribute, XMLData xmlData) {
        String minValue = xmlData.getMinValue(tableName, oldAttribute.getName());
        String maxValue = xmlData.getMaxValue(tableName, oldAttribute.getName());
        List<String> values = xmlData.getValues(tableName, oldAttribute.getName());
        if (oldAttribute instanceof OldIntegerAttribute) {
            IntegerOldRestriction restriction = (IntegerOldRestriction) oldAttribute.getRestriction();
            IntegerOldRestriction negativeRestriction = (IntegerOldRestriction) oldAttribute.getNegativeRestriction();
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

        } else if (oldAttribute instanceof OldStringAttribute) {
            StringOldRestriction restriction = (StringOldRestriction) oldAttribute.getRestriction();
            StringOldRestriction negativeRestriction = (StringOldRestriction) oldAttribute.getNegativeRestriction();

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

    @Override
    public String toString() {
        return "DataController{" +
                "tableInstanceMap=" + tableInstanceMap +
                '}';
    }
}
