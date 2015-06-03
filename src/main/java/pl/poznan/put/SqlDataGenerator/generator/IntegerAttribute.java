package pl.poznan.put.SqlDataGenerator.generator;

import pl.poznan.put.SqlDataGenerator.restriction.NumberRestriction;

public class IntegerAttribute extends Attribute {

    private Integer value;

    public IntegerAttribute(String name) {
        super(name);
        this.restriction = new NumberRestriction<Integer>();
        this.negativeRestriction = new NumberRestriction<Integer>();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    protected boolean generateFromRestrictionAndDependent() {
        this.value = 4;
        return true;
    }

    @Override
    protected void generateFromRestriction() {
        this.value = 6;
    }
}
