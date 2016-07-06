package pl.poznan.put.sqldatagenerator.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.poznan.put.sqldatagenerator.exception.XMLNotValidException;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DatabaseType;

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
import java.util.List;

public class XMLDatabasePropertiesReader implements DatabasePropertiesReader {
    private static final Logger logger = LoggerFactory.getLogger(XMLDatabasePropertiesReader.class);

    private final static String schemaLocation = "/schema.xsd";
    private final XPathFactory xPathfactory = XPathFactory.newInstance();
    private Document document;

    public XMLDatabasePropertiesReader(String fileName) throws ParserConfigurationException, IOException, SAXException {
        validate(fileName);
        logger.info("{} is valid", fileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(fileName);
    }

    private void validate(String fileName) throws IOException, SAXException {
        Source xmlFile = new StreamSource(new File(fileName));
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(getClass().getResource(schemaLocation));
        Validator validator = schema.newValidator();
        try {
            validator.validate(xmlFile);
        } catch (SAXException e) {
            throw new XMLNotValidException(e.getMessage());
        }
    }

    @Override
    public Integer getM() {
        Integer result = null;
        XPathExpression e = getXPathExpression("/TABLES/@M");
        try {
            result = Integer.parseInt(e.evaluate(document));
        } catch (XPathExpressionException e1) {
            e1.printStackTrace();
        } catch (NumberFormatException ignore) {
        }
        return result;
    }

    @Override
    public Integer getT() {
        Integer result = null;
        XPathExpression e = getXPathExpression("/TABLES/@T");
        try {
            result = Integer.parseInt(e.evaluate(document));
        } catch (XPathExpressionException e1) {
            e1.printStackTrace();
        } catch (NumberFormatException ignore) {
        }
        return result;
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

    @Override
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

    @Override
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

    @Override
    public DatabaseType getDatabaseType(String table, String attribute) {
        try {
            String type = getAttributeProperty(table, attribute, "TYPE");
            String typeName = getTypeName(type);
            Integer scale = getSecondParam(type);
            return new DatabaseType(DatabaseType.Type.valueOf(typeName), scale);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getTypeName(String type) {
        int parenthesisPos = type.indexOf('(');
        return parenthesisPos < 0 ? type : type.substring(0, parenthesisPos);
    }

    private Integer getFirstParam(String type) {
        String[] typeParams = getTypeParams(type);
        if (typeParams != null) {
            return Integer.valueOf(typeParams[0]);
        }
        return null;
    }

    private Integer getSecondParam(String type) {
        String[] typeParams = getTypeParams(type);
        if (typeParams != null && typeParams.length > 1) {
            return Integer.valueOf(typeParams[1]);
        }
        return null;
    }

    private String[] getTypeParams(String type) {
        int parenthesisPos = type.indexOf('(');
        if (parenthesisPos > 0) {
            String parameters = type.substring(parenthesisPos + 1, type.length() - 1);
            return parameters.split(",");
        }
        return null;
    }

    @Override
    public boolean isPrimaryKey(String table, String attribute) {
        return "true".equals(getAttributeProperty(table, attribute, "PRIMARY_KEY"));
    }

    @Override
    public Float getNullPercentage(String table, String attribute) {
        return getFloatAttributeProperty(table, attribute, "NULL_PERCENTAGE");
    }

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

    @Override
    public Float getMinUniquePercentage(String table, String attribute) {
        return getFloatAttributeProperty(table, attribute, "UNIQUE_PERCENTAGE/MIN");
    }

    @Override
    public Float getMaxUniquePercentage(String table, String attribute) {
        return getFloatAttributeProperty(table, attribute, "UNIQUE_PERCENTAGE/MAX");
    }

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

    private List<String> getList(XPathExpression expr) {
        List<String> result = new ArrayList<>();
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

    private XPathExpression getXPathExpression(String expr) {
        XPath xpath = xPathfactory.newXPath();
        try {
            return xpath.compile(expr);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
