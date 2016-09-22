package pl.poznan.put.sqldatagenerator.generators.key;

public class SequenceKeyGenerator implements KeyGenerator {
    private long current;

    public SequenceKeyGenerator() {
    }

    @Override
    public long getNextValue() {
        return current++;
    }
}
