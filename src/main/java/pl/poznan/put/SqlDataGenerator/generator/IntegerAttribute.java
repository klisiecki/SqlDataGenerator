package pl.poznan.put.SqlDataGenerator.generator;

import pl.poznan.put.SqlDataGenerator.restriction.IntegerRestriction;

public class IntegerAttribute extends Attribute {

    private Integer value;

    public IntegerAttribute(String name) {
        super(name);
        this.restriction = new IntegerRestriction();
        this.negativeRestriction = new IntegerRestriction();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        setClear(false);
        this.value = value;
    }

    @Override
    protected boolean generateFromRestrictionAndDependent() {
        boolean foundClear = false;
        for (Attribute a : dependentAttributes) {
            if (a.isClear()) {
                foundClear = true;
                break;
            }
        }
        if (foundClear || dependentAttributes.size() == 0) {
            generateFromRestriction();
            return true;
        } else {
            setValue(4);
            return true; //TODO
        }
    }

    @Override
    protected void generateFromRestriction() {
        IntegerRestriction integerRestriction = (IntegerRestriction) getRestriction();
        if (integerRestriction.getValues() == null) {
            setValue(RandomGenerator.getInteger(integerRestriction.getMinValue(), integerRestriction.getMaxValue()));
        }
    }

    @Override
    protected Object getObjectValue() {
        return getValue();
    }

    @Override
    protected void setObjectValue(Object value) {
        setValue((Integer) value);
    }
}
