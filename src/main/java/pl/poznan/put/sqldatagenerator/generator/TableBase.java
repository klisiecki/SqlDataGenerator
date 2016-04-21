package pl.poznan.put.sqldatagenerator.generator;

import com.opencsv.CSVWriter;
import pl.poznan.put.sqldatagenerator.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TableBase {
    private final Configuration configuration = Configuration.getInstance();

    private final String name;
    private OldAttribute primaryKey;
    private long dataCount;
    private long dataCountLimit;
    private int resetFactor;
    private CSVWriter writer;
    private int fileNum;
    private List<String> attributesNames;

    private List<TableInstance> instanceList;

    public TableBase(String name, long dataCountLimit) {
        this.name = name;
        this.dataCountLimit = dataCountLimit;
        this.dataCount = 0;
        this.fileNum = 1;

        instanceList = new ArrayList<>();
    }

    public void addInstance(TableInstance instance) {
        instanceList.add(instance);
    }

    public void calculateResetFactor(long maxDataRows) {
        this.resetFactor = (int) (100 * maxDataRows / (dataCountLimit / instanceList.size()) );
    }

    public boolean shouldBeGenerated(long iteration) {
        return iteration == 0 ||    (iteration * 100 / resetFactor !=
                (iteration - 1) * 100 / resetFactor);
    }

    public OldAttribute getPrimaryKey() {
        return primaryKey;
    }

    public void initFile() {
        String path = configuration.getInstanceName();
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

    public void saveAll() {
        for (TableInstance instance : instanceList) {
            saveInstance(instance);
        }
    }

    private void saveInstance(TableInstance table) {
        if (dataCount % configuration.getRowsPerFile() == 0) {
            fileNum++;
            initFile();
        }
        writeList(table.getValues(attributesNames));
    }
}
