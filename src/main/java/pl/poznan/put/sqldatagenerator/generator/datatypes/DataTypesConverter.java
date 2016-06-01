package pl.poznan.put.sqldatagenerator.generator.datatypes;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import pl.poznan.put.sqldatagenerator.exception.InvalidInternalStateException;
import pl.poznan.put.sqldatagenerator.exception.SQLAndXMLNotCompatibleException;
import pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataTypesConverter {
    public static Long getInternalLong(Expression expression, DatabaseType databaseType) {
        try {
            switch (databaseType) {
                case DATETIME:
                    if (expression instanceof StringValue) {
                        return getLongFromDatetime(((StringValue) expression).getValue());
                    }
                    throw new SQLAndXMLNotCompatibleException("Expression " + expression + " is not StringValue");
                case INTEGER:
                    return SQLExpressionsUtils.getLong(expression);
                default:
                    throw new InvalidInternalStateException("Invalid conversion request");
            }
        } catch (ParseException e) {
            throw new SQLAndXMLNotCompatibleException("Can`t convert " + expression.toString() + " to Long");
        }
    }

    public static String getDatabaseType(String input, InternalType internalType, DatabaseType databaseType) {
        if (input == null) return input;
        try {
            if (InternalType.LONG.equals(internalType) && DatabaseType.DATETIME.equals(databaseType)) {
                return new SimpleDateFormat("dd-MMM-yy hh.mm.ss", Locale.ENGLISH).format(new Date(Long.valueOf(input)));
            }
        } catch (NumberFormatException e) {
            e.getStackTrace();
        }
        //TODO implement all
        return input;
    }


    public static Long getLongFromDatetime(String input) throws ParseException {
        Date date = new SimpleDateFormat("yyyy/MM/dd HH.mm.ss").parse(input);
        return date.getTime();
    }
}
