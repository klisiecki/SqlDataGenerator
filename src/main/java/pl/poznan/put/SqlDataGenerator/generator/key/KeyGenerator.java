package pl.poznan.put.SqlDataGenerator.generator.key;

public abstract class KeyGenerator {
    protected final long max;

    public KeyGenerator(long max) {
        this.max = max;
    }

    public abstract long getNextValue();
}
