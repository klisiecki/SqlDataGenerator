package pl.poznan.put.sqldatagenerator.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import pl.poznan.put.sqldatagenerator.exception.XMLNotValidException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

public class XMLDatabaseTypesReader implements DatabaseTypesReader {
    private static final Logger logger = LoggerFactory.getLogger(XMLDatabaseTypesReader.class);

    private final static String schemaLocation = "/datatypeSchema.xsd";
    private final XPathFactory xPathfactory = XPathFactory.newInstance();
    private Document document;

    public XMLDatabaseTypesReader(String fileName) throws ParserConfigurationException, IOException, SAXException {
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
    public String getBaseType(String type) {
        String rootType = findRoot(type);

        XPathExpression expr = getXPathExpression(
                String.format("//DATATYPE[NAME/text()='%s']/BASETYPE/text()", rootType));
        return getStringFromExpression(expr);
    }

    private String findRoot(String type) {
        XPathExpression expr = getXPathExpression(
                String.format("//DATATYPE[NAME/text()='%s']/ALIAS_FOR/text()", type));
        String aliasFor = getStringFromExpression(expr);
        return !"".equals(aliasFor) ? findRoot(aliasFor) : type;
    }

    @Override
    public Double getMinValue(String type) {
        String rootType = findRoot(type);

        XPathExpression expr = getXPathExpression(
                String.format("//DATATYPE[NAME/text()='%s']/MIN_VALUE/text()", rootType));
        return getDoubleFromExpression(expr);
    }

    @Override
    public Double getMaxValue(String type) {
        String rootType = findRoot(type);

        XPathExpression expr = getXPathExpression(
                String.format("//DATATYPE[NAME/text()='%s']/MAX_VALUE/text()", rootType));
        return getDoubleFromExpression(expr);
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

    private Double getDoubleFromExpression(XPathExpression expr) {
        Double result;
        try {
            result = Double.parseDouble(expr.evaluate(document));
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    private String getStringFromExpression(XPathExpression expr) {
        String result;
        try {
            result = expr.evaluate(document);
        } catch (Exception e) {
            return null;
        }
        return result;
    }
}
