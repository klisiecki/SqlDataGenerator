package pl.poznan.put.SqlDataGenerator.generator;

import java.util.HashMap;
import java.util.Map;

public class DataTable {
    private String name;
    private int dataCount;
    private int maxDataCount;
    private Map<String, Attribute> attributeMap;

    public DataTable(String name, int maxDataCount) {
        this.name = name;
        this.maxDataCount = maxDataCount;
        this.dataCount = 0;
        this.attributeMap = new HashMap<>();
    }

    public Attribute getAttribute(String name) {
        return attributeMap.get(name);
    }

    public void addAttribute(Attribute attribute) {
        attributeMap.put(attribute.getName(), attribute);
    }

    public int getFill() {
        return dataCount * 100 / maxDataCount;
    }

    public void clear() {
        for (Map.Entry<String, Attribute> e: attributeMap.entrySet()) {
            e.getValue().clear();
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "DataTable{" +
                "name='" + name + '\'' +
                ", dataCount=" + dataCount +
                ", maxDataCount=" + maxDataCount +
                ", attributes=" + attributeMap +
                '}';
    }
}
