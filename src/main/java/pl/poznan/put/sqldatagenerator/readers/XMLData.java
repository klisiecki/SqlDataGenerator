package pl.poznan.put.sqldatagenerator.readers;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.poznan.put.sqldatagenerator.restriction.Restrictions;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XMLData {
    private final XPathFactory xPathfactory = XPathFactory.newInstance();
    private Document document;
    private final static String schemaLocation = "/pl/poznan/put/sqldatagenerator/schema.xsd";

    public XMLData(String fileName) throws ParserConfigurationException, IOException, SAXException {
        validate(fileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(fileName);
    }

    public Restrictions getConstraints() {
        return new Restrictions(Collections.emptyList());
    }

    private void validate(String fileName) throws IOException, SAXException {
        Source xmlFile = new StreamSource(new File(fileName));
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(getClass().getResource(schemaLocation));
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);

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

    public Integer getM() {
        Integer result = null;
        XPathExpression e = getXPathExpression("/TABLES/@M");
        try {
            result = Integer.parseInt(e.evaluate(document));
        } catch (XPathExpressionException e1) {
            e1.printStackTrace();
        } catch (NumberFormatException ignore) {}
        return result;
    }

    public Integer getT() {
        Integer result = null;
        XPathExpression e = getXPathExpression("/TABLES/@T");
        try {
            result = Integer.parseInt(e.evaluate(document));
        } catch (XPathExpressionException e1) {
            e1.printStackTrace();
        } catch (NumberFormatException ignore) {}
        return result;
    }

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

    public Integer getMaxRowsNum() {
        XPathExpression expr = getXPathExpression("max(//TABLE/ROWS_NUM/text())");
        Integer result;
        try {
            result = Integer.parseInt(expr.evaluate(document));
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    public String getDistribution(String table) {
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']/DISTRIBUTION/text()", table));
        String result;
        try {
            result = expr.evaluate(document);
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    public Integer getMinRowSize(String table) {
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']/MIN_ROW_SIZE/text()", table));
        Integer result;
        try {
            result = Integer.parseInt(expr.evaluate(document));
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    public List<String> getAttributes(String table) {
        List<String> result = new ArrayList<>();
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']//ATTRIBUTE/NAME/text()", table));
        NodeList nodes;
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
        String result;
        try {
            result = expr.evaluate(document);
        } catch (Exception e) {
            return null;
        }
        return result.equals("") ? null : result;
    }

    private Integer getIntegerAttributeProperty(String table, String attribute, String property) {
        try {
            return Integer.parseInt(getAttributeProperty(table, attribute, property));
        } catch (Exception e) {
            return null;
        }
    }

    private Float getFloatAttributeProperty(String table, String attribute, String property) {
        try {
            return Float.parseFloat(getAttributeProperty(table, attribute, property));
        } catch (Exception e) {
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

    public boolean isPrimaryKey(String table, String attribute) {
        return ("true").equals(getAttributeProperty(table, attribute, "PRIMARY_KEY"));
    }

    public Float getNullPercentage(String table, String attribute) {
        return getFloatAttributeProperty(table, attribute, "NULL_PERCENTAGE");
    }

    public String getMinValue(String table, String attribute) {
        return getAttributeProperty(table, attribute, "MIN_VALUE");
    }

    public String getMaxValue(String table, String attribute) {
        return getAttributeProperty(table, attribute, "MAX_VALUE");
    }

    public Float getMinUniquePercentage(String table, String attribute) {
        return getFloatAttributeProperty(table, attribute, "UNIQUE_PERCENTAGE/MIN");
    }

    public Float getMaxUniquePercentage(String table, String attribute) {
        return getFloatAttributeProperty(table, attribute, "UNIQUE_PERCENTAGE/MAX");
    }

    public List<String> getValues(String table, String attribute) {
        List<String> result = new ArrayList<>();
        XPathExpression expr = getXPathExpression(
                String.format("//TABLE[NAME/text()='%s']//ATTRIBUTE[NAME/text()='%s']//VALUE/text()", table, attribute));
        NodeList nodes;
        try {
            nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getNodeValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.size() == 0 ? null : result;
    }


}
