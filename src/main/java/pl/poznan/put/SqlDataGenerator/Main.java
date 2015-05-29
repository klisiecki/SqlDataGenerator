package pl.poznan.put.SqlDataGenerator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
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

//        String sql = readFile("in/simple6TablesJoin.sql");
//        String sql = readFile("in/ibm2.sql");
        String sql = Utils.readFile("in/simpleJoin.sql");

        XMLData xmlData = null;
        try {
            xmlData = new XMLData("xml/tabele.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }


        Statement statement = pm.parse(new StringReader(sql));

        if (statement instanceof Select) {
            Select selectStatement = (Select) statement;

            DataController dataController = new DataController();
            SQLData sqlData = new SQLData(selectStatement);
            dataController.initTables(xmlData, sqlData);

            System.out.println("dataController = " + dataController);

            for (String tableName: sqlData.getTables()) {
                System.out.println("Table: " + tableName);
                System.out.println(sqlData.getAttributes(tableName));
            }
        }
    }
}
