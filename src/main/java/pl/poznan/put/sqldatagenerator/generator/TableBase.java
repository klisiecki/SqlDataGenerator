package pl.poznan.put.sqldatagenerator.generator;

import com.opencsv.CSVWriter;
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing single table in database
 */
public class TableBase {
    private final Configuration configuration = Configuration.getInstance();

    private final int rowsPerFile = configuration.getIntegerProperty(ConfigurationKeys.MAX_ROWS_PER_FILE, 100000);

    private final String name;
    private long dataCount;
    private final long dataCountLimit;
    private CSVWriter writer;
    private int fileNum;
    private final List<String> attributesNames;

    private final List<TableInstance> instanceList;

    public TableBase(String name, List<String> attributesNames, long dataCountLimit) {
        this.name = name;
        this.dataCountLimit = dataCountLimit;
        this.dataCount = 0;
        this.fileNum = 0;
        this.instanceList = new ArrayList<>();
        this.attributesNames = attributesNames;
    }

    public String getName() {
        return name;
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

    private void initFile() {
        String path = configuration.getOutputPath();
        if (writer != null) {
            closeTableFile();
        }
        try {
            writer = new CSVWriter(new FileWriter(path + "/" + name + "_" + fileNum + ".csv"), ';');
            writeList(attributesNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeList(List<String> list) {
        writer.writeNext(list.stream().toArray(String[]::new), false);
    }

    public void closeTableFile() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveInstance(List<String> values) {
        if (dataCount % rowsPerFile == 0) {
            initFile();
            fileNum++;
        }
        writeList(values);
        dataCount++;
    }
}
