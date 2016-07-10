package pl.poznan.put.sqldatagenerator.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XMLDatabaseSchemaReader extends AbstractXMLReader implements DatabaseSchemaReader {
    private static final Logger logger = LoggerFactory.getLogger(XMLDatabaseSchemaReader.class);

    public XMLDatabaseSchemaReader(String fileName) throws ParserConfigurationException, IOException, SAXException {
        super(fileName, "/schema.xsd");
    }

    @Override
    public Integer getMaxRowsNum() {
        XPathExpression expr = getXPathExpression("//TABLE/ROWS_NUM/text()[not(. < //TABLE/ROWS_NUM/text())][1]");
        Integer result;
        try {
            result = Integer.parseInt(expr.evaluate(document));
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    @Override
    public List<String> getTables() {
        List<String> result = new ArrayList<>();
        XPathExpression expr = getXPathExpression("//TABLE/NAME/text()");
        NodeList nodes;
        try {
            nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        } catch (Exception e) {
            return null;
        }
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add(nodes.item(i).getNodeValue());
        }
        return result;
    }

    @Override
    public List<String> getAttributes(String table) {
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']//ATTRIBUTE/NAME/text()", table));
        return getList(expr);
    }

    @Override
    public Integer getRowsNum(String table) {
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']/ROWS_NUM/text()", table));
        Integer result;
        try {
            result = Integer.parseInt(expr.evaluate(document));
        } catch (Exception e) {
            return null;
        }
        return result;
    }

//    @Override
//    public Integer getMinRowSize(String table) {
//        XPathExpression expr = getXPathExpression(
//                String.format("//TABLE[NAME/text()='%s']/MIN_ROW_SIZE/text()", table));
//        Integer result;
//        try {
//            result = Integer.parseInt(expr.evaluate(document));
//        } catch (Exception e) {
//            return null;
//        }
//        return result;
//    }
//
//    @Override
//    public String getDistribution(String table) {
//        XPathExpression expr = getXPathExpression(
//                String.format("//TABLE[NAME/text()='%s']/DISTRIBUTION/text()", table));
//        String result;
//        try {
//            result = expr.evaluate(document);
//        } catch (Exception e) {
//            return null;
//        }
//        return result;
//    }

    @Override
    public String getType(String table, String attribute) {
        return getAttributeProperty(table, attribute, "TYPE");
    }

    @Override
    public boolean isPrimaryKey(String table, String attribute) {
        return "true".equals(getAttributeProperty(table, attribute, "PRIMARY_KEY"));
    }

//    @Override
//    public Float getNullPercentage(String table, String attribute) {
//        return getFloatAttributeProperty(table, attribute, "NULL_PERCENTAGE");
//    }

    @Override
    public String getMinValue(String table, String attribute) {
        return getAttributeProperty(table, attribute, "MIN_VALUE");
    }

    @Override
    public String getMaxValue(String table, String attribute) {
        return getAttributeProperty(table, attribute, "MAX_VALUE");
    }

    @Override
    public List<String> getValues(String table, String attribute) {
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']//ATTRIBUTE[NAME/text()='%s']//VALUE/text()", table, attribute));
        List<String> values = getList(expr);
        return values.size() == 0 ? null : values;
    }

//    @Override
//    public Float getMinUniquePercentage(String table, String attribute) {
//        return getFloatAttributeProperty(table, attribute, "UNIQUE_PERCENTAGE/MIN");
//    }
//
//    @Override
//    public Float getMaxUniquePercentage(String table, String attribute) {
//        return getFloatAttributeProperty(table, attribute, "UNIQUE_PERCENTAGE/MAX");
//    }

    private Float getFloatAttributeProperty(String table, String attribute, String property) {
        try {
            return Float.parseFloat(getAttributeProperty(table, attribute, property));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getIntegerAttributeProperty(String table, String attribute, String property) {
        try {
            return Integer.parseInt(getAttributeProperty(table, attribute, property));
        } catch (Exception e) {
            return null;
        }
    }

    private String getAttributeProperty(String table, String attribute, String property) {
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']//ATTRIBUTE[NAME/text()='%s']/%s/text()", table, attribute, property));
        String result;
        try {
            result = expr.evaluate(document);
        } catch (Exception e) {
            return null;
        }
        return result.equals("") ? null : result;
    }

}
