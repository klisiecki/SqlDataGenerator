package pl.poznan.put.SqlDataGenerator.generator;

import pl.poznan.put.SqlDataGenerator.restriction.Restriction;

import java.util.*;

public abstract class Attribute {

    private String name;
    private boolean clear;
    private boolean isPrimaryKey;
    private KeyGenerator keyGenerator;
    protected List<Attribute> dependentAttributes;
    protected List<Attribute> equalsAttributes;
    protected Restriction restriction;
    protected Restriction negativeRestriction;
    private Boolean canBeNegative;

    public Attribute(String name, boolean isPrimaryKey) {
        this.name = name;
        this.isPrimaryKey = isPrimaryKey;
        if (isPrimaryKey) {
            keyGenerator = new KeyGenerator(Integer.MAX_VALUE); //brać z góry: przekazać albo odwołanie do tabeli
        }
        this.dependentAttributes = new ArrayList<>();
        this.equalsAttributes = new ArrayList<>();
        setClear(true);
    }

    public void addDependent(Attribute attribute) {
        if (!dependentAttributes.contains(attribute)) {
            dependentAttributes.add(attribute);
        }
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * @return czy można użyć atrybutu do wygenerowania wiersza niespełniającego warunktów zapytania
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

    public void addEquals(Collection<Attribute> attributes) {
        for (Attribute a : attributes) {
            addEquals(a);
        }
    }

    public List<Attribute> getEqualsAttributes() {
        return equalsAttributes;
    }

    public void collectEquals(Set<Attribute> result) {
        result.add(this);
        for (Attribute a : equalsAttributes) {
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

    private void rollback() {
        if (isPrimaryKey) {
            throw new RuntimeException("Rollback on primary key");
        }
        setClear(true);
        for (Attribute a : dependentAttributes) {
            if (!a.isClear()) {
                a.rollback();
            }
        }
    }

    public boolean generateValue(boolean negative) {
        if (!isClear()) {
            return true;
        }
        if (isPrimaryKey) {
            setObjectValue(keyGenerator.getValue());
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
        } else { //ostatni z kliki i trzeba wyliczyć jego wartość
            calculateValue();
            return true; //TODO obsługa wyrażeń matematycznych
        }
    }

    protected abstract void generateFromRestriction(boolean negative);

    @Override
    public String toString() {
        return "{" + name + ": " + getObjectValue() + "}";
    }
}
