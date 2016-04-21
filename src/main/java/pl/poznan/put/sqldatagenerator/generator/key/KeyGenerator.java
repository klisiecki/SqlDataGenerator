package pl.poznan.put.sqldatagenerator.generator.key;

public abstract class KeyGenerator {
    protected final long max;

    protected KeyGenerator(long max) {
        this.max = max;
    }

    public abstract long getNextValue();
}
