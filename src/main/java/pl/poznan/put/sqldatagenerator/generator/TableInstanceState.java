package pl.poznan.put.sqldatagenerator.generator;

import pl.poznan.put.sqldatagenerator.exception.InvalidInfernalStateException;

import java.util.HashMap;
import java.util.Map;

public class TableInstanceState {
    private final Map<String, String> attributeValues;
    private boolean isSaved;

    public TableInstanceState() {
        this.attributeValues = new HashMap<>();
        this.isSaved = false;
    }

    public String getValue(String attributeName) {
        return attributeValues.get(attributeName);
    }

    public void setValue(String attributeName, String value) {
        if (attributeValues.containsKey(attributeName)) {
            throw new InvalidInfernalStateException("Already set");
        }
        attributeValues.put(attributeName, value);
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}
