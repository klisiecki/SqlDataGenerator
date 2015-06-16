package pl.poznan.put.SqlDataGenerator.generator;


import pl.poznan.put.SqlDataGenerator.restriction.StringRestriction;

public class StringAttribute extends Attribute {
    private String value;

    public StringAttribute(String name) {
        super(name);
        this.restriction = new StringRestriction();
        this.negativeRestriction = new StringRestriction();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        setClear(false);
        this.value = value;
    }

    @Override
    protected boolean generateFromRestrictionAndDependent(boolean negative) {
        this.value = "all";
        return false;
    }

    @Override
    protected void generateFromRestriction(boolean negative) {
        this.value = "restriction";
    }

    @Override
    protected Object getObjectValue() {
        return getValue();
    }

    @Override
    protected void setObjectValue(Object value) {
        setValue((String) value);
    }
}
