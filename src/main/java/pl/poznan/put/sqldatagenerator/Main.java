package pl.poznan.put.sqldatagenerator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import pl.poznan.put.sqldatagenerator.generator.DataController;
import pl.poznan.put.sqldatagenerator.readers.DatabaseProperties;
import pl.poznan.put.sqldatagenerator.readers.SQLData;
import pl.poznan.put.sqldatagenerator.readers.XMLDatabasePropertiesReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Configuration configuration = Configuration.getInstance();

    public static void main(String[] args) {
        Namespace ns;
        try {
            ns = initArgumentParser(args);
        } catch (ArgumentParserException e) {
            return;
        }

        configuration.setOutputPath(ns.getString("output"));
        configuration.setSelectivity(ns.getDouble("selectivity"));
        configuration.setRowsPerFile(ns.getInt("maxRows"));

        SQLData sqlData = getSqlData(ns.getString("sqlFile"));
        DatabaseProperties databaseProperties = getDatabaseProperties(ns.getString("xmlFile"));
        createOutputDirectory();

        DataController dataController = new DataController();
        dataController.initTables(databaseProperties, sqlData);
        dataController.generate();
    }

    private static Namespace initArgumentParser(String[] args) throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("generator")
                .defaultHelp(true)
                .description("Some description.");

        parser.addArgument("--xmlFile")
                .required(true)
                .help("XML file");

        parser.addArgument("--sqlFile")
                .required(true)
                .help("SQL file");

        parser.addArgument("--output")
                .required(true)
                .help("Output data location");

        parser.addArgument("--selectivity")
                .setDefault(0.5)
                .type(Double.class)
                .help("Expected selectivity");

        parser.addArgument("--maxRows")
                .setDefault(10000)
                .type(Integer.class)
                .help("Max number of rows in output file");


        try {
            return parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw e;
        }
    }

    private static SQLData getSqlData(String file) {
        try {
            String sql = Utils.readFile(file);
            Statement statement = new CCJSqlParserManager().parse(new StringReader(sql));
            if (statement instanceof Select) {
                return new SQLData((Select)statement);
            } else {
                logger.info("Incorrect statement, must be SELECT");
                //TODO Throw exception
            }
        } catch (IOException | JSQLParserException e) {
            e.printStackTrace();
            //TODO Throw exception
        }
        return null;
    }

    private static DatabaseProperties getDatabaseProperties(String file) {
        try {
            return new DatabaseProperties(new XMLDatabasePropertiesReader(file));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("Error reading XML file", e);
            //TODO Throw exception
            //TODO separate info for non valid XML file
        }
        return null;
    }

    private static void createOutputDirectory() {
        File file = new File(configuration.getOutputPath());
        if(!file.exists() && !file.mkdir()) {
            logger.error("Unable to create dir {}", configuration.getOutputPath());
            //TODO Throw exception
        }
    }
}
