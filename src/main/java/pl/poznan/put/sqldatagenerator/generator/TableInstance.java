package pl.poznan.put.sqldatagenerator.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class TableInstance {
    private final TableBase base;

    private final String aliasName;
    private final Map<String, AttributeInstance> attributeMap;
    public TableInstance(TableBase base, String aliasName) {
        this.base = base;
        this.aliasName = aliasName;
        this.attributeMap = new HashMap<>();

        base.addInstance(this);
    }

    public void addAttribute(AttributeInstance attribute) {
        attributeMap.put(attribute.getName(), attribute);
    }

    public List<String> getValues(List<String> attributesNames) {
        return attributesNames.stream().map(name -> attributeMap.get(name).getValue()).collect(toList());
    }

    public void clear() {
        attributeMap.entrySet().forEach(e -> e.getValue().clear());
    }

    public TableBase getBase() {
        return base;
    }
}
