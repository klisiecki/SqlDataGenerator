package pl.poznan.put.sqldatagenerator.readers;

public interface DatabaseTypesReader {
    String getBaseType(String type);

    Double getMinValue(String type);

    Double getMaxValue(String type);
}