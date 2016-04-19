package pl.poznan.put.SqlDataGenerator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.log4j.Logger;
import pl.poznan.put.SqlDataGenerator.generator.DataController;
import pl.poznan.put.SqlDataGenerator.readers.SQLData;
import pl.poznan.put.SqlDataGenerator.readers.XMLData;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;


public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    private static final Configuration configuration = Configuration.getInstance();

    public static void main(String[] args) throws JSQLParserException, IOException {

        if (args.length != 3) {
            logger.error("Required parameters: "); //TODO print usage
            return;
        }

        CCJSqlParserManager pm = new CCJSqlParserManager();
        String instanceName = args[0];
        configuration.setInstanceName(instanceName);
        String sql = Utils.readFile(instanceName + ".sql");

        try {
            configuration.setSelectivity(Double.parseDouble(args[1]));
        } catch (NumberFormatException e) {
            logger.error(args[1] + " is not valid number");
        }

        try {
            configuration.setRowsPerFile(Integer.parseInt(args[2]));
        } catch (NumberFormatException e) {
            logger.error(args[1] + " is not valid integer");
        }

        XMLData xmlData;
        try {
            xmlData = new XMLData(instanceName + ".xml");
            logger.info(instanceName + ".xml is valid");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        File file = new File(instanceName);
        if(!file.exists() && !file.mkdir()) {
            logger.error("Unable to create dir " + instanceName);
        }

        Statement statement = pm.parse(new StringReader(sql));

        if (statement instanceof Select) {
            generateDataForSelect(xmlData, (Select) statement);
        } else {
            logger.info("Incorrect statement, must be SELECT");
        }
    }

    private static void generateDataForSelect(XMLData xmlData, Select selectStatement) {
        logger.info("Parsed statement: " + selectStatement);
        DataController dataController = new DataController();
        SQLData sqlData = new SQLData(selectStatement);
        dataController.initTables(xmlData, sqlData);

        logger.info("Tables (name, synonym, columns):");
        for (Table table : sqlData.getTables()) {
            logger.info(table + " " + sqlData.getAttributes(table));
        }

        logger.info("Generating...");
        dataController.generate();
        logger.info("Done.");
    }
}
