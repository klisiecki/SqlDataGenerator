package pl.poznan.put.sqldatagenerator.generator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;

public class StoreGeneratorTest extends GeneratorTestBase {

    private static final String CLIENTS_FILENAME = "CLIENTS_0.csv";
    private static final String ORDERS_FILENAME = "ORDERS_0.csv";
    private static final String PRODUCTS_FILENAME = "PRODUCTS_0.csv";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        databaseSchema = "store_test/store.xml";
        databaseTypesDescription = "datatypes/netezza.xml";
    }

    private static void assertStoreOutputCorrect(List<File> files) {
        assertOutputFilesCount(3, files);
        assertExpectedFiles(asList(CLIENTS_FILENAME, ORDERS_FILENAME, PRODUCTS_FILENAME), files);
    }

    @Test
    public void testSimpleSelect() throws Exception {
        List<File> files = runGenerator("store_test/simpleSelect.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> clientsLines = getFileLines(files, CLIENTS_FILENAME);
        assertColumns(asList("ID", "FIRST_NAME", "LAST_NAME"), clientsLines);

        assertColumnCondition(clientsLines, "LAST_NAME", s -> asList("Cundiff", "Mastroianni").contains(s));
    }

    @Test
    public void testIntegerRanges() throws Exception {
        List<File> files = runGenerator("store_test/integerRanges.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> ordersLines = getFileLines(files, ORDERS_FILENAME);

        List<Long> expectedIds = asList(0L, 10L, 11L, 16L, 17L, 18L, 19L, 20L, 101L, 102L, 103L, 104L, 105L);
        assertColumnCondition(ordersLines, "PRODUCT_ID", s -> {
            long id = Long.parseLong(s);
            return expectedIds.contains(id);
        });
    }

    @Test
    public void testDoubleRanges() throws Exception {
        List<File> files = runGenerator("store_test/doubleRanges.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> productsLines = getFileLines(files, PRODUCTS_FILENAME);

        assertColumnCondition(productsLines, "PRICE", s -> {
            double price = Double.parseDouble(s);
            return (price >= 100 || price < 10) && price != 0;
        });
    }

    @Ignore //TODO remove after strings inequality is implemented
    @Test
    public void testStrings() throws Exception {
        List<File> files = runGenerator("store_test/strings.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> clientsLines = getFileLines(files, CLIENTS_FILENAME);

        assertColumnCondition(clientsLines, "LAST_NAME", s -> asList("A", "B", "D").contains(s));
    }

    @Test
    public void testNullWithSelectivity() throws Exception {
        List<File> files = runGenerator("store_test/null.sql", 0.21);
        assertStoreOutputCorrect(files);

        List<String[]> clientsLines = getFileLines(files, CLIENTS_FILENAME);
        String nullValue = configuration.getStringProperty(ConfigurationKeys.DATABASE_NULL_VALUE, "NULL");
        assertColumnConditionCount(clientsLines, "LAST_NAME", s -> s.equals(nullValue), 21);
    }
}
