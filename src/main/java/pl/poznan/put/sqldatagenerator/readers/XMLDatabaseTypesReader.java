package pl.poznan.put.sqldatagenerator.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import java.io.IOException;

public class XMLDatabaseTypesReader extends AbstractXMLReader implements DatabaseTypesReader {
    private static final Logger logger = LoggerFactory.getLogger(XMLDatabaseTypesReader.class);

    public XMLDatabaseTypesReader(String fileName) throws ParserConfigurationException, IOException, SAXException {
        super(fileName, "/datatypeSchema.xsd");
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
    public String getMinValue(String type) {
        String rootType = findRoot(type);
        XPathExpression expr = getXPathExpression(
                String.format("//DATATYPE[NAME/text()='%s']/MIN_VALUE/text()", rootType));
        return getStringFromExpression(expr);
    }

    @Override
    public String getMaxValue(String type) {
        String rootType = findRoot(type);
        XPathExpression expr = getXPathExpression(
                String.format("//DATATYPE[NAME/text()='%s']/MAX_VALUE/text()", rootType));
        return getStringFromExpression(expr);
    }

}
