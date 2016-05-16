package pl.poznan.put.sqldatagenerator.generator;

import java.util.HashMap;
import java.util.Map;

public class TableInstanceState {
    private final Map<String, String> attributeValues = new HashMap<>();

    public void setValue(String attributeName, String value) {
        if (attributeValues.containsKey(attributeName)) {
            throw new RuntimeException("Already set");
        }
        attributeValues.put(attributeName, value);
    }

    public String getValue(String attributeName) {
        return attributeValues.get(attributeName);
    }
}
