package pl.poznan.put.SqlDataGenerator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import pl.poznan.put.SqlDataGenerator.generator.DataController;
import pl.poznan.put.SqlDataGenerator.generator.RandomGenerator;
import pl.poznan.put.SqlDataGenerator.readers.SQLData;
import pl.poznan.put.SqlDataGenerator.readers.XMLData;

import java.io.IOException;
import java.io.StringReader;


public class Main {

    public static void main(String[] args) throws JSQLParserException, IOException {

        if (args.length < 1) {
            System.out.println("Required parameter: name for .sql and .xml file");
        }

        CCJSqlParserManager pm = new CCJSqlParserManager();
        String file = args[0];
        String sql = Utils.readFile(file + ".sql");

        XMLData xmlData = null;
        try {
            xmlData = new XMLData(file + ".xml");
            System.out.println(file + ".xml is valid");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }


        Statement statement = pm.parse(new StringReader(sql));

        if (statement instanceof Select) {
            Select selectStatement = (Select) statement;
            System.out.println("Parsed statement: " + selectStatement);
            DataController dataController = new DataController();
            SQLData sqlData = new SQLData(selectStatement);
            dataController.initTables(xmlData, sqlData, file);

            System.out.println();
            System.out.println("Tables (name, synonim, columns):");
            for (Table table : sqlData.getTables()) {
                System.out.println(table + " " + sqlData.getAttributes(table));
            }
            System.out.println();

            System.out.println("Generating...");
            dataController.generate();
            System.out.println("Done.");
        } else {
            System.out.println("Incorrect statement, must be SELECT");
        }
    }
}
