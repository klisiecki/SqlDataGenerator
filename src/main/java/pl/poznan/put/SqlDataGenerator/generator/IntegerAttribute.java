package pl.poznan.put.SqlDataGenerator.generator;

import pl.poznan.put.SqlDataGenerator.restriction.IntegerRestriction;

public class IntegerAttribute extends Attribute {

    private Integer value;

    public IntegerAttribute(String name, boolean isPrimaryKey) {
        super(name, isPrimaryKey);
        this.restriction = new IntegerRestriction(true);
        this.negativeRestriction = new IntegerRestriction(true);
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        setClear(false);
        this.value = value;
    }

    @Override
    protected void calculateValue() {
        setValue(4);
    }

    @Override
    protected void generateFromRestriction(boolean negative) {
        IntegerRestriction integerRestriction = negative ? (IntegerRestriction) getNegativeRestriction() : (IntegerRestriction) getRestriction();
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
