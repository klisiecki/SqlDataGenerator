package pl.poznan.put.sqldatagenerator.generator;

import java.util.HashMap;
import java.util.Map;

public class TablesState {
    private final Map<String, TableInstanceState> instancesMap = new HashMap<>();

    public void add(String tableAlias, TableInstanceState state) {
        instancesMap.put(tableAlias, state);
    }

    public TableInstanceState get(String tableAlias) {
        return instancesMap.get(tableAlias);
    }
}
