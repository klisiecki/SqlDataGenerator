package pl.poznan.put.sqldatagenerator.generator;


import pl.poznan.put.sqldatagenerator.restriction.StringRestriction;

public class StringAttribute extends Attribute {
    private String value;

    public StringAttribute(String name) {
        super(name, false);
        this.restriction = new StringRestriction(true);
        this.negativeRestriction = new StringRestriction(true);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        setClear(false);
        this.value = value;
    }

    @Override
    protected void calculateValue() {
        //TODO handle generating strings
        setValue("random string");
    }

    @Override
    protected void generateFromRestriction(boolean negative) {
        StringRestriction stringRestriction = negative ? (StringRestriction) getNegativeRestriction() : (StringRestriction) getRestriction();
        setValue(RandomGenerator.getString(stringRestriction.getRangeSet()));
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
