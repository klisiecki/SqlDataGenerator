package pl.poznan.put.sqldatagenerator.generator.datatypes;

public enum DatabaseType {
    VARCHAR(InternalType.STRING),
    DATETIME(InternalType.LONG),
    INTEGER(InternalType.LONG),
    FLOAT(InternalType.DOUBLE);

    private final InternalType internalType;

    DatabaseType(InternalType internalType) {
        this.internalType = internalType;
    }

    public InternalType getInternalType() {
        return internalType;
    }


}
