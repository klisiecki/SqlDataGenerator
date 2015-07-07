package pl.poznan.put.SqlDataGenerator.generator;

import pl.poznan.put.SqlDataGenerator.restriction.IntegerRestriction;

public class IntegerAttribute extends Attribute {

    private Integer value;

    public IntegerAttribute(String name, boolean isPrimaryKey) {
        super(name, isPrimaryKey);
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
    protected boolean generateFromRestrictionAndDependent(boolean negative) {
        boolean foundClear = false;
        for (Attribute a : dependentAttributes) {
            if (a.isClear()) {
                foundClear = true;
                break;
            }
        }
        if (foundClear || dependentAttributes.size() == 0) {
            generateFromRestriction(negative);
            return true;
        } else {
            setValue(4);
            return true; //TODO
        }
    }

    @Override
    protected void generateFromRestriction(boolean negative) {
        IntegerRestriction integerRestriction = negative ? (IntegerRestriction) getNegativeRestriction(): (IntegerRestriction) getRestriction();
        setValue(RandomGenerator.getInteger(integerRestriction.getRangeSet()));
    }

    @Override
    protected Object getObjectValue() {
        return getValue();
    }

    @Override
    protected void setObjectValue(Object value) {
        if (value instanceof Long) {
            setValue(((Long) value).intValue()); //TODO zmienić wszystko na long?
        } else {
            setValue((Integer) value);
        }
    }
}
