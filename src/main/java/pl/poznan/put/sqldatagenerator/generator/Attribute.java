package pl.poznan.put.sqldatagenerator.generator;

public class Attribute {

    private final String name;
    private final String fullyQualifiedName;
    private String value;
    private final AttributeType type;
    private boolean isClear;
    private Attribute baseAttribute;


    public Attribute(String tableName, String name, AttributeType type) {
        this.name = name;
        this.fullyQualifiedName = tableName + "." + name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        if (baseAttribute != null) {
            return baseAttribute.getValue();
        }
        return value;
    }

    public void setValue(String value) {
        if (baseAttribute != null) {
            throw new RuntimeException("Attempt to set value of dependent attribute");
        }
        if (!isClear) {
            throw new RuntimeException("Value for attribute '" + name + "' already set");
        }
        isClear = false;
        this.value = value;
    }

    public AttributeType getType() {
        return type;
    }

    public boolean isClear() {
        return isClear;
    }

    public void clear() {
        if (baseAttribute == null) {
            isClear = true;
            value = null;
        }
    }

    public void setBaseAttribute(Attribute baseAttribute) {
        if (type != baseAttribute.getType()) {
            throw new RuntimeException("Dependent attribute must have the same type as base attribute");
        }
        this.baseAttribute = baseAttribute;
    }

    @Override
    public String toString() {
        return fullyQualifiedName;
    }
}
