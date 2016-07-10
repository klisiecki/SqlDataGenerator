package pl.poznan.put.sqldatagenerator.generator.datatypes;

public class DatabaseType {
    public enum Type {
        VARCHAR(InternalType.STRING),
        DATETIME(InternalType.LONG),
        INTEGER(InternalType.LONG),
        FLOAT(InternalType.DOUBLE);

        private final InternalType internalType;

        Type(InternalType internalType) {
            this.internalType = internalType;
        }

        public InternalType getInternalType() {
            return internalType;
        }
    }

    private final Type type;
    private final Integer scale;

    public DatabaseType(Type type, Integer scale) {
        this.type = type;
        this.scale = scale;
    }

    public Type getType() {
        return type;
    }

    public InternalType getInternalType() {
        return type.getInternalType();
    }
}
