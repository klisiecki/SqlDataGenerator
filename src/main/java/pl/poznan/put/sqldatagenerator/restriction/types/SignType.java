package pl.poznan.put.sqldatagenerator.restriction.types;

public enum SignType {
    GREATER_THAN {
        @Override
        SignType reversed() {
            return MINOR_THAN;
        }
    },
    MINOR_THAN {
        @Override
        SignType reversed() {
            return GREATER_THAN;
        }
    },
    EQUALS {
        @Override
        SignType reversed() {
            return NOT_EQUALS;
        }
    },
    NOT_EQUALS {
        @Override
        SignType reversed() {
            return EQUALS;
        }
    };

    abstract SignType reversed();
}
