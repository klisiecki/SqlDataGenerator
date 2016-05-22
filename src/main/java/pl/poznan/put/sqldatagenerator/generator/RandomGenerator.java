package pl.poznan.put.sqldatagenerator.generator;


import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.commons.lang.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unchecked")
public class RandomGenerator {

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    /**
     * Returns a pseudorandom value from given {@link RangeSet}.
     *
     * @param rangeSet {@link TreeRangeSet} of {@link Long} type. Must not contain empty ranges.
     * @return random {@link Long} value from random {@link Range} in given set.
     */
    public static Long randomLong(RangeSet<Long> rangeSet) {
        Range<Long> range = getRandomRange(rangeSet);
        long minValue = getMinLong(range);
        long maxValue = getMaxLong(range);
        if (minValue == maxValue) {
            return minValue;
        }
        return random.nextLong(minValue, maxValue);
    }

    public static int randomIndex(List list) {
        return random.nextInt(list.size());
    }

    public static Double randomDouble(RangeSet<Double> rangeSet) {
        Range<Double> range = getRandomRange(rangeSet);
        double minValue = getMinDouble(range);
        double maxValue = getMaxDouble(range);
        if (minValue == maxValue) {
            return minValue;
        }
        return random.nextDouble(minValue, maxValue);
    }

    private static Range getRandomRange(RangeSet rangeSet) {
        List<Range> ranges = new ArrayList<>();
        ranges.addAll(rangeSet.asRanges());
        int i = ranges.size() == 1 ? 0 : random.nextInt(ranges.size());
        return ranges.get(i);
    }

    private static long getMinLong(Range<Long> range) {
        if (!range.hasLowerBound()) {
            return Long.MIN_VALUE;
        }
        int rangeTypeCorrection = range.lowerBoundType() == BoundType.CLOSED ? 0 : 1;
        return range.lowerEndpoint() + rangeTypeCorrection;
    }

    private static double getMinDouble(Range<Double> range) {
        if (!range.hasLowerBound()) {
            return Long.MIN_VALUE;
        }
        double rangeTypeCorrection = range.lowerBoundType() == BoundType.CLOSED ? 0 : 0.0001; //TODO consider double ranges
        return range.lowerEndpoint() + rangeTypeCorrection;
    }

    private static long getMaxLong(Range<Long> range) {
        if (!range.hasUpperBound()) {
            return Long.MAX_VALUE;
        }
        int rangeTypeCorrection = range.upperBoundType() == BoundType.CLOSED ? 1 : 0;
        return range.upperEndpoint() + rangeTypeCorrection;
    }

    private static double getMaxDouble(Range<Double> range) {
        if (!range.hasUpperBound()) {
            return Double.MAX_VALUE;
        }
        return range.upperEndpoint();
    }

    public static String randomString(int minLength, int maxLength) {
        int length = random.nextInt(minLength, maxLength + 1);
        return RandomStringUtils.randomAlphabetic(length);
    }
}
