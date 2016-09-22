package pl.poznan.put.sqldatagenerator.generator.datatypes;

public enum InternalType {
    STRING(String.class),
    LONG(Long.class),
    DOUBLE(Double.class);

    private final Class typeClass;

    InternalType(Class typeClass) {
        this.typeClass = typeClass;
    }

    public Class getTypeClass() {
        return typeClass;
    }

    public boolean isNumeric() {
        return Number.class.isAssignableFrom(typeClass);
    }
}
