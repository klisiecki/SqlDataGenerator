package pl.poznan.put.sqldatagenerator.generator;

import org.junit.Before;
import org.junit.Test;
import pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys;
import pl.poznan.put.sqldatagenerator.exception.SQLInvalidSyntaxException;
import pl.poznan.put.sqldatagenerator.exception.SQLNotCompatibleWithDatabaseException;
import pl.poznan.put.sqldatagenerator.exception.UnsatisfiableSQLException;
import pl.poznan.put.sqldatagenerator.exception.XMLNotValidException;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StoreGeneratorTest extends GeneratorTestBase {

    private static final String CLIENTS_FILENAME = "CLIENTS_0.csv";
    private static final String ORDERS_FILENAME = "ORDERS_0.csv";
    private static final String PRODUCTS_FILENAME = "PRODUCTS_0.csv";

    private static final int CLIENTS_COUNT = 100;
    private static final int PRODUCTS_COUNT = 50;
    private static final int ORDERS_COUNT = 1000;

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
        List<File> files = runGenerator("store_test/sql_correct/simpleSelect.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> clientsLines = getFileLines(files, CLIENTS_FILENAME);
        assertColumnNames(asList("ID", "FIRST_NAME", "LAST_NAME", "BIRTH_DATE"), clientsLines);
        assertColumnCondition(clientsLines, "LAST_NAME", s -> asList("Cundiff", "Mastroianni").contains(s));
    }

    @Test
    public void testXMLValuesList() throws Exception {
        List<File> files = runGenerator("store_test/sql_correct/simpleSelect.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> ordersLines = getFileLines(files, ORDERS_FILENAME);
        List<String[]> productsLines = getFileLines(files, PRODUCTS_FILENAME);
        assertColumnCondition(ordersLines, "STATE", s -> asList(1, 2, 3).contains(Integer.parseInt(s)));
        assertColumnCondition(productsLines, "CATEGORY", s -> asList("AGD", "RTV", "COMPUTERS", "ELECTRONICS").contains(s));
    }

    @Test
    public void testIntegerRanges() throws Exception {
        List<File> files = runGenerator("store_test/sql_correct/integerRanges.sql", 1.0);
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
        List<File> files = runGenerator("store_test/sql_correct/doubleRanges.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> productsLines = getFileLines(files, PRODUCTS_FILENAME);
        assertColumnCondition(productsLines, "PRICE", s -> {
            double price = Double.parseDouble(s);
            return (price >= 100 || price < 10) && price != 0;
        });
    }

    @Test
    public void testStringsSimple() throws Exception {
        List<File> files = runGenerator("store_test/sql_correct/strings.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> clientsLines = getFileLines(files, CLIENTS_FILENAME);
        assertColumnCondition(clientsLines, "LAST_NAME", s -> asList("A", "B", "D").contains(s));
    }

    @Test
    public void testStringsLike() throws Exception {
        List<File> files = runGenerator("store_test/sql_correct/stringsLike.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> clientsLines = getFileLines(files, CLIENTS_FILENAME);
        assertColumnCondition(clientsLines, "FIRST_NAME", s -> s.length() == 4 && s.charAt(1) == 'A');
        assertColumnCondition(clientsLines, "LAST_NAME", s -> !s.contains("B"));
    }

    @Test
    public void testNullWithSelectivity() throws Exception {
        double selectivity = 0.21;
        List<File> files = runGenerator("store_test/sql_correct/isNull.sql", selectivity);
        assertStoreOutputCorrect(files);

        List<String[]> clientsLines = getFileLines(files, CLIENTS_FILENAME);
        String nullValue = configuration.getStringProperty(ConfigurationKeys.DATABASE_NULL_VALUE, "NULL");
        assertColumnConditionCount(clientsLines, "LAST_NAME", s -> s.equals(nullValue), (int) (CLIENTS_COUNT * selectivity));
    }

    @Test
    public void testDuplicatedIsNull() throws Exception {
        List<File> files = runGenerator("store_test/sql_correct/isNullDuplicated.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> clientsLines = getFileLines(files, CLIENTS_FILENAME);
        String nullValue = configuration.getStringProperty(ConfigurationKeys.DATABASE_NULL_VALUE, "NULL");
        assertColumnCondition(clientsLines, "LAST_NAME", s -> s.equals(nullValue));
    }

    @Test
    public void testDateRangesWithSelectivity() throws Exception {
        double selectivity = 0.68;
        List<File> files = runGenerator("store_test/sql_correct/dateRanges.sql", selectivity);
        assertStoreOutputCorrect(files);

        String outputDateFormat = "yyyy-MM-dd HH:mm:ss";
        DateFormat df = new SimpleDateFormat(outputDateFormat);
        List<String[]> ordersLines = getFileLines(files, ORDERS_FILENAME);
        Date from = df.parse("2015-01-01 00:00:00");
        Date to = df.parse("2015-02-01 00:00:00");
        assertColumnConditionCount(ordersLines, "DATETIME", s -> {
            try {
                Date date = df.parse(s);
                return date.after(from) && date.before(to);
            } catch (ParseException e) {
                return false;
            }
        }, (int) (ORDERS_COUNT * selectivity));
        assertColumnConditionCount(ordersLines, "DATETIME", s -> {
            try {
                Date date = df.parse(s);
                return date.before(from) || date.after(to);
            } catch (ParseException e) {
                return false;
            }
        }, ORDERS_COUNT - (int) (ORDERS_COUNT * selectivity));
    }

    @Test
    public void testKeysAndJoins() throws Exception {
        List<File> files = runGenerator("store_test/sql_correct/joins.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> clientsLines = getFileLines(files, CLIENTS_FILENAME);
        List<String[]> productsLines = getFileLines(files, PRODUCTS_FILENAME);
        List<String[]> ordersLines = getFileLines(files, ORDERS_FILENAME);

        assertColumnCondition(clientsLines, "FIRST_NAME", "John"::equals);
        assertColumnCondition(productsLines, "PRICE", s -> Double.parseDouble(s) >= 100);

        List<String> cl_ids = getColumnValues(clientsLines, "ID");
        List<String> o_cl_ids = getColumnValues(ordersLines, "CLIENT_ID");
        HashSet<String> cl_ids_set = new HashSet<>(cl_ids);
        assertEquals(cl_ids_set, new HashSet<>(o_cl_ids));
        assertEquals("Column ID cannot contain duplicated values", cl_ids.size(), cl_ids_set.size());

        List<String> pr_ids = getColumnValues(productsLines, "ID");
        List<String> o_pr_ids = getColumnValues(ordersLines, "PRODUCT_ID");
        HashSet<String> pr_ids_set = new HashSet<>(pr_ids);
        assertEquals(pr_ids_set, new HashSet<>(o_pr_ids));
        assertEquals("Column ID cannot contain duplicated values", pr_ids.size(), pr_ids_set.size());

        List<String> or_ids = getColumnValues(ordersLines, "ID");
        HashSet<String> or_ids_set = new HashSet<>(or_ids);
        assertEquals("Column ID cannot contain duplicated values", or_ids.size(), or_ids_set.size());
    }

    @Test
    public void testTwoColumnsRelation1() throws Exception {
        List<File> files = runGenerator("store_test/sql_correct/twoColumnsRelation1.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> productsLines = getFileLines(files, PRODUCTS_FILENAME);
        assertColumnsRelation(productsLines, "PACKAGE_WIDTH", "PACKAGE_HEIGHT",
                (p, op) -> Double.parseDouble(p) <= Double.parseDouble(op));
        assertColumnCondition(productsLines, "PACKAGE_HEIGHT", s -> Double.parseDouble(s) <= 100);

    }

    @Test
    public void testTwoColumnsRelation2() throws Exception {
        List<File> files = runGenerator("store_test/sql_correct/twoColumnsRelation2.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> productsLines = getFileLines(files, PRODUCTS_FILENAME);
        assertColumnsRelation(productsLines, "PACKAGE_WIDTH", "PACKAGE_HEIGHT",
                (p, op) -> Double.parseDouble(p) > Double.parseDouble(op));
    }

    @Test
    public void testTwoColumnsRelations1() throws Exception {
        List<File> files = runGenerator("store_test/sql_correct/twoColumnsRelations1.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> productsLines = getFileLines(files, PRODUCTS_FILENAME);
        assertColumnsRelation(productsLines, "PRICE", "OLD_PRICE",
                (p, op) -> Double.parseDouble(p) >= Double.parseDouble(op));
        assertColumnsRelation(productsLines, "NAME", "DESCRIPTION", String::equals);
    }

    @Test
    public void testTwoColumnsRelations2() throws Exception {
        List<File> files = runGenerator("store_test/sql_correct/twoColumnsRelations2.sql", 1.0);
        assertStoreOutputCorrect(files);

        List<String[]> productsLines = getFileLines(files, PRODUCTS_FILENAME);
        assertColumnsRelation(productsLines, "PRICE", "OLD_PRICE",
                (p, op) -> Double.parseDouble(p) < Double.parseDouble(op));
        assertColumnsRelation(productsLines, "NAME", "DESCRIPTION", (n, d) -> !n.equals(d));
    }

    @Test
    public void testNonexistentTable() throws Exception {
        Throwable caught = null;
        try {
            runGenerator("store_test/sql_incorrect/nonexistent_table.sql", 1.0);
        } catch (Throwable t) {
            caught = t;
        }
        assertNotNull(caught);
        assertEquals(caught.getClass(), SQLNotCompatibleWithDatabaseException.class);
    }

    @Test
    public void testIncorrectSyntax() throws Exception {
        Throwable caught = null;
        try {
            runGenerator("store_test/sql_incorrect/syntax_error.sql", 1.0);
        } catch (Throwable t) {
            caught = t;
        }
        assertNotNull(caught);
        assertEquals(caught.getClass(), SQLInvalidSyntaxException.class);
    }

    @Test
    public void testUnsatisfiableQuery() throws Exception {
        Throwable caught = null;
        try {
            runGenerator("store_test/sql_incorrect/unsatisfiable.sql", 1.0);
        } catch (Throwable t) {
            caught = t;
        }
        assertNotNull(caught);
        assertEquals(caught.getClass(), UnsatisfiableSQLException.class);
    }

    @Test
    public void testInvalidXML() throws Exception {
        String schema = databaseSchema;
        try {
            databaseSchema = "store_test/store_invalid.xml";
            Throwable caught = null;
            try {
                runGenerator("store_test/sql_correct/simpleSelect.sql", 1.0);
            } catch (Throwable t) {
                caught = t;
            }
            assertNotNull(caught);
            assertEquals(caught.getClass(), XMLNotValidException.class);
        } finally {
            databaseSchema = schema;
        }
    }
}
