package pl.poznan.put.sqldatagenerator.restriction.types;

import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.key.KeyGenerator;

public class PrimaryKeyRestriction extends OneAttributeRestriction {

    private KeyGenerator keyGenerator;

    public PrimaryKeyRestriction(Attribute attribute, KeyGenerator keyGenerator) {
        super(attribute);
        this.keyGenerator = keyGenerator;
    }

    public Long getNextValue() {
        return keyGenerator.getNextValue();
    }

    @Override
    public Restriction reverse() {
        throw new RuntimeException("Cannot reverse primary key restriction");
    }
}
