package pl.poznan.put.sqldatagenerator.generator;

/**
 * Class representing an abstract concept of attribute in SQL query.
 * one {@link AttributeBase} may refer to multiple attributes used in SQL query when they have the same type
 * and there is equals operator between them.
 */
public class AttributeBase {

    private String value;
    private AttributeType type;
    private boolean isClear;

    public AttributeBase(AttributeType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AttributeType getType() {
        return type;
    }

    public boolean isClear() {
        return isClear;
    }

    public void clear() {
        isClear = true;
    }
}
