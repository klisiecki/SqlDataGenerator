package pl.poznan.put.sqldatagenerator.generator;

import pl.poznan.put.sqldatagenerator.restriction.IntegerOldRestriction;

public class IntegerAttribute extends Attribute {
    private Integer value;

    public IntegerAttribute(String name, boolean isPrimaryKey, long dataRows) {
        super(name, isPrimaryKey, dataRows);
        this.restriction = new IntegerOldRestriction(true);
        this.negativeRestriction = new IntegerOldRestriction(true);
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
        //TODO handle calculating integer values
        setValue(4); //chosen by dice roll
    }

    @Override
    protected void generateFromRestriction(boolean negative) {
        IntegerOldRestriction integerRestriction = negative ? (IntegerOldRestriction) getNegativeRestriction() : (IntegerOldRestriction) getRestriction();
        setValue(RandomGenerator.getInteger(integerRestriction.getRangeSet()));
    }

    @Override
    protected Object getObjectValue() {
        return getValue();
    }

    @Override
    protected void setObjectValue(Object value) {
        if (value instanceof Long) {
            setValue(((Long) value).intValue()); //TODO consider changing to long?
        } else {
            setValue((Integer) value);
        }
    }
}
