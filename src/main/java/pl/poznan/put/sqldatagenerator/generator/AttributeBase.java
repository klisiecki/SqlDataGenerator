package pl.poznan.put.sqldatagenerator.generator;

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
