package pl.poznan.put.sqldatagenerator.generator;


import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;

import java.util.Random;

public class RandomGenerator {
    public static Integer getInteger(int from, int to) {
        if (from == to) return from;
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
        int length = getInteger(from.length(), to.length());
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(getChar('A', 'z')); //TODO change to length ranges
        }

        return stringBuilder.toString();
    }

    public static String getString(TreeRangeSet rangeSet) {
        Object[] ranges = rangeSet.asRanges().toArray();
        int i = ranges.length == 1 ? 0 : getInteger(0, ranges.length);
        Range range = (Range) ranges[i];
        return getString(range.lowerEndpoint().toString(), range.upperEndpoint().toString());

    }
}
