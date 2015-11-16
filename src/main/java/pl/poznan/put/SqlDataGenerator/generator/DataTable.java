package pl.poznan.put.SqlDataGenerator.generator;

import com.opencsv.CSVWriter;
import pl.poznan.put.SqlDataGenerator.Configuration;
import pl.poznan.put.SqlDataGenerator.restriction.StringRestriction;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTable {
    private Configuration configuration = Configuration.getInstance();

    private final String name;
    private final String originalName;
    private int fileNum;
    private long dataCount;
    private final long dataCountLimit;
    private int resetFactor;
    private final Map<String, Attribute> attributeMap;
    private CSVWriter writer;
    private Attribute primaryKey;

    public DataTable(String name, String originalName, long dataCountLimit) {
        this.name = name;
        this.originalName = originalName;
        this.dataCountLimit = dataCountLimit;
        this.dataCount = 0;
        this.fileNum = 1;
        this.attributeMap = new HashMap<>();
        initTableFile();
    }

    public Attribute getPrimaryKey() {
        return primaryKey;
    }

    private void initTableFile() {
        String path = configuration.getInstanceName();
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writer = new CSVWriter(new FileWriter(path + "/" + originalName + "_" + fileNum + ".csv"), ';');
            List<String> attributes = new ArrayList<>();
            for (Map.Entry<String, Attribute> e2 : attributeMap.entrySet()) {
                Attribute attribute = e2.getValue();
                attributes.add(attribute.getName());
            }
            writer.writeNext(attributes.toArray(new String[attributes.size()]), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeTableFile() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Attribute getAttribute(String name) {
        return attributeMap.get(name);
    }

    public void addAttribute(Attribute attribute) {
        attributeMap.put(attribute.getName(), attribute);
        if (attribute.isPrimaryKey()) {
            this.primaryKey = attribute;
        }
    }

    public int getFill() {
        return (int) (dataCount * 100 / dataCountLimit);
    }

    public void clear() {
        dataCount++;
        for (Map.Entry<String, Attribute> e : attributeMap.entrySet()) {
            e.getValue().setClear(true);
        }
    }

    public void calculateResetFactor(long maxDataRows) {
        this.resetFactor = (int) (100 * maxDataRows / dataCountLimit);
    }

    public boolean checkIteration(long iteration) {
        return iteration == 0 || (iteration * 100 / resetFactor != (iteration - 1) * 100 / resetFactor);
    }

    public String getName() {
        return name;
    }

    public Map<String, Attribute> getAttributeMap() {
        return attributeMap;
    }

    public void save() {
        if (dataCount % configuration.getRowsPerFile() == 0) {
            fileNum++;
            initTableFile();
        }
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, Attribute> e2 : attributeMap.entrySet()) {
            Attribute attribute = e2.getValue();
            values.add(attribute.getObjectValue().toString());
        }
        writer.writeNext(values.toArray(new String[values.size()]), false);
    }

    @Override
    public String toString() {
        return "DataTable{" +
                "name='" + name + '\'' +
                ", dataCount=" + dataCount +
                ", dataCountLimit=" + dataCountLimit +
                ", attributes=" + attributeMap +
                '}';
    }
}
