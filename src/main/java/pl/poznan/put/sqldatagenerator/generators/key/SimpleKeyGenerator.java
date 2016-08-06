package pl.poznan.put.sqldatagenerator.generators.key;

public class SimpleKeyGenerator extends KeyGenerator {
    private long current;

    public SimpleKeyGenerator(long max) {
        super(max);
    }

    @Override
    public long getNextValue() {
        return current++;
    }
}
