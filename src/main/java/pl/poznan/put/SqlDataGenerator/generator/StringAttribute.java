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
        this.value = value;
    }

    @Override
    protected boolean generateFromRestrictionAndDependent() {
        this.value = "all";
        return false;
    }

    @Override
    protected void generateFromRestriction() {
        this.value = "restriction";
    }
}
