package pl.poznan.put.sqldatagenerator.readers;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.AttributeType;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;
import pl.poznan.put.sqldatagenerator.generator.TableBase;
import pl.poznan.put.sqldatagenerator.generator.key.KeyGenerator;
import pl.poznan.put.sqldatagenerator.generator.key.SimpleKeyGenerator;
import pl.poznan.put.sqldatagenerator.restriction.Restrictions;
import pl.poznan.put.sqldatagenerator.restriction.types.PrimaryKeyRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.RangeRestriction;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;

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
import java.util.Map;

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

    public Restrictions getConstraints(Map<String, TableBase> tableBaseMap) {
        List<Restriction> restrictionList = new ArrayList<>();
        for (String tableName : getTables()) {
            for (String attributeName : getAttributes(tableName)) {
                List<Attribute> attributes = AttributesMap.get(tableBaseMap.get(tableName), attributeName);
                AttributeType attributeType = getType(tableName, attributeName);
                RangeSet rangeSet = null;
                KeyGenerator keyGenerator = null;
                switch (attributeType) {
                    case INTEGER:
                        if (isPrimaryKey(tableName, attributeName)) {
                            keyGenerator = new SimpleKeyGenerator(getRowsNum(tableName));
                        } else {
                            rangeSet = getIntegerRangeSet(tableName, attributeName);
                        }
                        break;
                }
                for (Attribute attribute : attributes) {
                    if (rangeSet != null) {
                        restrictionList.add(new RangeRestriction(attribute, rangeSet));
                    } else if (keyGenerator != null) {
                        restrictionList.add(new PrimaryKeyRestriction(attribute, keyGenerator));
                    } else {
                        throw new RuntimeException("Attribute must have restriction");
                    }
                }
            }
        }
        return new Restrictions(restrictionList);
    }

    private RangeSet<Long> getIntegerRangeSet(String table, String attribute) {
        RangeSet<Long> rangeSet = TreeRangeSet.create();
        rangeSet.add(Range.all());
        String minValue = getMinValue(table, attribute);
        String maxValue = getMaxValue(table, attribute);
        if (minValue != null) {
            rangeSet.add(Range.downTo(Long.valueOf(minValue), BoundType.CLOSED));
        }
        if (maxValue != null) {
            rangeSet.add(Range.upTo(Long.valueOf(maxValue), BoundType.CLOSED));
        }
        return rangeSet;
    }

    private void validate(String fileName) throws IOException, SAXException {
        Source xmlFile = new StreamSource(new File(fileName));
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(getClass().getResource(schemaLocation));
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);

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
        XPathExpression expr = getXPathExpression("//TABLE/ROWS_NUM/text()[not(. < //TABLE/ROWS_NUM/text())][1]");
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

    public AttributeType getType(String table, String attribute) {
        try {
            return AttributeType.valueOf(getAttributeProperty(table, attribute, "TYPE"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isPrimaryKey(String table, String attribute) {
        return "true".equals(getAttributeProperty(table, attribute, "PRIMARY_KEY"));
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
