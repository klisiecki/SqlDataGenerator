package pl.poznan.put.sqldatagenerator.generator;


import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;

import java.util.Random;

public class RandomGenerator {

    //TODO if (from == Long.MIN_VALUE && to == Long.MAX_VALUE)
    public static long getLong(long from, long to) {
        if (from == to) return from;
        Random rand = new Random();
        if (from == Long.MIN_VALUE && to == Long.MAX_VALUE) {
            return rand.nextLong();
        }

        long max = to - from;
        long randomNum = (long) (rand.nextDouble() * max);
        return randomNum + from;
    }

    //TODO handle inclusive and exclusive bounds
    public static long getLong(TreeRangeSet rangeSet) {
        Object[] ranges = rangeSet.asRanges().toArray();
        int i = ranges.length == 1 ? 0 : (int) getLong(0, ranges.length);
        Range range = (Range) ranges[i];
        return getLong((Long) range.lowerEndpoint(), (Long) range.upperEndpoint());
    }

    public static char getChar(char from, char to) {
        Random r = new Random();
        int offset = 0;
        if (from <= 90 && to >= 97) offset = 6;
        int i = r.nextInt(to - from - offset) + from;
        if (offset != 0 && i >= 91) {
            i += offset;
        }
        return (char) i;
    }

    public static String getString(String from, String to) {
        if (from.equals(to)) return from;
        int length = (int) getLong(from.length(), to.length());
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(getChar('A', 'z')); //TODO change to length ranges
        }

        return stringBuilder.toString();
    }

    public static String getString(TreeRangeSet rangeSet) {
        Object[] ranges = rangeSet.asRanges().toArray();
        int i = ranges.length == 1 ? 0 : (int) getLong(0, ranges.length);
        Range range = (Range) ranges[i];
        return getString(range.lowerEndpoint().toString(), range.upperEndpoint().toString());

    }
}
