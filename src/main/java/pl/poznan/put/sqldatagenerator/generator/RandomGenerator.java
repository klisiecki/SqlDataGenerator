package pl.poznan.put.sqldatagenerator.generator;


import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.commons.lang.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        Set<Range<Long>> ranges = rangeSet.asRanges();
        List<Range> rangesList = new ArrayList<>(ranges);
        List<Long> cumulativeProbabilities = new ArrayList<>();
        long prevProbability = 0;
        for (Range<Long> longRange : rangesList) {
            long width = longRange.upperEndpoint() - longRange.lowerEndpoint() + 1;
            long end = width + prevProbability;
            cumulativeProbabilities.add(end);
            prevProbability = end;
        }
        Range<Long> range = getRandomRange(rangesList, cumulativeProbabilities);
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

    //TODO remove duplicated code if possible
    public static Double randomDouble(RangeSet<Double> rangeSet) {
        Set<Range<Double>> ranges = rangeSet.asRanges();
        List<Range> rangesList = new ArrayList<>(ranges);
        List<Long> cumulativeProbabilities = new ArrayList<>();
        long prevProbability = 0;
        for (Range<Double> longRange : rangesList) {
            long width = (longRange.upperEndpoint().longValue() - longRange.lowerEndpoint().longValue()) * 1000 + 1;
            long end = width + prevProbability;
            cumulativeProbabilities.add(end);
            prevProbability = end;
        }
        Range<Double> range = getRandomRange(rangesList, cumulativeProbabilities);
        double minValue = getMinDouble(range);
        double maxValue = getMaxDouble(range);
        if (minValue == maxValue) {
            return minValue;
        }
        return random.nextDouble(minValue, maxValue);
    }

    private static Range getRandomRange(List<Range> ranges, List<Long> probabilities) {
        long max = probabilities.get(probabilities.size() - 1);
        long probabilityIndex = random.nextLong(max);
        int i = 0;
        long sum = 0;
        while (sum < probabilityIndex) {
            sum += probabilities.get(i++);
        }
        return ranges.get(Math.max(0, i - 1));
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
