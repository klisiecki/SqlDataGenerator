package pl.poznan.put.sqldatagenerator.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableInstance {
    private final TableBase base;
    private final String aliasName;
    private final Map<String, Attribute> attributeMap;

    public TableInstance(TableBase base, String aliasName) {
        this.base = base;
        this.aliasName = aliasName;
        this.attributeMap = new HashMap<>();

        base.addInstance(this);
    }

    public void addAttribute(Attribute attribute) {
        attributeMap.put(attribute.getName(), attribute);
    }

    public List<String> getValues(List<String> names) {
        List<String> values = new ArrayList<>();
        for (String name : names) {
            values.add(attributeMap.get(name).getObjectValue().toString());
        }
        return values;
    }
}
