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

    private static void assertStoreOutputCorrect(List<File> files) {
        assertOutputFilesCount(3, files);
        assertExpectedFiles(asList("CLIENTS_0.csv", "ORDERS_0.csv", "PRODUCTS_0.csv"), files);
    }

    @Test
    public void testSimpleSelect() throws Exception {
        List<File> files = runGenerator("store_test/simpleSelect.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> clientsLines = getFileLines(files, "CLIENTS_0.csv");
        assertColumns(asList("ID", "FIRST_NAME", "LAST_NAME"), clientsLines);

        assertColumnCondition(clientsLines, "LAST_NAME", s -> asList("Cundiff", "Mastroianni").contains(s));
    }

    @Test
    public void testIntegerRanges() throws Exception {
        List<File> files = runGenerator("store_test/integerRanges.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> ordersLines = getFileLines(files, "ORDERS_0.csv");

        List<Long> expectedIds = asList(0L, 10L, 11L, 16L, 17L, 18L, 19L, 20L, 101L, 102L, 103L, 104L, 105L);
        assertColumnCondition(ordersLines, "PRODUCT_ID", s -> {
            long id = Long.parseLong(s);
            return expectedIds.contains(id);
        });

    }
}
