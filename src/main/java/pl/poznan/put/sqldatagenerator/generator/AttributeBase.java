package pl.poznan.put.sqldatagenerator.generator;

import pl.poznan.put.sqldatagenerator.generator.key.KeyGenerator;

public class AttributeBase {
    // Czy wystarczy wspólny jeden typ?
    private String value;
    private AttributeTypes type;

    private boolean isPrimaryKey;

    private KeyGenerator keyGenerator;
    public AttributeBase(AttributeTypes type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AttributeTypes getType() {
        return type;
    }
}
