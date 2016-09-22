package pl.poznan.put.sqldatagenerator.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
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
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AbstractXMLReader {
    private static final Logger logger = LoggerFactory.getLogger(AbstractXMLReader.class);

    private final XPathFactory xPathfactory = XPathFactory.newInstance();
    protected Document document;

    protected AbstractXMLReader(String fileName, String schemaLocation) throws ParserConfigurationException,
            IOException, SAXException {
        validateXML(fileName, getClass().getResource(schemaLocation));
        logger.info("{} is valid", fileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(fileName);
    }

    private void validateXML(String fileName, URL schemaUrl) throws IOException, SAXException {
        Source xmlFile = new StreamSource(new File(fileName));
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaUrl);
        Validator validator = schema.newValidator();
        try {
            validator.validate(xmlFile);
        } catch (SAXException e) {
            throw new XMLNotValidException(e.getMessage());
        }
    }

    protected XPathExpression getXPathExpression(String expr) {
        XPath xpath = xPathfactory.newXPath();
        try {
            return xpath.compile(expr);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected Double getDoubleFromExpression(XPathExpression expr) {
        Double result;
        try {
            result = Double.parseDouble(expr.evaluate(document));
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    protected String getStringFromExpression(XPathExpression expr) {
        String result;
        try {
            result = expr.evaluate(document);
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    protected List<String> getList(XPathExpression expr) {
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
}
