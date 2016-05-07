package pl.poznan.put.sqldatagenerator.generator;


import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomGenerator {

    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    /**
     * Returns a pseudorandom value from given {@link RangeSet}.
     *
     * @param rangeSet {@link TreeRangeSet} of {@link Long} type. Must not contain empty ranges.
     * @return random {@link Long} value from random {@link Range} in given set.
     */
    public static Long getLong(RangeSet<Long> rangeSet) {
        List<Range<Long>> ranges = new ArrayList<>();
        ranges.addAll(rangeSet.asRanges());
        int i = ranges.size() == 1 ? 0 : random.nextInt(ranges.size());
        Range<Long> range = ranges.get(i);
        long minValue = getMinLong(range);
        long maxValue = getMaxLong(range);
        if (minValue == maxValue) {
            return minValue;
        }
        return random.nextLong(minValue, maxValue);
    }

    private static long getMinLong(Range<Long> range) {
        if (!range.hasLowerBound()) {
            return Long.MIN_VALUE;
        }
        int rangeTypeCorrection = range.lowerBoundType() == BoundType.CLOSED ? 0 : 1;
        return range.lowerEndpoint() + rangeTypeCorrection;
    }

    private static long getMaxLong(Range<Long> range) {
        if (!range.hasUpperBound()) {
            return Long.MAX_VALUE;
        }
        int rangeTypeCorrection = range.upperBoundType() == BoundType.CLOSED ? 1 : 0;
        return range.upperEndpoint() + rangeTypeCorrection;
    }

    private static char getChar(char from, char to) {
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
        int length = random.nextInt(from.length(), to.length());
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(getChar('A', 'z')); //TODO change to length ranges
        }

        return stringBuilder.toString();
    }

    public static String getString(RangeSet rangeSet) {
        Object[] ranges = rangeSet.asRanges().toArray();
        int i = ranges.length == 1 ? 0 : random.nextInt(ranges.length);
        Range range = (Range) ranges[i];
        return getString(range.lowerEndpoint().toString(), range.upperEndpoint().toString());

    }
}
