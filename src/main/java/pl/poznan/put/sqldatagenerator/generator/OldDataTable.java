package pl.poznan.put.sqldatagenerator.generator;

import com.opencsv.CSVWriter;
import pl.poznan.put.sqldatagenerator.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class OldDataTable {
    private final Configuration configuration = Configuration.getInstance();

    private final String name;
    private final String originalName;
    private int fileNum;
    private long dataCount;
    private final long dataCountLimit;
    private int resetFactor;
    private final Map<String, OldAttribute> attributeMap;
    private CSVWriter writer;
    private OldAttribute primaryKey;

    public OldDataTable(String name, String originalName, long dataCountLimit) {
        this.name = name;
        this.originalName = originalName;
        this.dataCountLimit = dataCountLimit;
        this.dataCount = 0;
        this.fileNum = 1;
        this.attributeMap = new HashMap<>();
    }

    public OldAttribute getPrimaryKey() {
        return primaryKey;
    }

    public void initTableFile() {
        String path = configuration.getInstanceName();
        if (writer != null) {
            closeTableFile();
        }
        try {
            writer = new CSVWriter(new FileWriter(path + "/" + originalName + "_" + fileNum + ".csv"), ';');
            List<String> attributes = new ArrayList<>();
            for (Map.Entry<String, OldAttribute> e2 : attributeMap.entrySet()) {
                OldAttribute oldAttribute = e2.getValue();
                attributes.add(oldAttribute.getName());
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

    public OldAttribute getAttribute(String name) {
        return attributeMap.get(name);
    }

    public void addAttribute(OldAttribute oldAttribute) {
        attributeMap.put(oldAttribute.getName(), oldAttribute);
        if (oldAttribute.isPrimaryKey()) {
            this.primaryKey = oldAttribute;
        }
    }

    public int getFill() {
        return (int) (dataCount * 100 / dataCountLimit);
    }

    public void clear() {
        dataCount++;
        for (Map.Entry<String, OldAttribute> e : attributeMap.entrySet()) {
            e.getValue().setClear(true);
        }
    }

    public void calculateResetFactor(long maxDataRows) {
        this.resetFactor = (int) (100 * maxDataRows / dataCountLimit);
    }

    public boolean shouldBeGenerated(long iteration) {
        return iteration == 0 ||    (iteration * 100 / resetFactor !=
                                    (iteration - 1) * 100 / resetFactor);
    }

    public String getName() {
        return name;
    }

    public Map<String, OldAttribute> getAttributeMap() {
        return attributeMap;
    }

    public void save() {
        if (dataCount % configuration.getRowsPerFile() == 0) {
            fileNum++;
            initTableFile();
        }
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, OldAttribute> e2 : attributeMap.entrySet()) {
            OldAttribute oldAttribute = e2.getValue();
            values.add(oldAttribute.getObjectValue().toString());
        }
        writer.writeNext(values.toArray(new String[values.size()]), false);
    }

    @Override
    public String toString() {
        return "OldDataTable{" +
                "name='" + name + '\'' +
                ", dataCount=" + dataCount +
                ", dataCountLimit=" + dataCountLimit +
                ", attributes=" + attributeMap +
                '}';
    }
}
