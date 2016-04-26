package pl.poznan.put.sqldatagenerator.generator;

/**
 * Class representing single occurrence of attribute in SQL query.
 */
@Deprecated
public class AttributeInstance {
    private final AttributeBase base;

    private final String name;

    public AttributeInstance(AttributeBase base, String name) {
        this.base = base;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return base.getValue();
    }

    public void clear() {
        base.clear();
    }
}
