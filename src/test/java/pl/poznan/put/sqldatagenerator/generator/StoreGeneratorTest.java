package pl.poznan.put.sqldatagenerator.generator;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;

public class StoreGeneratorTest extends GeneratorTestBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        databaseSchema = "store_test/store.xml";
        databaseTypesDescription = "datatypes/netezza.xml";
    }

    @Test
    public void testSimpleSelect() throws Exception {
        List<File> files = runGenerator("store_test/simpleSelect.sql", 1.0);

        assertOutputFilesCount(3, files);
        assertExpectedFiles(asList("CLIENTS_0.csv", "ORDERS_0.csv", "PRODUCTS_0.csv"), files);

        List<String[]> clientsLines = getFileLines(files, "CLIENTS_0.csv");
        assertColumns(asList("ID", "FIRST_NAME", "LAST_NAME"), clientsLines);

        assertColumnCondition(clientsLines, "LAST_NAME", s -> asList("Cundiff", "Mastroianni").contains(s));
    }

}
