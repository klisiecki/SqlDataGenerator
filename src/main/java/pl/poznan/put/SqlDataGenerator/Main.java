package pl.poznan.put.SqlDataGenerator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.xml.sax.SAXException;
import pl.poznan.put.SqlDataGenerator.generator.DataController;
import pl.poznan.put.SqlDataGenerator.generator.DataTable;
import pl.poznan.put.SqlDataGenerator.readers.SQLData;
import pl.poznan.put.SqlDataGenerator.readers.XMLData;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;


public class Main {

    public static void main(String[] args) throws JSQLParserException, IOException {
        CCJSqlParserManager pm = new CCJSqlParserManager();

//        String sql = readFile("in/simple6TablesJoin.sql");
//        String sql = readFile("in/ibm2.sql");
        String sql = readFile("in/simpleJoin.sql");

        Statement statement = pm.parse(new StringReader(sql));

        /*
        now you should use a class that implements StatementVisitor to decide what to do
        based on the kind of the statement, that is SELECT or INSERT etc. but here we are only
        interested in SELECTS
        */

        TestVisitor visitor = new TestVisitor();

        if (statement instanceof Select) {
            Select selectStatement = (Select) statement;

            System.out.println(visitor.getSelectItems(selectStatement));
            System.out.println("selectStatement = " + selectStatement);
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(selectStatement);

            for(String table : tableList) {
                System.out.println("table = " + table);
            }

        }
        XMLData data = null;
        try {
            data = new XMLData("xml/tabele.xml");
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        if (data != null) {
            List<String> tables = data.getTables();
            System.out.println(tables);

            String table = tables.get(0);
            System.out.println(data.getRows(table));
            System.out.println(data.getDistribution(table));
            System.out.println(data.getMinRowSize(table));
            System.out.println(data.getAttributes(table));

            System.out.println(data.getNullPercentage(table, data.getAttributes(table).get(0)));
            System.out.println(data.getValues(table, data.getAttributes(table).get(0)));
        }

        DataController dataController = new DataController();
        dataController.initTables(data, new SQLData());
    }



    static String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
}
