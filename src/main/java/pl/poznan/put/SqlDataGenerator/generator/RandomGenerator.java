package pl.poznan.put.SqlDataGenerator.generator;


import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.util.Random;
import java.util.Set;

public class RandomGenerator {
    public static Integer getInteger(int from, int to) {
        if (from == to) return from;
        int min = 0;
        int max = to - from;
        Random rand = new Random();
        int randomNum = rand.nextInt(max);
        return randomNum + from;
    }

    public static Integer getInteger(TreeRangeSet rangeSet) {
        Object[] ranges = rangeSet.asRanges().toArray();
        int i = ranges.length == 1 ? 0 : getInteger(0, ranges.length);
        Range range = (Range) ranges[i];
        return getInteger((Integer) range.lowerEndpoint(), (Integer) range.upperEndpoint());
    }
}
