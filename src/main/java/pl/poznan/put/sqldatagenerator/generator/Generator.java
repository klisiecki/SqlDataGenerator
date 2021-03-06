package pl.poznan.put.sqldatagenerator.generator;

import net.sf.jsqlparser.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.exception.SQLNotCompatibleWithDatabaseException;
import pl.poznan.put.sqldatagenerator.exception.SQLSyntaxNotSupportedException;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DatabaseType;
import pl.poznan.put.sqldatagenerator.history.HistoryManager;
import pl.poznan.put.sqldatagenerator.readers.DatabaseProperties;
import pl.poznan.put.sqldatagenerator.readers.SQLData;
import pl.poznan.put.sqldatagenerator.restriction.RestrictionsManager;
import pl.poznan.put.sqldatagenerator.solver.Solver;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;
import pl.poznan.put.sqldatagenerator.writers.TableWriter;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;
import static pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys.ONLY_QUERY_ATTRIBUTES;
import static pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys.PRINT_PROGRESS_DELAY;

public class Generator {
    private static final Configuration configuration = Configuration.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(Generator.class);
    private static final int progressDelay = configuration.getIntegerProperty(PRINT_PROGRESS_DELAY, 100);
    private static final boolean onlyQueryAttributes = configuration.getBooleanProperty(ONLY_QUERY_ATTRIBUTES, false);

    private final Class<? extends TableWriter> writerClass;
    private final Map<String, BaseTable> tableBaseMap;
    private final Map<String, TableInstance> tableInstanceMap;
    private final Random random;
    private final RestrictionsManager restrictionsManager;
    private final HistoryManager historyManager;
    private long maxDataRows = 0;

    public Generator(Class<? extends TableWriter> writerClass) {
        this.tableBaseMap = new HashMap<>();
        this.tableInstanceMap = new HashMap<>();
        this.random = new Random();
        this.restrictionsManager = new RestrictionsManager();
        this.historyManager = new HistoryManager();
        this.writerClass = writerClass;
    }

    public void generate(DatabaseProperties databaseProperties, SQLData sqlData) {
        initTables(databaseProperties, sqlData);
        logger.info("Generating process started");
        int positiveRows = (int) (configuration.getSelectivity() * maxDataRows);
        Instant lastTimestamp = now();

        for (long iteration = 0; iteration < maxDataRows; iteration++) {
            float progress = (float) iteration / maxDataRows;
            lastTimestamp = printProgress(lastTimestamp, progress);

            boolean positive = iteration < positiveRows;
            int restrictionsIndex = random.nextInt(restrictionsManager.getListSize(positive));
            prepareTables(restrictionsIndex, positive, progress);
            generateRow(restrictionsIndex, positive);
            saveTables(restrictionsIndex, positive);
        }
        tableBaseMap.values().forEach(BaseTable::closeWriter);
        logger.info("Generating process done");
    }

    private void initTables(DatabaseProperties databaseProperties, SQLData sqlData) {
        initBaseTable(databaseProperties);

        for (Table table : sqlData.getTables()) {
            String tableName = table.getName();
            String aliasName = table.getAlias().getName();
            if (!tableBaseMap.containsKey(tableName)) {
                throw new SQLNotCompatibleWithDatabaseException(
                        "Table " + tableName + " not found in database properties");
            }

            TableInstance tableInstance = new TableInstance(tableBaseMap.get(tableName), aliasName);
            initAttributes(databaseProperties, sqlData, table, tableInstance);
            tableInstanceMap.put(aliasName, tableInstance);
        }
        connectKeys(databaseProperties, sqlData);
        maxDataRows = databaseProperties.getMaxRowsNum();

        restrictionsManager.initialize(sqlData.getCriteria(), databaseProperties.getConstraints(tableBaseMap));

        List<String> tablesAliasNames = tableInstanceMap.values().stream()
                                                        .map(TableInstance::getAliasName)
                                                        .collect(toList());
        historyManager.initialize(tablesAliasNames, restrictionsManager, sqlData.getJoinEquals());
    }

