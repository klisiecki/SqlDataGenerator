package pl.poznan.put.SqlDataGenerator.restriction;


import java.util.List;

public class NumberRestriction<T extends Number> extends Restriction {
    private T minValue;
    private T maxValue;
    private List<T> values;

    public NumberRestriction() {
        setMinValue((T) (Integer) (Integer.MIN_VALUE/2));
        setMaxValue((T) (Integer) (Integer.MAX_VALUE/2));
    }

    public T getMinValue() {
        return minValue;
    }

    public void setMinValue(T minValue) {
        if (minValue != null) {
            this.minValue = minValue;
        }
    }

    public T getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(T maxValue) {
        if (maxValue != null) {
            this.maxValue = maxValue;
        }
    }

    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }


}
