package pl.poznan.put.SqlDataGenerator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

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
        if (statement instanceof Select) {
            Select selectStatement = (Select) statement;

            //System.out.println("selectStatement.toString() = " + selectStatement.toString());
            
            ParserTest parserTest = new ParserTest();
            List<String> tableList = parserTest.getTableList(selectStatement);

            for(String table : tableList) {
                System.out.println("table = " + table);
            }

        }
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
