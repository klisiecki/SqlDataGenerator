package pl.poznan.put.sqldatagenerator.readers;

import com.bpodgursky.jbool_expressions.Expression;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.exception.SQLInvalidSyntaxException;
import pl.poznan.put.sqldatagenerator.exception.SQLSyntaxNotSupportedException;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.sql.AttributesNamesFinder;
import pl.poznan.put.sqldatagenerator.sql.JoinEqualsFinder;
import pl.poznan.put.sqldatagenerator.sql.RestrictionFinder;
import pl.poznan.put.sqldatagenerator.sql.TablesFinder;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;
import pl.poznan.put.sqldatagenerator.util.Utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class SQLData {
    private static final Logger logger = LoggerFactory.getLogger(SQLData.class);

    private final Select selectStatement;

    private SQLData(Select selectStatement) {
        this.selectStatement = selectStatement;

        logger.info("Parsed statement: {}", selectStatement);
        logger.info("Tables (name, synonym, columns):");
        for (Table table : getTables()) {
            logger.info("{} {}", table, getAttributes(table));
        }
    }

    public List<Table> getTables() {
        TablesFinder tablesFinder = new TablesFinder();
        return tablesFinder.getTableList(selectStatement);
    }

    public List<String> getAttributes(Table table) {
        AttributesNamesFinder attributesNamesFinder = new AttributesNamesFinder();
        return attributesNamesFinder.getAttributesList(selectStatement, table);
    }

    public List<AttributesPair> getJoinEquals() {
        JoinEqualsFinder joinEqualsFinder = new JoinEqualsFinder();
        return joinEqualsFinder.findEquals(selectStatement);
    }

    public Expression<Restriction> getCriteria() {
        RestrictionFinder restrictionFinder = new RestrictionFinder(selectStatement);
        return restrictionFinder.getResult();
    }

    public static SQLData fromFile(String file) throws IOException {
        try {
            String sql = Utils.readFile(file);
            Statement statement = new CCJSqlParserManager().parse(new StringReader(sql));
            if (statement instanceof Select) {
                return new SQLData((Select) statement);
            } else {
                throw new SQLSyntaxNotSupportedException("Incorrect SQL statement, must be SELECT");
            }
        } catch (JSQLParserException e) {
            throw new SQLInvalidSyntaxException(e.getCause().getMessage());
        }
    }

}
