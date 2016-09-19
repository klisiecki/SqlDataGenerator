package pl.poznan.put.sqldatagenerator;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.exception.*;
import pl.poznan.put.sqldatagenerator.generator.Generator;
import pl.poznan.put.sqldatagenerator.readers.*;
import pl.poznan.put.sqldatagenerator.writers.CSVTableWriter;

import java.io.File;
import java.io.IOException;

import static pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys.DATABASE_TYPES_DESCRIPTION;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Configuration configuration = Configuration.getInstance();

    private static final String databaseTypesDescription =
            configuration.getStringProperty(DATABASE_TYPES_DESCRIPTION, "/datatypes/netezza.xml");

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
            SQLData sqlData = SQLData.fromFile(ns.getString("sqlFile"));
            DatabaseSchemaReader databaseSchemaReader = new XMLDatabaseSchemaReader(ns.getString("xmlFile"));
            DatabaseTypesReader databaseTypesReader = new XMLDatabaseTypesReader(databaseTypesDescription);
            createOutputDirectory();

            DatabaseProperties databaseProperties = new DatabaseProperties(databaseSchemaReader, databaseTypesReader);
            Generator generator = new Generator(CSVTableWriter.class);
            generator.generate(databaseProperties, sqlData);
        } catch (SQLSyntaxNotSupportedException | SQLInvalidSyntaxException | XMLNotValidException
                | SQLNotCompatibleWithDatabaseException | UnsatisfiableSQLException e) {
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

    private static void createOutputDirectory() throws IOException {
        File file = new File(configuration.getOutputPath());
        if(!file.exists() && !file.mkdir()) {
            throw new IOException("Unable to create directory " + configuration.getOutputPath());
        }
    }
}
