package pl.poznan.put.sqldatagenerator.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class AttributesMap {
    private static Map<String, Attribute> map = new HashMap<>();

    public static void add(TableInstance tableInstance, String attributeName, Attribute attribute) {
        map.put(getKey(tableInstance.getAliasName(), attributeName), attribute);
    }

    public static Attribute get(String aliasName, String attributeName) {
        return map.get(getKey(aliasName, attributeName));
    }

    public static List<Attribute> get(TableBase tableBase, String attributeName) {
        return tableBase.getInstances().stream()
                .map(tableInstance -> get(tableInstance.getAliasName(), attributeName)).collect(toList());
    }

    private static String getKey(String aliasName, String attributeName) {
        return aliasName + "." + attributeName;
    }
}
