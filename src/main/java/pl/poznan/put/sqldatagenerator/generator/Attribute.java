package pl.poznan.put.sqldatagenerator.generator;

public class Attribute {

    private final String name;
    private final AttributeType type;
    private Attribute baseAttribute;
    private final TableInstance tableInstance;

    private boolean isClear;

    public Attribute(TableInstance tableInstance, String name, AttributeType type) {
        this.name = name;
        this.tableInstance = tableInstance;
        this.type = type;
        this.isClear = true;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        if (baseAttribute != null) {
            return baseAttribute.getValue();
        }
        return tableInstance.getState().getValue(name);
    }

    public void setValue(String value) {
        if (baseAttribute != null) {
            throw new RuntimeException("Attempt to set value of dependent attribute");
        }
        if (!isClear) {
            throw new RuntimeException("Value for attribute '" + name + "' already set");
        }
        isClear = false;
        tableInstance.getState().setValue(name, value);
    }

    public AttributeType getType() {
        return type;
    }

    public String getBaseTableName() {
        return tableInstance.getBase().getName();
    }

    public boolean canBeGenerated() {
        return baseAttribute == null && isClear;
    }

    public void setClear(boolean isClear) {
        if (baseAttribute == null) {
            this.isClear = isClear;
        }
    }

    public void setBaseAttribute(Attribute baseAttribute) {
        if (this.baseAttribute != null) {
            throw new RuntimeException("Base attribute already set");
        }
        if (type != baseAttribute.getType()) {
            throw new RuntimeException("Dependent attribute must have the same type as base attribute");
        }
        this.baseAttribute = baseAttribute;
    }

    @Override
    public String toString() {
        return tableInstance.getAliasName() + "." + name;
    }
}
