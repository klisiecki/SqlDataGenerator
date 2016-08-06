package pl.poznan.put.sqldatagenerator.generator;

import com.opencsv.CSVReader;
import org.xml.sax.SAXException;
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.readers.*;
import pl.poznan.put.sqldatagenerator.writers.CSVTableWriter;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class GeneratorTestBase {
    protected static final Configuration configuration = Configuration.getInstance();

    protected static String databaseSchema;
    protected static String databaseTypesDescription;

    protected List<File> runGenerator(String sqlFile) throws IOException, SAXException, ParserConfigurationException {
        SQLData sqlData = SQLData.fromFile(getAbsolutePath(sqlFile));
        DatabaseSchemaReader databaseSchemaReader = new XMLDatabaseSchemaReader(getAbsolutePath(databaseSchema));
        DatabaseTypesReader databaseTypesReader = new XMLDatabaseTypesReader(getAbsolutePath(databaseTypesDescription));
        DatabaseProperties databaseProperties = new DatabaseProperties(databaseSchemaReader, databaseTypesReader);

        Generator generator = new Generator();
        generator.setWriterClass(CSVTableWriter.class);
        generator.initTables(databaseProperties, sqlData);
        generator.generate();

        return getOutputFiles();
    }

    private List<File> getOutputFiles() {
        String outputPath = configuration.getOutputPath();
        File outputDir = new File(outputPath);
        assertTrue(outputDir.isDirectory());
        return asList(outputDir.listFiles());
    }

    private String getAbsolutePath(String resPath) {
        return getClass().getClassLoader().getResource(resPath).getPath();
    }

    protected List<String[]> getFileLines(List<File> files, String fileName) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(
                files.stream().filter(f -> f.getName().equals(fileName)).findFirst().get()), ';');
        return csvReader.readAll();
    }

    protected static void assertColumns(List<String> expectedColumns, List<String[]> lines) {
        List<String> header = asList(lines.get(0));
        assertEquals(expectedColumns.size(), header.size());
        for (String column : header) {
            assertTrue(column + " not found in " + expectedColumns, expectedColumns.contains(column));
        }
    }

    protected static void assertColumnCondition(List<String[]> fileLines, String column, Predicate<String> predicate) {
        List<String> header = asList(fileLines.get(0));
        fileLines = fileLines.subList(1, fileLines.size());
        int columnIndex = header.indexOf(column);
        for (String[] fileLine : fileLines) {
            String value = fileLine[columnIndex];
            assertTrue(value + " doesn't match given condition", predicate.test(value));
        }
    }

    protected static void assertOutputFilesCount(int count, List<File> files) {
        assertTrue("There should be " + count + " output files for " + databaseSchema + " database but found " + files.size(),
                files.size() == count);
    }

    protected static void assertExpectedFiles(List<String> expectedFiles, List<File> files) {
        for (File file : files) {
            assertTrue(file.getName() + " not found in " + expectedFiles, expectedFiles.contains(file.getName()));
        }
    }
}