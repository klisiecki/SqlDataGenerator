package pl.poznan.put.sqldatagenerator.generator;

import pl.poznan.put.sqldatagenerator.writers.TableWriter;
import pl.poznan.put.sqldatagenerator.writers.TableWriterFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing single table in database
 */
public class BaseTable {
    private final String name;
    private final List<String> attributesNames;
    private final List<TableInstance> instanceList;
    private final long dataCountLimit;
    private final TableWriter tableWriter;

    private long dataCount;

    public BaseTable(String name, List<String> attributesNames, long dataCountLimit, Class<? extends TableWriter> writerClass) {
        this.name = name;
        this.dataCountLimit = dataCountLimit;
        this.instanceList = new ArrayList<>();
        this.attributesNames = attributesNames;
        this.tableWriter = TableWriterFactory.createTableWriter(writerClass, this);
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

    public void removeAttributeName(String name) {
        attributesNames.remove(name);
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
