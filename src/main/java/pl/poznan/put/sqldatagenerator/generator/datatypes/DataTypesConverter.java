package pl.poznan.put.sqldatagenerator.generator.datatypes;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.commons.lang.time.DateUtils;
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.exception.InvalidInternalStateException;
import pl.poznan.put.sqldatagenerator.exception.SQLNotCompatibleWithDatabaseException;
import pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys.*;

public class DataTypesConverter {

    private static final Configuration configuration = Configuration.getInstance();

    private static final String inputDateFormat = configuration.getStringProperty(INPUT_DATE_FORMAT, "yyyy-MM-dd HH:mm:ss");
    private static final String outputDateFormat = configuration.getStringProperty(OUTPUT_DATE_FORMAT, "yyyy-MM-dd HH:mm:ss");
    private static final String nullValue = configuration.getStringProperty(DATABASE_NULL_VALUE, "NULL");

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
                    throw new InvalidInternalStateException("Invalid conversion request (" + databaseType + " to Long");
            }
        } catch (ParseException e) {
            throw new SQLNotCompatibleWithDatabaseException("Can`t convert " + expression.toString() + " to Long");
        }
    }

    public static Double getInternalDouble(Expression expression, DatabaseType databaseType) {
        if (databaseType.getType() == DatabaseType.Type.FLOAT) {
            return SQLExpressionsUtils.getDouble(expression);
        }
        throw new InvalidInternalStateException("Invalid conversion request (" + databaseType + " to Double");
    }

    public static String getInternalString(Expression expression, DatabaseType databaseType) {
        if (databaseType.getType() == DatabaseType.Type.VARCHAR) {
            return SQLExpressionsUtils.getString(expression);
        }
        throw new InvalidInternalStateException("Invalid conversion request (" + databaseType + " to String");
    }

    public static String getDatabaseValue(String input, InternalType internalType, DatabaseType databaseType) {
        if (input == null) {
            return nullValue;
        }
        if (internalType == InternalType.LONG && databaseType.getType() == DatabaseType.Type.DATETIME) {
            return getDatetimeFromLong(input);
        }
        return input;
    }

    private static String getDatetimeFromLong(String input) {
        Date date = new Date(Long.valueOf(input));
        return new SimpleDateFormat(outputDateFormat, Locale.ENGLISH).format(date);
    }

    public static Long getLongFromDatetime(String input) throws ParseException {
        Date date = new SimpleDateFormat(inputDateFormat).parse(input);
        return date.getTime();
    }
}
