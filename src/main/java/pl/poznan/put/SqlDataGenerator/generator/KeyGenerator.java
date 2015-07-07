package pl.poznan.put.SqlDataGenerator.generator;

public class KeyGenerator {
    private long max;
    private long current;

    public KeyGenerator(long max) {
        this.max = max;
        this.current = 0;
    }

    public long getValue() {
        //TODO jakiś algorytm, który będzie generował klucze w innej kolejności (wymieszanie danych)
        return current++;
    }
}
