package pl.poznan.put.sqldatagenerator.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.writers.CSVTableWriter;
import pl.poznan.put.sqldatagenerator.writers.TableWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing single table in database
 */
public class BaseTable {
    private static final Logger logger = LoggerFactory.getLogger(BaseTable.class);

    private final String name;
    private long dataCount;
    private final long dataCountLimit;
    private TableWriter tableWriter;
    private final List<String> attributesNames;

    private final List<TableInstance> instanceList;

    public BaseTable(String name, List<String> attributesNames, long dataCountLimit, Class<? extends TableWriter> writerClass) {
        this.name = name;
        this.dataCountLimit = dataCountLimit;
        this.instanceList = new ArrayList<>();
        this.attributesNames = attributesNames;
        try {
            tableWriter = writerClass.getConstructor(BaseTable.class).newInstance(this);
        } catch (Exception e) {
            logger.warn("Error while creating " + writerClass + " instance. Using default writer");
            this.tableWriter = new CSVTableWriter(this);
        }
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
