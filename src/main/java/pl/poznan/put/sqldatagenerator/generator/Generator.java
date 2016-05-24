package pl.poznan.put.sqldatagenerator.generator;

import net.sf.jsqlparser.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.Configuration;
import pl.poznan.put.sqldatagenerator.readers.DatabaseProperties;
import pl.poznan.put.sqldatagenerator.readers.SQLData;
import pl.poznan.put.sqldatagenerator.restriction.RestrictionsManager;
import pl.poznan.put.sqldatagenerator.solver.Solver;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Generator {
    private static final Configuration configuration = Configuration.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(Generator.class);

    private final Map<String, TableBase> tableBaseMap;
    private final Map<String, TableInstance> tableInstanceMap;

    private long maxDataRows = 0;

    private final Random random;

    private final RestrictionsManager restrictionsManager;
    private final HistoryManager positiveHistoryManager;
    private final HistoryManager negativeHistoryManager;

    public Generator() {
        this.tableBaseMap = new HashMap<>();
        this.tableInstanceMap = new HashMap<>();
        this.random = new Random();
        this.restrictionsManager = new RestrictionsManager();
        this.positiveHistoryManager = new HistoryManager();
        this.negativeHistoryManager = new HistoryManager();
    }

    public void initTables(DatabaseProperties databaseProperties, SQLData sqlData) {
        initTableBase(databaseProperties);

        for (Table table : sqlData.getTables()) {
            String tableName = table.getName();
            String aliasName = table.getAlias().getName();
            if (!tableBaseMap.containsKey(tableName)) {
                throw new RuntimeException("Table " + tableName + " not found in database properties");
            }

            TableInstance tableInstance = new TableInstance(tableBaseMap.get(tableName), aliasName);
            initAttributes(databaseProperties, sqlData, table, tableInstance);
            tableInstanceMap.put(aliasName, tableInstance);
        }
        connectKeys(databaseProperties, sqlData);
        maxDataRows = databaseProperties.getMaxRowsNum();

        restrictionsManager.initialize(sqlData.getCriteria(), databaseProperties.getConstraints(tableBaseMap));
        positiveHistoryManager.initialize(restrictionsManager.getListSize(true));
        negativeHistoryManager.initialize(restrictionsManager.getListSize(false));
    }

    public void generate() {
        logger.info("Generating process started");
        int positiveRows = (int) (configuration.getSelectivity() * maxDataRows);
        Instant lastTimestamp = Instant.now();

        for (long iteration = 0; iteration < maxDataRows; iteration++) {
            float progress = (float) iteration / maxDataRows;
            if (Duration.between(lastTimestamp, Instant.now()).toMillis() > 1000) {
                lastTimestamp = Instant.now();
                logger.info("Generation progress: {}%", (int) (progress * 100));
            }
            boolean positive = iteration < positiveRows;
            int restrictionsIndex = random.nextInt(restrictionsManager.getListSize(positive));
            prepareTables(restrictionsIndex, positive, progress);
            generateRow(restrictionsIndex, positive);
            saveTables(restrictionsIndex, positive);
        }
        tableBaseMap.values().forEach(TableBase::closeTableFile);

        logger.info("Generating process done");
    }

    private void prepareTables(int restrictionsIndex, boolean positive, float progress) {
        TablesState tablesState = positive ? positiveHistoryManager.get(restrictionsIndex)
                : negativeHistoryManager.get(restrictionsIndex);

        if (tablesState != null) {
            tableInstanceMap.values().stream()
                    .filter(table -> !table.shouldBeGenerated(progress))
                    .forEach(tableInstance -> tableInstance.setState(tablesState.get(tableInstance.getAliasName())));
        }
    }

    private int generateRow(int restrictionsIndex, boolean positive) {
        new Solver(restrictionsManager.get(positive, restrictionsIndex)).solve();
        return restrictionsIndex;
    }

    private void saveTables(int restrictionsIndex, boolean positive) {
        tableInstanceMap.values().stream()
                .filter(table -> !table.getState().isSaved())
                .forEach(TableInstance::save);

        TablesState tablesState = new TablesState();
        tableInstanceMap.values().forEach(tableInstance ->
                tablesState.add(tableInstance.getAliasName(), tableInstance.getState()));

        if (positive) {
            positiveHistoryManager.add(restrictionsIndex, tablesState);
        } else {
            negativeHistoryManager.add(restrictionsIndex, tablesState);
        }
        tableInstanceMap.values().forEach(TableInstance::clear);
    }

    private void initTableBase(DatabaseProperties databaseProperties) {
        int m = databaseProperties.databasePropertiesReader.getM();
        int t = databaseProperties.getT();
        logger.info("m = {}, t = {}", m, t);

        for (String tableName : databaseProperties.getTables()) {
            long count = databaseProperties.getRowsNum(tableName);
            if (count > m) {
                count = count * t / 100;
            }
            tableBaseMap.put(tableName, new TableBase(tableName, databaseProperties.getAttributes(tableName), count));
        }
    }

    private void initAttributes(DatabaseProperties databaseProperties, SQLData sqlData, Table table, TableInstance tableInstance) {
        String tableName = table.getName();
        List<String> databaseAttributes = databaseProperties.getAttributes(tableName);
        List<String> sqlAttributes = sqlData.getAttributes(table);

        List<String> missingAttributes = new ArrayList<>(sqlAttributes);
        missingAttributes.removeAll(databaseAttributes);
        if (!missingAttributes.isEmpty()) {
            throw new RuntimeException("Attributes " + Arrays.toString(missingAttributes.toArray()) + " not found in " + tableName + " definition");
        }

        for (String attributeName : databaseAttributes) {
            //TODO consider adding only attributes present in SQL (could be configurable)
            AttributeType attributeType = databaseProperties.getType(tableName, attributeName);
            Attribute attribute = new Attribute(tableInstance, attributeName, attributeType);
            tableInstance.addAttribute(attribute);
            AttributesMap.add(tableInstance, attributeName, attribute);
        }
    }

    private void connectKeys(DatabaseProperties databaseProperties, SQLData sqlData) {
        for (AttributesPair attributesPair : sqlData.getJoinEquals()) {
            Attribute attribute1 = attributesPair.getAttribute1();
            Attribute attribute2 = attributesPair.getAttribute2();
            if (databaseProperties.isPrimaryKey(attribute1.getBaseTableName(), attribute1.getName())) {
                attribute2.setBaseAttribute(attribute1);
            } else if (databaseProperties.isPrimaryKey(attribute2.getBaseTableName(), attribute2.getName())) {
                attribute1.setBaseAttribute(attribute2);
            } else {
                throw new RuntimeException("Join on two primary keys or not keys");
            }
        }
    }

    @Override
    public String toString() {
        return "DataController{" +
                "tableInstanceMap=" + tableInstanceMap +
                '}';
    }
}
