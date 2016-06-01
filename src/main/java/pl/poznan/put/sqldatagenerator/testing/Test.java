package pl.poznan.put.sqldatagenerator.testing;

import pl.poznan.put.sqldatagenerator.generator.datatypes.DataTypesConverter;

import java.text.ParseException;

public class Test {

    public static void main(String[] args) throws ParseException {
        Long timestamp = DataTypesConverter.getLongFromDatetime("15-02-2014 00:00:00");
        System.out.println("timestamp = " + timestamp);
    }
}
