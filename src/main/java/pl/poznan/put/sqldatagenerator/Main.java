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
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.exception.*;
import pl.poznan.put.sqldatagenerator.generator.Generator;
import pl.poznan.put.sqldatagenerator.readers.DatabasePropertiesReader;
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
            logger.info("Wrong program arguments");
            return;
        }

        configuration.setOutputPath(ns.getString("output"));
        configuration.setSelectivity(ns.getDouble("selectivity"));
        configuration.setPropertiesLocation(ns.getString("properties"));

        try {
            SQLData sqlData = getSqlData(ns.getString("sqlFile"));
            DatabasePropertiesReader databasePropertiesReader = getXMLDatabasePropertiesReader(ns.getString("xmlFile"));
            createOutputDirectory();

            Generator generator = new Generator();
            generator.initTables(databasePropertiesReader, sqlData);
            generator.generate();
        } catch (SQLSyntaxNotSupportedException | SQLInvalidSyntaxException | XMLNotValidException
                | SQLNotCompatibleWithDatabaseException e) {
            logger.info(e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (InvalidInternalStateException | IOException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Not expected exception occurred: {}", e.getMessage());
            e.printStackTrace();
        }

        //TODO remove when problem with killing threads fixed
        System.exit(1);
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

        parser.addArgument("--properties")
                .setDefault(10000)
                .type(String.class)
                .help("Path to file with additional properties");

        try {
            return parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw e;
        }
    }

    private static SQLData getSqlData(String file) throws IOException {
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

    private static DatabasePropertiesReader getXMLDatabasePropertiesReader(String file) throws IOException, SAXException, ParserConfigurationException {
        return new XMLDatabasePropertiesReader(file);
    }

    private static void createOutputDirectory() throws IOException {
        File file = new File(configuration.getOutputPath());
        if(!file.exists() && !file.mkdir()) {
            throw new IOException("Unable to create directory " + configuration.getOutputPath());
        }
    }
}
