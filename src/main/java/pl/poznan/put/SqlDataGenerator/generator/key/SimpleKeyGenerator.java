package pl.poznan.put.SqlDataGenerator.generator.key;

public class SimpleKeyGenerator extends KeyGenerator {
    protected long current;


    public SimpleKeyGenerator(long max) {
        super(max);
    }

    @Override
    public long getNextValue() {
        return current++;
    }
}
