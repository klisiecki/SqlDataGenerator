package pl.poznan.put.sqldatagenerator.generator;

import pl.poznan.put.sqldatagenerator.exception.InvalidInfernalStateException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Class representing single occurrence of table in JOIN clause.
 * One table may be used multiple times in JOIN and all of this occurrences refers to the same {@link TableBase} object
 */
public class TableInstance {
    private final TableBase base;

    private final String aliasName;
    private final Map<String, Attribute> attributeMap;
    private TableInstanceState state;

    public TableInstance(TableBase base, String aliasName) {
        this.base = base;
        this.aliasName = aliasName;
        this.attributeMap = new HashMap<>();
        this.state = new TableInstanceState();
        base.addInstance(this);
    }

    public void addAttribute(Attribute attribute) {
        attributeMap.put(attribute.getName(), attribute);
    }

    public TableInstanceState getState() {
        return state;
    }

    public void setState(TableInstanceState state) {
        if (state == null) {
            throw new InvalidInfernalStateException("State cannot be null");
        }
        attributeMap.values().forEach(a -> a.setClear(false));
        this.state = state;
    }

    public void clear() {
        this.state = new TableInstanceState();
        attributeMap.values().forEach(a -> a.setClear(true));
    }

    /**
     * @return list of attribute values in the same order as as in base table
     */
    private List<String> getValues() {
        List<String> attributesNames = base.getAttributesNames();
        if (attributesNames.stream().anyMatch(name -> !attributeMap.containsKey(name))) {
            throw new InvalidInfernalStateException("One of given attributes does not match table attributes");
        }
        return attributesNames.stream().map(name -> attributeMap.get(name).getValue()).collect(toList());
    }

    public TableBase getBase() {
        return base;
    }

    public String getAliasName() {
        return aliasName;
    }

    public boolean shouldBeGenerated(float progress) {
        return base.shouldBeGenerated(progress);
    }

    public void save() {
        base.saveInstance(getValues());
        state.setSaved(true);
    }
}
