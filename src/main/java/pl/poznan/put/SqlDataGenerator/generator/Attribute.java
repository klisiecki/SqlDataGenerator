package pl.poznan.put.SqlDataGenerator.generator;

import pl.poznan.put.SqlDataGenerator.restriction.Restriction;

import java.util.ArrayList;
import java.util.List;

//TODO remove generic, create subclases
public class Attribute<T> {

    private String name;
    private T value;
    private boolean clear;
    private List<Attribute> dependentAttributes;
    private Restriction restriction;
    private Restriction negativeRestriction;

    public Attribute(String name) {
        this.name = name;
        this.dependentAttributes = new ArrayList<>();
        clear();
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        clear = false;
    }

    public boolean isClear() {
        return clear;
    }

    public void clear() {
        clear = true;
    }

    public void reset() {
        clear();
        for (Attribute a: dependentAttributes) {
            if (!a.isClear()) {
                a.reset();
            }
        }
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", clear=" + clear +
                '}';
    }
}
