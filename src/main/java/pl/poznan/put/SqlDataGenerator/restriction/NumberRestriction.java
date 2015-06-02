package pl.poznan.put.SqlDataGenerator.restriction;


import java.util.List;

public class NumberRestriction<T> extends Restriction {
    private T minValue;
    private T maxValue;
    private List<T> values;

    public T getMinValue() {
        return minValue;
    }

    public void setMinValue(T minValue) {
        this.minValue = minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(T maxValue) {
        this.maxValue = maxValue;
    }

    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }



}
