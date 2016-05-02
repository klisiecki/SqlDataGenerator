package pl.poznan.put.sqldatagenerator.generator;

import java.util.HashMap;
import java.util.Map;

public class AttributesMap {
    private static Map<String, Attribute> map = new HashMap<>();

    public static void add(String aliasName, String attributeName, Attribute attribute) {
        map.put(getKey(aliasName, attributeName), attribute);
    }

    public static Attribute get(String aliasName, String attributeName) {
        return map.get(getKey(aliasName, attributeName));
    }

    private static String getKey(String aliasName, String attributeName) {
        return aliasName + "." + attributeName;
    }
}
