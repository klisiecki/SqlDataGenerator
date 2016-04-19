package pl.poznan.put.sqldatagenerator.generator;

import pl.poznan.put.sqldatagenerator.generator.key.KeyGenerator;
import pl.poznan.put.sqldatagenerator.generator.key.SimpleKeyGenerator;
import pl.poznan.put.sqldatagenerator.restriction.OldRestriction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class Attribute {
    private final String name;
    private boolean clear;
    private final boolean isPrimaryKey;
    private KeyGenerator keyGenerator;
    protected final List<Attribute> dependentAttributes;
    protected final List<Attribute> equalsAttributes;
    protected OldRestriction restriction;
    protected OldRestriction negativeRestriction;
    private Boolean canBeNegative;

    public Attribute(String name, boolean isPrimaryKey) {
        this.name = name;
        this.isPrimaryKey = isPrimaryKey;
        this.dependentAttributes = new ArrayList<>();
        this.equalsAttributes = new ArrayList<>();
        setClear(true);
    }

    public Attribute(String name, boolean isPrimaryKey, long dataRows) {
        this(name, isPrimaryKey);
        if (isPrimaryKey) {
            keyGenerator = new SimpleKeyGenerator(dataRows);
        }
    }

    public void addDependent(Attribute attribute) {
        if (!dependentAttributes.contains(attribute)) {
            dependentAttributes.add(attribute);
        }
    }

    public void addEquals(Collection<Attribute> attributes) {
        attributes.forEach(this::addEquals);
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * @return true if attribute can be used to generate non-matching query row
     */
    public boolean canBeNegative() {
        if (canBeNegative == null) {
            canBeNegative = !restriction.equals(negativeRestriction);
        }
        return canBeNegative;
    }

    public void addEquals(Attribute attribute) {
        if (!equalsAttributes.contains(attribute) && !attribute.equals(this)) {
            equalsAttributes.add(attribute);
            addDependent(attribute);
            restriction.addAndRangeSet(attribute.getRestriction().getRangeSet());
        }
    }

    public List<Attribute> getEqualsAttributes() {
        return equalsAttributes;
    }

    public void collectEquals(Set<Attribute> result) {
        result.add(this);
        equalsAttributes.stream().filter(a -> !result.contains(a))
                .forEach(a -> a.collectEquals(result));
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

    private void rollback() {
        if (isPrimaryKey) {
            throw new RuntimeException("Rollback on primary key");
        }
        setClear(true);
        dependentAttributes.stream().filter(a -> !a.isClear()).forEach(Attribute::rollback);
    }

    public boolean generateValue(boolean negative) {
        if (!isClear()) {
            return true;
        }
        if (isPrimaryKey) {
            setObjectValue(keyGenerator.getNextValue());
        } else if (!generateFromEquals()) {
            if (!generateFromRestrictionAndDependent(negative && canBeNegative())) {
                rollback();
                return false;
            }
        }
        for (Attribute attribute : dependentAttributes) {
            if (!attribute.generateValue(negative)) {
                return false;
            }
        }
        return true;
    }

    public OldRestriction getRestriction() {
        return restriction;
    }

    public OldRestriction getNegativeRestriction() {
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

    protected abstract void calculateValue();

    protected abstract Object getObjectValue();

    protected abstract void setObjectValue(Object value);

    protected boolean generateFromRestrictionAndDependent(boolean negative) {
        boolean foundClear = false;
        for (Attribute a : dependentAttributes) {
            if (a.isClear()) {
                foundClear = true;
                break;
            }
        }
        if (foundClear || dependentAttributes.size() == 0) {
            generateFromRestriction(negative);
            return true;
        } else { //last attribute from clique
            calculateValue();
            return true;
        }
    }

    protected abstract void generateFromRestriction(boolean negative);

    @Override
    public String toString() {
        return "{" + name + ": " + getObjectValue() + "}";
    }
}
