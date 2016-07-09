package pl.poznan.put.sqldatagenerator.readers;

import java.util.List;

public interface DatabaseSchemaReader {
    Integer getMaxRowsNum();

    List<String> getTables();

    List<String> getAttributes(String table);

    Integer getRowsNum(String table);

//    Integer getMinRowSize(String table);
//
//    String getDistribution(String table);

    String getType(String table, String attribute);

    boolean isPrimaryKey(String table, String attribute);

//    Float getNullPercentage(String table, String attribute);

    String getMinValue(String table, String attribute);

    String getMaxValue(String table, String attribute);

    List<String> getValues(String table, String attribute);

//    Float getMinUniquePercentage(String table, String attribute);
//
//    Float getMaxUniquePercentage(String table, String attribute);
}
