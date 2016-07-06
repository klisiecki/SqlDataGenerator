package pl.poznan.put.sqldatagenerator.generator.datatypes;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import pl.poznan.put.sqldatagenerator.exception.InvalidInternalStateException;
import pl.poznan.put.sqldatagenerator.exception.SQLNotCompatibleWithDatabaseException;
import pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataTypesConverter {
    public static Long getInternalLong(Expression expression, DatabaseType databaseType) {
        try {
            switch (databaseType.getType()) {
                case DATETIME:
                    if (expression instanceof StringValue) {
                        return getLongFromDatetime(((StringValue) expression).getValue());
                    }
                    throw new SQLNotCompatibleWithDatabaseException("Expression " + expression + " is not StringValue");
                case INTEGER:
                    return SQLExpressionsUtils.getLong(expression);
                default:
                    throw new InvalidInternalStateException("Invalid conversion request");
            }
        } catch (ParseException e) {
            throw new SQLNotCompatibleWithDatabaseException("Can`t convert " + expression.toString() + " to Long");
        }
    }

    public static Double getInternalDouble(Expression expression, DatabaseType databaseType) {
        if (databaseType.getType() == DatabaseType.Type.FLOAT) {
            return SQLExpressionsUtils.getDouble(expression);
        }
        throw new InvalidInternalStateException("Invalid conversion request");
    }

    public static String getInternalString(Expression expression, DatabaseType databaseType) {
        if (databaseType.getType() == DatabaseType.Type.VARCHAR) {
            return SQLExpressionsUtils.getString(expression);
        }
        throw new InvalidInternalStateException("Invalid conversion request");
    }

    public static String getDatabaseValue(String input, InternalType internalType, DatabaseType databaseType) {
        if (input == null) {
            return null;
        }
        if (internalType == InternalType.LONG && databaseType.getType() == DatabaseType.Type.DATETIME) {
            return new SimpleDateFormat("dd-MMM-yy hh.mm.ss", Locale.ENGLISH).format(new Date(Long.valueOf(input)));
        }
        //TODO implement all
        return input;
    }

    private static Long getLongFromDatetime(String input) throws ParseException {
        Date date = new SimpleDateFormat("yyyy/MM/dd HH.mm.ss").parse(input);
        return date.getTime();
    }
}
