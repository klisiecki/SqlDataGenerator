package pl.poznan.put.sqldatagenerator.restriction.types;

import pl.poznan.put.sqldatagenerator.exception.InvalidInternalStateException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generators.key.KeyGenerator;

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
        throw new InvalidInternalStateException("Cannot reverse primary key restriction");
    }

    @Override
    public Restriction clone() {
        throw new InvalidInternalStateException("Cannot clone primary key restriction");
    }

    @Override
    public String toString() {
        return "PrimaryKeyRestriction{" + getAttribute() + "}";
    }
}
