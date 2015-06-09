package pl.poznan.put.SqlDataGenerator.restriction;


import java.util.List;

public class IntegerRestriction extends Restriction {
    private Integer minValue;
    private Integer maxValue;
    private List<Integer> values;

    public IntegerRestriction() {
        this.minValue = Integer.MIN_VALUE/2;
        this.maxValue = Integer.MAX_VALUE/2;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        if (minValue != null) {
            this.minValue = Math.max(this.minValue, minValue);
        }
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        if (maxValue != null) {
            this.maxValue = Math.min(this.maxValue, maxValue);
        }
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        if (this.values == null) {
            this.values = values;
        } else {
            //TODO
        }
    }


    @Override
    public void merge(Restriction restriction) {
        setMinValue(((IntegerRestriction) restriction).getMinValue());
        setMaxValue(((IntegerRestriction) restriction).getMaxValue());
        setValues(((IntegerRestriction) restriction).getValues());
    }
}
