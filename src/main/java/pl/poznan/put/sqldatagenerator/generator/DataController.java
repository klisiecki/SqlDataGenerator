package pl.poznan.put.sqldatagenerator.generator;

import net.sf.jsqlparser.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.Configuration;
import pl.poznan.put.sqldatagenerator.readers.SQLData;
import pl.poznan.put.sqldatagenerator.readers.XMLData;
import pl.poznan.put.sqldatagenerator.restriction.RestrictionsManager;
import pl.poznan.put.sqldatagenerator.solver.Solver;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class DataController {
    private static final Configuration configuration = Configuration.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(DataController.class);

    private final Map<String, TableBase> tableBaseMap;
    private final Map<String, TableInstance> tableInstanceMap;

    private long maxDataRows = 0;

    private final Random random;

    private final RestrictionsManager restrictionsManager;
    private final HistoryManager positiveHistoryManager;
    private final HistoryManager negativeHistoryManager;

    public DataController() {
        this.tableBaseMap = new HashMap<>();
        this.tableInstanceMap = new HashMap<>();
        this.random = new Random();
        this.restrictionsManager = new RestrictionsManager();
        this.positiveHistoryManager = new HistoryManager();
        this.negativeHistoryManager = new HistoryManager();
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
        connectKeys(xmlData, sqlData);
        maxDataRows = xmlData.getMaxRowsNum();
        tableBaseMap.values().forEach(table -> table.calculateResetFactor(maxDataRows));

        restrictionsManager.initialize(sqlData.getCriteria(), xmlData.getConstraints(tableBaseMap));
        positiveHistoryManager.initialize(restrictionsManager.getListSize(true));
        negativeHistoryManager.initialize(restrictionsManager.getListSize(false));
    }

    public void generate() {
        logger.info("Generating process started");
        int positiveRows = (int) (configuration.getSelectivity() * maxDataRows);
        Instant lastTimestamp = Instant.now();

        for (long iteration = 0; iteration < maxDataRows; iteration++) {
            if (Duration.between(lastTimestamp, Instant.now()).toMillis() > 1000) {
                lastTimestamp = Instant.now();
                logger.info("Generation progress: {} %", (int) ((double) iteration / maxDataRows * 100));
            }
            prepareTables(iteration);
            generateRow(iteration < positiveRows);
            saveTables(iteration);
        }
        tableBaseMap.values().forEach(TableBase::closeTableFile);

        logger.info("Generating process done");
    }

    private void initTableBase(XMLData xmlData) {
        int m = xmlData.getM();
        int t = xmlData.getT();
        logger.info("m = {}, t = {}", m, t);

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
            AttributeType attributeType = xmlData.getType(tableName, attributeName);
            Attribute attribute = new Attribute(tableInstance, attributeName, attributeType);
            tableInstance.addAttribute(attribute);
            AttributesMap.add(tableInstance, attributeName, attribute);
        }
    }

    private void connectKeys(XMLData xmlData, SQLData sqlData) {
        for (AttributesPair attributesPair : sqlData.getJoinEquals()) {
            Attribute attribute1 = attributesPair.getAttribute1();
            Attribute attribute2 = attributesPair.getAttribute2();
            if (xmlData.isPrimaryKey(attribute1.getBaseTableName(), attribute1.getName())) {
                attribute2.setBaseAttribute(attribute1);
            } else if (xmlData.isPrimaryKey(attribute2.getBaseTableName(), attribute2.getName())) {
                attribute1.setBaseAttribute(attribute2);
            } else {
                throw new RuntimeException("Join on two primary keys or not keys");
            }
        }
    }

    private void prepareTables(long iteration) {
        tableInstanceMap.values().stream()
                .filter(table -> table.shouldBeGenerated(iteration))
                .forEach(TableInstance::getStateAndClear);
    }

    private void generateRow(boolean positive) {
        int restrictionsIndex = random.nextInt(restrictionsManager.getListSize(positive));
        new Solver(restrictionsManager.get(positive, restrictionsIndex)).solve();
    }

    private void saveTables(long iteration) {
        tableInstanceMap.values().stream()
                .filter(table -> table.shouldBeGenerated(iteration))
                .forEach(TableInstance::save);
    }

    @Override
    public String toString() {
        return "DataController{" +
                "tableInstanceMap=" + tableInstanceMap +
                '}';
    }
}
