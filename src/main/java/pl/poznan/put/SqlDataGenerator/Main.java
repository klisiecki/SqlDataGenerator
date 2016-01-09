package pl.poznan.put.SqlDataGenerator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import pl.poznan.put.SqlDataGenerator.generator.DataController;
import pl.poznan.put.SqlDataGenerator.readers.SQLData;
import pl.poznan.put.SqlDataGenerator.readers.XMLData;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;


public class Main {

    private static final Configuration configuration = Configuration.getInstance();

    public static void main(String[] args) throws JSQLParserException, IOException {

        if (args.length < 1) {
            System.out.println("Required parameter: name for .sql and .xml file");
            return;
        }

        CCJSqlParserManager pm = new CCJSqlParserManager();
        String instanceName = args[0];
        configuration.setInstanceName(instanceName);
        String sql = Utils.readFile(instanceName + ".sql");

        if (args.length == 2) {
            try {
                configuration.setSelectivity(Double.parseDouble(args[2]));
            } catch (NumberFormatException e) {
                System.out.println(args[1] + " is not valid number");
            }
        }

        if (args.length == 3) {
            try {
                configuration.setRowsPerFile(Integer.parseInt(args[1]));
            } catch (NumberFormatException e) {
                System.out.println(args[1] + " is not valid integer");
            }
        }

        XMLData xmlData;
        try {
            xmlData = new XMLData(instanceName + ".xml");
            System.out.println(instanceName + ".xml is valid");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }

        File file = new File(instanceName);
        if(!file.exists() && !file.mkdir()) {
            System.err.println("Unable to create dir " + instanceName);
        }

        Statement statement = pm.parse(new StringReader(sql));

        if (statement instanceof Select) {
            generateDataForSelect(xmlData, (Select) statement);
        } else {
            System.out.println("Incorrect statement, must be SELECT");
        }
    }

    private static void generateDataForSelect(XMLData xmlData, Select selectStatement) {
        System.out.println("Parsed statement: " + selectStatement);
        DataController dataController = new DataController();
        SQLData sqlData = new SQLData(selectStatement);
        dataController.initTables(xmlData, sqlData);

        System.out.println();
        System.out.println("Tables (name, synonym, columns):");
        for (Table table : sqlData.getTables()) {
            System.out.println(table + " " + sqlData.getAttributes(table));
        }
        System.out.println();

        System.out.println("Generating...");
        dataController.generate();
        System.out.println("Done.");
    }
}
