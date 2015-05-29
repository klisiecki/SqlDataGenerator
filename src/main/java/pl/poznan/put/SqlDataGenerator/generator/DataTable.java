package pl.poznan.put.SqlDataGenerator.generator;

import java.util.ArrayList;
import java.util.List;

public class DataTable {
    private String name;
    private int dataCount;
    private int maxDataCount;
    private List<Attribute> attributes;

    public DataTable(String name, int maxDataCount) {
        this.name = name;
        this.maxDataCount = maxDataCount;
        this.dataCount = 0;
        this.attributes = new ArrayList<>();
    }

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public int getFill() {
        return dataCount * 100 / maxDataCount;
    }

    public void clear() {
        for (Attribute a: attributes) {
            a.clear();
        }
    }

    @Override
    public String toString() {
        return "DataTable{" +
                "name='" + name + '\'' +
                ", dataCount=" + dataCount +
                ", maxDataCount=" + maxDataCount +
                ", attributes=" + attributes +
                '}';
    }
}
