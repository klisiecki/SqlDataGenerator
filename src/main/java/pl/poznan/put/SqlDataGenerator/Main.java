package pl.poznan.put.SqlDataGenerator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import pl.poznan.put.SqlDataGenerator.generator.DataController;
import pl.poznan.put.SqlDataGenerator.readers.SQLData;
import pl.poznan.put.SqlDataGenerator.readers.XMLData;

import java.io.IOException;
import java.io.StringReader;


public class Main {

    public static void main(String[] args) throws JSQLParserException, IOException {
        CCJSqlParserManager pm = new CCJSqlParserManager();

        String file = args[0];
        String sql = Utils.readFile(file + ".sql");

        XMLData xmlData = null;
        try {
            xmlData = new XMLData(file + ".xml");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }


        Statement statement = pm.parse(new StringReader(sql));

        if (statement instanceof Select) {
            Select selectStatement = (Select) statement;
            System.out.println(selectStatement);
            DataController dataController = new DataController();
            SQLData sqlData = new SQLData(selectStatement);
            dataController.initTables(xmlData, sqlData, file);

            System.out.println("dataController = " + dataController);

            for (Table table : sqlData.getTables()) {
                System.out.println("Table: " + table);
                System.out.println(sqlData.getAttributes(table));
            }

            dataController.generate();
        }
    }
}
