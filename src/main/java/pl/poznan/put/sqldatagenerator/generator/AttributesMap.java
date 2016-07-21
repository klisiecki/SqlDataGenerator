package pl.poznan.put.sqldatagenerator.generator;

import net.sf.jsqlparser.schema.Column;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class AttributesMap {
    private static final Map<String, Attribute> map = new HashMap<>();

    public static void add(TableInstance tableInstance, String attributeName, Attribute attribute) {
        map.put(getKey(tableInstance.getAliasName(), attributeName), attribute);
    }

    private static Attribute get(String aliasName, String attributeName) {
        return map.get(getKey(aliasName, attributeName));
    }

    public static Attribute get(Column column) {
        return get(column.getTable().getName(), column.getColumnName());
    }

    public static List<Attribute> get(BaseTable baseTable, String attributeName) {
        return baseTable.getInstances().stream()
                .map(tableInstance -> get(tableInstance.getAliasName(), attributeName)).collect(toList());
    }

    private static String getKey(String aliasName, String attributeName) {
        return aliasName + "." + attributeName;
    }
}
