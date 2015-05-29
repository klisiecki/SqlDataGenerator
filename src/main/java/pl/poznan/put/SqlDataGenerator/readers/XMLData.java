package pl.poznan.put.SqlDataGenerator.readers;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XMLData {
    private XPathFactory xPathfactory = XPathFactory.newInstance();
    private Document document;

    public XMLData(String fileName) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(fileName);


    }

    public XPathExpression getXPathExpression(String expr) {
        XPath xpath = xPathfactory.newXPath();
        try {
            return xpath.compile(expr);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getTables() {
        List<String> result = new ArrayList<String>();
        XPathExpression expr = getXPathExpression("//TABLE/NAME/text()");
        NodeList nodes = null;
        try {
            nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        } catch (Exception e) {
        }
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add(nodes.item(i).getNodeValue());
        }
        return result;
    }

    public Integer getRows(String table) {
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']/ROWS_NUM/text()", table));
        Integer result = null;
        try {
            result = Integer.parseInt(expr.evaluate(document));
        } catch (Exception e) {
        }
        return result;
    }

    public String getDistribution(String table) {
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']/DISTRIBUTION/text()", table));
        String result = null;
        try {
            result = expr.evaluate(document);
        } catch (Exception e) {
        }
        return result;
    }

    public Integer getMinRowSize(String table) {
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']/MIN_ROW_SIZE/text()", table));
        Integer result = null;
        try {
            result = Integer.parseInt(expr.evaluate(document));
        } catch (Exception e) {
        }
        return result;
    }

    public List<String> getAttributes(String table) {
        List<String> result = new ArrayList<String>();
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']//ATTRIBUTE/NAME/text()", table));
        NodeList nodes = null;
        try {
            nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getNodeValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String getAttributeProperty(String table, String attribute, String property) {
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']//ATTRIBUTE[NAME/text()='%s']/%s/text()", table, attribute, property));
        String result = null;
        try {
            result = expr.evaluate(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Integer getIntegerAttributeProperty(String table, String attribute, String property) {
        try {
            return Integer.parseInt(getAttributeProperty(table, attribute, property));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Float getFloatAttributeProperty(String table, String attribute, String property) {
        try {
            return Float.parseFloat(getAttributeProperty(table, attribute, property));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getType(String table, String attribute) {
        try {
            return getAttributeProperty(table, attribute, "TYPE");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Float getNullPercentage(String table, String attribute) {
        return getFloatAttributeProperty(table, attribute, "NULL_PERCENTAGE");
    }

    public Integer getMinValue(String table, String attribute) {
        return getIntegerAttributeProperty(table, attribute, "MIN_VALUE");
    }

    public Integer getMaxValue(String table, String attribute) {
        return getIntegerAttributeProperty(table, attribute, "MAX_VALUE");
    }

    public Float getMinUniquePercentage(String table, String attribute) {
        return getFloatAttributeProperty(table, attribute, "MIN_UNIQUE_PERCENTAGE");
    }

    public Float getMaxUniquePercentage(String table, String attribute) {
        return getFloatAttributeProperty(table, attribute, "MAX_UNIQUE_PERCENTAGE");
    }

    public List<String> getValues(String table, String attribute) {
        List<String> result = new ArrayList<>();
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']//ATTRIBUTE[NAME/text()='%s']//VALUE/text()", table, attribute));
        NodeList nodes = null;
        try {
            nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getNodeValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }



}