    private Instant printProgress(Instant lastTimestamp, float progress) {
        if (Duration.between(lastTimestamp, now()).toMillis() > progressDelay) {
            lastTimestamp = now();
            logger.info("Generation progress: {}%", (int) (progress * 100));
        }
        return lastTimestamp;
    }

    private void prepareTables(int restrictionsIndex, boolean positive, float progress) {
        List<String> notNeededTableAliasList = tableInstanceMap.values().stream()
                                                               .filter(table -> table.shouldBeGenerated(progress))
                                                               .map(TableInstance::getAliasName)
                                                               .collect(toList());

        TablesState historyTablesState = historyManager.getState(positive, restrictionsIndex, notNeededTableAliasList);

        if (historyTablesState != null) {
            tableInstanceMap.values().stream()
                            .filter(table -> !table.shouldBeGenerated(progress))
                            .forEach(tableInstance -> {
                                TableInstanceState historyState = historyTablesState.get(tableInstance.getAliasName());
                                if (historyState != null) {
                                    tableInstance.setState(historyState);
                                }
                            });
        }
    }

    private void generateRow(int restrictionsIndex, boolean positive) {
        new Solver(restrictionsManager.get(positive, restrictionsIndex).clone()).solve();
    }

    private void saveTables(int restrictionsIndex, boolean positive) {
        tableInstanceMap.values().stream()
                        .filter(table -> !table.getState().isSaved())
                        .forEach(TableInstance::save);

        TablesState tablesState = new TablesState();
        tableInstanceMap.values().forEach(tableInstance ->
                tablesState.add(tableInstance.getAliasName(), tableInstance.getState()));

        historyManager.addState(positive, restrictionsIndex, tablesState);
        tableInstanceMap.values().forEach(TableInstance::clear);
    }

    private void initBaseTable(DatabaseProperties databaseProperties) {
        for (String tableName : databaseProperties.getTables()) {
            long count = databaseProperties.getRowsNum(tableName);
            BaseTable baseTable =
                    new BaseTable(tableName, databaseProperties.getAttributes(tableName), count, writerClass);
            tableBaseMap.put(tableName, baseTable);
        }
    }

    private void initAttributes(DatabaseProperties databaseProperties, SQLData sqlData, Table table,
                                TableInstance tableInstance) {
        String tableName = table.getName();
        List<String> databaseAttributes = databaseProperties.getAttributes(tableName);
        List<String> sqlAttributes = sqlData.getAttributes(table);

        List<String> missingAttributes = new ArrayList<>(sqlAttributes);
        missingAttributes.removeAll(databaseAttributes);
        if (!missingAttributes.isEmpty()) {
            throw new SQLNotCompatibleWithDatabaseException("Attributes " + Arrays.toString(missingAttributes.toArray())
                    + " not found in " + tableName + " definition");
        }

        for (String attributeName : databaseAttributes) {
            if (shouldBeRemoved(attributeName, tableName, databaseProperties, sqlAttributes)) {
                tableBaseMap.get(tableName).removeAttributeName(attributeName);
                continue;
            }
            DatabaseType databaseType = databaseProperties.getType(tableName, attributeName);
            Attribute attribute = new Attribute(tableInstance, attributeName, databaseType);
            tableInstance.addAttribute(attribute);
            AttributesMap.add(tableInstance, attributeName, attribute);
        }
    }

    private boolean shouldBeRemoved(String attributeName, String tableName, DatabaseProperties databaseProperties,
                                    List<String> sqlAttributes) {
        return onlyQueryAttributes && !sqlAttributes.contains(attributeName)
                && !databaseProperties.isPrimaryKey(tableName, attributeName);
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
                throw new SQLSyntaxNotSupportedException("Join on two primary keys or not keys");
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
