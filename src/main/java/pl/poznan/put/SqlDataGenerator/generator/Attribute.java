package pl.poznan.put.SqlDataGenerator.generator;

public class Attribute<T> {
    private String name;
    private T value;
    private boolean clear;

    public Attribute(String name) {
        this.name = name;
        clear();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        clear = false;
    }

    public boolean isClear() {
        return clear;
    }

    public void clear() {
        clear = true;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", clear=" + clear +
                '}';
    }
}
