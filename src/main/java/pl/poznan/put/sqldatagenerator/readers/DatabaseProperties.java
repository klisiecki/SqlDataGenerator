package pl.poznan.put.sqldatagenerator.readers;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;
import pl.poznan.put.sqldatagenerator.generator.BaseTable;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DatabaseType;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;
import pl.poznan.put.sqldatagenerator.generators.RandomGenerator;
import pl.poznan.put.sqldatagenerator.generators.key.KeyGenerator;
import pl.poznan.put.sqldatagenerator.restriction.Restrictions;
import pl.poznan.put.sqldatagenerator.restriction.types.PrimaryKeyRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.restriction.types.StringRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DatabaseProperties {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseProperties.class);

    private final DatabaseSchemaReader databaseSchemaReader;
    private final DatabaseTypesReader databaseTypesReader;

    public DatabaseProperties(DatabaseSchemaReader databaseSchemaReader, DatabaseTypesReader databaseTypesReader) {
        this.databaseSchemaReader = databaseSchemaReader;
        this.databaseTypesReader = databaseTypesReader;
    }

    public Restrictions getConstraints(Map<String, BaseTable> tableBaseMap) {
        List<Restriction> restrictionList = new ArrayList<>();
        for (String tableName : tableBaseMap.keySet()) {
            for (String attributeName : tableBaseMap.get(tableName).getAttributesNames()) {
                List<String> values = databaseSchemaReader.getValues(tableName, attributeName);
                List<Attribute> attributes = AttributesMap.get(tableBaseMap.get(tableName), attributeName);
                InternalType internalType = getType(tableName, attributeName).getInternalType();
                switch (internalType) {
                    case LONG:
                        restrictionList.addAll(getIntegerConstraints(tableName, attributeName, values, attributes));
                        break;
                    case DOUBLE:
                        restrictionList.addAll(getFloatConstraints(tableName, attributeName, attributes));
                        break;
                    case STRING:
                        restrictionList.addAll(getStringConstraints(tableName, attributeName, values, attributes));
                        break;
                }
            }
        }
        return new Restrictions(restrictionList);
    }

    private List<Restriction> getIntegerConstraints(String tableName, String attributeName, List<String> values,
                                                    List<Attribute> attributes) {
        List<Restriction> restrictionList = new ArrayList<>();
        RangeSet<Long> rangeSet;
        if (databaseSchemaReader.isPrimaryKey(tableName, attributeName)) {
            KeyGenerator keyGenerator = RandomGenerator.getKeyGenerator(databaseSchemaReader.getRowsNum(tableName));
            attributes.forEach(attribute -> restrictionList.add(new PrimaryKeyRestriction(attribute, keyGenerator)));

        } else if (values == null) {
            rangeSet = getIntegerRangeSet(tableName, attributeName);
            attributes.forEach(attribute -> restrictionList.add(new RangeRestriction(attribute, rangeSet)));
        } else {
            rangeSet = TreeRangeSet.create();
            values.stream().map(Long::parseLong).map(v -> Range.closed(v, v)).forEach(rangeSet::add);
            attributes.forEach(attribute -> restrictionList.add(new RangeRestriction(attribute, rangeSet)));
        }
        return restrictionList;
    }

    private RangeSet<Long> getIntegerRangeSet(String table, String attribute) {
        RangeSet<Long> rangeSet = TreeRangeSet.create();

        Long minValue = getMinValue(table, attribute).longValue();
        Long maxValue = getMaxValue(table, attribute).longValue();
        Range<Long> range = Range.closed(minValue, maxValue);

        rangeSet.add(range);
        return rangeSet;
    }

    private List<Restriction> getFloatConstraints(String tableName, String attributeName, List<Attribute> attributes) {
        List<Restriction> restrictionList = new ArrayList<>();
        RangeSet<Double> rangeSet = getFloatRangeSet(tableName, attributeName);
        attributes.forEach(attribute -> restrictionList.add(new RangeRestriction(attribute, rangeSet)));
        return restrictionList;
    }

    private RangeSet<Double> getFloatRangeSet(String table, String attribute) {
        RangeSet<Double> rangeSet = TreeRangeSet.create();

        Double minValue = getMinValue(table, attribute);
        Double maxValue = getMaxValue(table, attribute);
        Range<Double> range = Range.closed(minValue, maxValue);

        rangeSet.add(range);
        return rangeSet;
    }

    private List<Restriction> getStringConstraints(String tableName, String attributeName, List<String> allowedValues,
                                                   List<Attribute> attributes) {
        List<Restriction> restrictionList = new ArrayList<>();
        for (Attribute attribute : attributes) {
            Range<Integer> allowedLength = Range.closed(getMinValue(tableName, attributeName).intValue(),
                    getMaxValue(tableName, attributeName).intValue());

            StringRestriction stringRestriction =
                    new StringRestriction(attribute, allowedLength, null, allowedValues, null);
            restrictionList.add(stringRestriction);
        }
        return restrictionList;
    }

    private Double getMinValue(String tableName, String attributeName) {
        String typeName = getTypeName(tableName, attributeName);
        Double result = databaseTypesReader.getMinValue(typeName);

        String schemaMinValueString = databaseSchemaReader.getMinValue(tableName, attributeName);
        if (schemaMinValueString != null) {
            Double schemaMinValue = Double.valueOf(schemaMinValueString);
            result = max(result, schemaMinValue);
        }
        return result;
    }

    private Double getMaxValue(String tableName, String attributeName) {
        String typeName = getTypeName(tableName, attributeName);
        Double result = databaseTypesReader.getMaxValue(typeName);

        String schemaMaxValueString = databaseSchemaReader.getMaxValue(tableName, attributeName);
        if (schemaMaxValueString != null) {
            Double schemaMaxValue = Double.valueOf(schemaMaxValueString);
            result = min(result, schemaMaxValue);
        }

        Integer precision = getFirstParam(tableName, attributeName);
        if (precision != null) {
            if (!databaseTypesReader.getBaseType(typeName).equals("VARCHAR")) {
                precision = (int) Math.pow(10, precision);
            }
            result = min(result, precision);
        }
        return result;
    }

    public long getMaxRowsNum() {
        return databaseSchemaReader.getMaxRowsNum();
    }

    public List<String> getTables() {
        return databaseSchemaReader.getTables();
    }

    public long getRowsNum(String tableName) {
        return databaseSchemaReader.getRowsNum(tableName);
    }

    public List<String> getAttributes(String tableName) {
        return databaseSchemaReader.getAttributes(tableName);
    }

    public boolean isPrimaryKey(String tableName, String attributeName) {
        return databaseSchemaReader.isPrimaryKey(tableName, attributeName);
    }

    public DatabaseType getType(String tableName, String attributeName) {
        String originalTypeName = getTypeName(tableName, attributeName);
        Integer scale = getSecondParam(tableName, attributeName);
        String typeName = databaseTypesReader.getBaseType(originalTypeName);
        return new DatabaseType(DatabaseType.Type.valueOf(typeName), scale);
    }

    private String getTypeName(String tableName, String attributeName) {
        String type = databaseSchemaReader.getType(tableName, attributeName);
        int parenthesisPos = type.indexOf('(');
        return parenthesisPos < 0 ? type : type.substring(0, parenthesisPos);
    }

    private Integer getFirstParam(String tableName, String attributeName) {
        String type = databaseSchemaReader.getType(tableName, attributeName);
        String[] typeParams = getTypeParams(type);
        if (typeParams != null) {
            return Integer.valueOf(typeParams[0]);
        }
        return null;
    }

    private Integer getSecondParam(String tableName, String attributeName) {
        String type = databaseSchemaReader.getType(tableName, attributeName);
        String[] typeParams = getTypeParams(type);
        if (typeParams != null && typeParams.length > 1) {
            return Integer.valueOf(typeParams[1]);
        }
        return null;
    }

    private String[] getTypeParams(String type) {
        int parenthesisPos = type.indexOf('(');
        if (parenthesisPos > 0) {
            String parameters = type.substring(parenthesisPos + 1, type.length() - 1);
            return parameters.split(",");
        }
        return null;
    }
}
