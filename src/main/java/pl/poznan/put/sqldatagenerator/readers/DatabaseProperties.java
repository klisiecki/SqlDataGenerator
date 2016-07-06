package pl.poznan.put.sqldatagenerator.readers;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;
import pl.poznan.put.sqldatagenerator.generator.TableBase;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DatabaseType;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;
import pl.poznan.put.sqldatagenerator.generator.key.KeyGenerator;
import pl.poznan.put.sqldatagenerator.generator.key.SimpleKeyGenerator;
import pl.poznan.put.sqldatagenerator.restriction.Restrictions;
import pl.poznan.put.sqldatagenerator.restriction.types.PrimaryKeyRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.restriction.types.StringRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseProperties {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseProperties.class);

    private final DatabasePropertiesReader databasePropertiesReader;

    public DatabaseProperties(DatabasePropertiesReader databasePropertiesReader) {
        this.databasePropertiesReader = databasePropertiesReader;
    }

    public Restrictions getConstraints(Map<String, TableBase> tableBaseMap) {
        List<Restriction> restrictionList = new ArrayList<>();
        for (String tableName : databasePropertiesReader.getTables()) {
            for (String attributeName : databasePropertiesReader.getAttributes(tableName)) {
                List<String> values = databasePropertiesReader.getValues(tableName, attributeName);
                List<Attribute> attributes = AttributesMap.get(tableBaseMap.get(tableName), attributeName);
                InternalType internalType = databasePropertiesReader.getDatabaseType(tableName, attributeName).getInternalType();
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

    private List<Restriction> getIntegerConstraints(String tableName, String attributeName, List<String> values, List<Attribute> attributes) {
        List<Restriction> restrictionList = new ArrayList<>();
        RangeSet<Long> rangeSet;
        if (databasePropertiesReader.isPrimaryKey(tableName, attributeName)) {
            KeyGenerator keyGenerator = new SimpleKeyGenerator(databasePropertiesReader.getRowsNum(tableName));
            attributes.forEach(attribute -> restrictionList.add(new PrimaryKeyRestriction(attribute, keyGenerator)));

        } else if (values == null) {
            rangeSet = getIntegerRangeSet(tableName, attributeName);
            attributes.forEach(attribute -> restrictionList.add(new RangeRestriction(attribute, rangeSet)));
        } else {
            rangeSet = TreeRangeSet.create();
            for (String valueString : values) {
                Long value = Long.parseLong(valueString);
                rangeSet.add(Range.closed(value, value));
            }
            attributes.forEach(attribute -> restrictionList.add(new RangeRestriction(attribute, rangeSet)));
        }
        return restrictionList;
    }

    private RangeSet<Long> getIntegerRangeSet(String table, String attribute) {
        RangeSet<Long> rangeSet = TreeRangeSet.create();
        Range<Long> range = Range.all();
        String minValue = databasePropertiesReader.getMinValue(table, attribute);
        String maxValue = databasePropertiesReader.getMaxValue(table, attribute);
        if (minValue != null) {
            range = range.intersection(Range.downTo(Long.valueOf(minValue), BoundType.CLOSED));
        }
        if (maxValue != null) {
            range = range.intersection(Range.upTo(Long.valueOf(maxValue), BoundType.CLOSED));
        }
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
        Range<Double> range = Range.all();
        String minValue = databasePropertiesReader.getMinValue(table, attribute);
        String maxValue = databasePropertiesReader.getMaxValue(table, attribute);
        if (minValue != null) {
            range = range.intersection(Range.downTo(Double.valueOf(minValue), BoundType.CLOSED));
        }
        if (maxValue != null) {
            range = range.intersection(Range.upTo(Double.valueOf(maxValue), BoundType.CLOSED));
        }
        rangeSet.add(range);
        return rangeSet;
    }

    private List<Restriction> getStringConstraints(String tableName, String attributeName, List<String> values, List<Attribute> attributes) {
        List<Restriction> restrictionList = new ArrayList<>();
        for (Attribute attribute : attributes) {
            StringRestriction stringRestriction = new StringRestriction(attribute);
            stringRestriction.setAllowedValues(values);
            if (databasePropertiesReader.getMinValue(tableName, attributeName) != null) {
                stringRestriction.setMinLength(Integer.parseInt(databasePropertiesReader.getMinValue(tableName, attributeName)));
            }
            if (databasePropertiesReader.getMaxValue(tableName, attributeName) != null) {
                stringRestriction.setMaxLength(Integer.parseInt(databasePropertiesReader.getMaxValue(tableName, attributeName)));
            }
            restrictionList.add(stringRestriction);
        }
        return restrictionList;
    }

    public long getMaxRowsNum() {
        return databasePropertiesReader.getMaxRowsNum();
    }

    public int getT() {
        return databasePropertiesReader.getT();
    }

    public int getM() {
        return databasePropertiesReader.getM();
    }

    public List<String> getTables() {
        return databasePropertiesReader.getTables();
    }

    public long getRowsNum(String tableName) {
        return databasePropertiesReader.getRowsNum(tableName);
    }

    public List<String> getAttributes(String tableName) {
        return databasePropertiesReader.getAttributes(tableName);
    }

    public DatabaseType getType(String tableName, String attributeName) {
        return databasePropertiesReader.getDatabaseType(tableName, attributeName);
    }

    public boolean isPrimaryKey(String tableName, String attributeName) {
        return databasePropertiesReader.isPrimaryKey(tableName, attributeName);
    }
}
