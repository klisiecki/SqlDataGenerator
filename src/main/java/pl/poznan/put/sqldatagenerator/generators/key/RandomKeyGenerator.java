package pl.poznan.put.sqldatagenerator.generators.key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

public class RandomKeyGenerator implements KeyGenerator {
    private int index;
    private final Integer[] keys;

    public RandomKeyGenerator(long max) {
        index = 0;
        List<Integer> keys = IntStream.range(0, (int) max).boxed().collect(toCollection(ArrayList::new));
        Collections.shuffle(keys);
        this.keys = keys.toArray(new Integer[keys.size()]);
    }

    @Override
    public long getNextValue() {
        return keys[index++];
    }
}
