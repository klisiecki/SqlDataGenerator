package pl.poznan.put.sqldatagenerator.generator;

import pl.poznan.put.sqldatagenerator.generator.key.KeyGenerator;
import pl.poznan.put.sqldatagenerator.generator.key.SimpleKeyGenerator;
import pl.poznan.put.sqldatagenerator.restriction.OldRestriction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Deprecated
public abstract class OldAttribute {
    private final String name;
    private boolean clear;
    private final boolean isPrimaryKey;
    private KeyGenerator keyGenerator;
    protected final List<OldAttribute> dependentOldAttributes;
    protected final List<OldAttribute> equalsOldAttributes;
    protected OldRestriction restriction;
    protected OldRestriction negativeRestriction;
    private Boolean canBeNegative;

    public OldAttribute(String name, boolean isPrimaryKey) {
        this.name = name;
        this.isPrimaryKey = isPrimaryKey;
        this.dependentOldAttributes = new ArrayList<>();
        this.equalsOldAttributes = new ArrayList<>();
        setClear(true);
    }

    public OldAttribute(String name, boolean isPrimaryKey, long dataRows) {
        this(name, isPrimaryKey);
        if (isPrimaryKey) {
            keyGenerator = new SimpleKeyGenerator(dataRows);
        }
    }

    public void addDependent(OldAttribute oldAttribute) {
        if (!dependentOldAttributes.contains(oldAttribute)) {
            dependentOldAttributes.add(oldAttribute);
        }
    }

    public void addEquals(Collection<OldAttribute> oldAttributes) {
        oldAttributes.forEach(this::addEquals);
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

    public void addEquals(OldAttribute oldAttribute) {
        if (!equalsOldAttributes.contains(oldAttribute) && !oldAttribute.equals(this)) {
            equalsOldAttributes.add(oldAttribute);
            addDependent(oldAttribute);
            restriction.addAndRangeSet(oldAttribute.getRestriction().getRangeSet());
        }
    }

    public List<OldAttribute> getEqualsOldAttributes() {
        return equalsOldAttributes;
    }

    public void collectEquals(Set<OldAttribute> result) {
        result.add(this);
        equalsOldAttributes.stream().filter(a -> !result.contains(a))
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
        dependentOldAttributes.stream().filter(a -> !a.isClear()).forEach(OldAttribute::rollback);
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
        for (OldAttribute oldAttribute : dependentOldAttributes) {
            if (!oldAttribute.generateValue(negative)) {
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
        for (OldAttribute oldAttribute : equalsOldAttributes) {
            if (!oldAttribute.isClear()) {
                setObjectValue(oldAttribute.getObjectValue());
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
        for (OldAttribute a : dependentOldAttributes) {
            if (a.isClear()) {
                foundClear = true;
                break;
            }
        }
        if (foundClear || dependentOldAttributes.size() == 0) {
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
