package pl.poznan.put.sqldatagenerator.restriction.types;

import pl.poznan.put.sqldatagenerator.exception.InvalidInteralStateException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.key.KeyGenerator;

public class PrimaryKeyRestriction extends OneAttributeRestriction {

    private final KeyGenerator keyGenerator;

    public PrimaryKeyRestriction(Attribute attribute, KeyGenerator keyGenerator) {
        super(attribute);
        this.keyGenerator = keyGenerator;
    }

    public Long getNextValue() {
        return keyGenerator.getNextValue();
    }

    @Override
    public Restriction reverse() {
        throw new InvalidInteralStateException("Cannot reverse primary key restriction");
    }

    @Override
    public Restriction clone() {
        throw new InvalidInteralStateException("Cannot clone primary key restriction");
    }

    @Override
    public String toString() {
        return "PrimaryKeyRestriction{" + getAttribute() + "}";
    }
}
