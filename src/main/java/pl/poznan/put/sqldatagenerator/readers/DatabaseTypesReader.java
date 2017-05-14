package pl.poznan.put.sqldatagenerator.readers;

public interface DatabaseTypesReader {
    String getBaseType(String type);

    String getMinValue(String type);

    String getMaxValue(String type);
}