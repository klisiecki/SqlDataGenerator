package pl.poznan.put.SqlDataGenerator.generator;

import pl.poznan.put.SqlDataGenerator.restriction.Restriction;

import java.util.ArrayList;
import java.util.List;

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
//        boolean foundClear = false;
//        for (Attribute a: dependentAttributes) {
//            if (a.isClear()) {
//                foundClear = true;
//                break;
//            }
//        }
//        if (foundClear) {
//            generateFromRestriction();
//            for (Attribute a: dependentAttributes) {
//                if(!a.generateValue()) {
//                    return false;
//                }
//            }
//            return true;
//        } else {
//            return generateFromRestrictionAndDependent();
//        }
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
