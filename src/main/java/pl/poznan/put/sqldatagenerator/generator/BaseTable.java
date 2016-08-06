package pl.poznan.put.sqldatagenerator.generator;

import pl.poznan.put.sqldatagenerator.writers.CSVTableWriter;
import pl.poznan.put.sqldatagenerator.writers.TableWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing single table in database
 */
public class BaseTable {

    private final String name;
    private long dataCount;
    private final long dataCountLimit;
    private TableWriter tableWriter;
    private final List<String> attributesNames;

    private final List<TableInstance> instanceList;

    public BaseTable(String name, List<String> attributesNames, long dataCountLimit) {
        this.name = name;
        this.dataCountLimit = dataCountLimit;
        this.instanceList = new ArrayList<>();
        this.attributesNames = attributesNames;
        this.tableWriter = new CSVTableWriter(this);
    }

    public String getName() {
        return name;
    }

    public long getDataCount() {
        return dataCount;
    }

    public void addInstance(TableInstance instance) {
        instanceList.add(instance);
    }

    public List<TableInstance> getInstances() {
        return instanceList;
    }

    public List<String> getAttributesNames() {
        return attributesNames;
    }

    public boolean shouldBeGenerated(float progress) {
        float tableProgress = (float) dataCount / dataCountLimit;
        return tableProgress <= progress;
    }

    public void save(List<String> values) {
        tableWriter.save(values);
        dataCount++;
    }

    public void closeWriter() {
        tableWriter.closeWriter();
    }
}
