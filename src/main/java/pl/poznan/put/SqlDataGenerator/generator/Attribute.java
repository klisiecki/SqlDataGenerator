package pl.poznan.put.SqlDataGenerator.generator;

import pl.poznan.put.SqlDataGenerator.restriction.Restriction;

import java.util.ArrayList;
import java.util.List;

public abstract class Attribute {

    private String name;
    private boolean clear;
    private List<Attribute> dependentAttributes;
    protected Restriction restriction;
    protected Restriction negativeRestriction;

    public Attribute(String name) {
        this.name = name;
        this.dependentAttributes = new ArrayList<>();
        clear();
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

    public boolean generateValue() {
        if (!isClear()) {
            return true;
        }
        boolean foundClear = false;
        for (Attribute a: dependentAttributes) {
            if (a.isClear()) {
                foundClear = true;
                break;
            }
        }
        if (foundClear) {
            generateFromRestriction();
            for (Attribute a: dependentAttributes) {
                if(!a.generateValue()) {
                    return false;
                }
            }
            return true;
        } else {
            return generateFromRestrictionAndDependent();
        }
    }

    public Restriction getRestriction() {
        return restriction;
    }

    public Restriction getNegativeRestriction() {
        return negativeRestriction;
    }

    protected abstract boolean generateFromRestrictionAndDependent();

    protected abstract void generateFromRestriction();
}
