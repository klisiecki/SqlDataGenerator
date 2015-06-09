package pl.poznan.put.SqlDataGenerator.generator;

import pl.poznan.put.SqlDataGenerator.restriction.Restriction;

import java.util.*;

public abstract class Attribute {

    private String name;
    private boolean clear;
    protected List<Attribute> dependentAttributes;
    protected List<Attribute> equalsAttributes;
    protected Restriction restriction;
    protected Restriction negativeRestriction;

    public Attribute(String name) {
        this.name = name;
        this.dependentAttributes = new ArrayList<>();
        this.equalsAttributes = new ArrayList<>();
        setClear(true);
    }

    public void addDependent(Attribute attribute) {
        if (!dependentAttributes.contains(attribute)) {
            dependentAttributes.add(attribute);
        }
    }

    public void addEquals(Attribute attribute) {
        if (!equalsAttributes.contains(attribute) && !attribute.equals(this)) {
            equalsAttributes.add(attribute);
            addDependent(attribute);
            restriction.merge(attribute.getRestriction());
        }
    }

    public void addEquals(Collection<Attribute> attributes) {
        for (Attribute a: attributes) {
            addEquals(a);
        }
    }

    public List<Attribute> getEqualsAttributes() {
        return equalsAttributes;
    }

    public void collectEquals(Set<Attribute> result) {
        result.add(this);
        for (Attribute a: equalsAttributes) {
            if (!result.contains(a)) {
                a.collectEquals(result);
            }
        }
    }

    public String getName() {
        return name;
    }

    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean isClear) {
        this.clear = isClear;
    }

    public void reset() {
        setClear(true);
        for (Attribute a : dependentAttributes) {
            if (!a.isClear()) {
                a.reset();
            }
        }
    }

    public boolean generateValue() {
        if (!isClear()) {
            return true;
        }
        if (!generateFromEquals()) {
            if (!generateFromRestrictionAndDependent()) {
                reset();
                return false;
            }
        }
        for (Attribute attribute : dependentAttributes) {
            if (!attribute.generateValue()) {
                return false;
            }
        }
        return true;
    }

    public Restriction getRestriction() {
        return restriction;
    }

    public Restriction getNegativeRestriction() {
        return negativeRestriction;
    }

    private boolean generateFromEquals() {
        for (Attribute attribute : equalsAttributes) {
            if (!attribute.isClear()) {
                setObjectValue(attribute.getObjectValue());
                return true;
            }
        }
        return false;
    }

    protected abstract Object getObjectValue();

    protected abstract void setObjectValue(Object value);

    protected abstract boolean generateFromRestrictionAndDependent();

    protected abstract void generateFromRestriction();

    @Override
    public String toString() {
        return "{"+name + ": "+getObjectValue()+"}";
    }
}
