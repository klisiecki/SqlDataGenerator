package pl.poznan.put.sqldatagenerator.util;

import com.google.common.collect.*;

import java.util.Set;

public class RangeUtils {
    @SuppressWarnings("unchecked")
    public static RangeSet intersectRangeSets(RangeSet a, RangeSet b) {
        RangeSet result = TreeRangeSet.create();
        for (Range aRange : (Set<Range>) a.asRanges()) {
            for (Range bRange : (Set<Range>) b.asRanges()) {
                try {
                    Range r = bRange.intersection(aRange);
                    result.add(r);
                } catch (IllegalArgumentException ignore) {
                }
            }
        }
        return result;
    }

    public static long getMinLong(RangeSet<Long> rangeSet) {
        return getMinLong(rangeSet.asRanges().iterator().next());
    }

    public static long getMinLong(Range<Long> range) {
        if (!range.hasLowerBound()) {
            return Long.MIN_VALUE;
        }
        int rangeTypeCorrection = range.lowerBoundType() == BoundType.CLOSED ? 0 : 1;
        return range.lowerEndpoint() + rangeTypeCorrection;
    }


    public static double getMinDouble(Range<Double> range) {
        if (!range.hasLowerBound()) {
            return -Double.MAX_VALUE;
        }
        double rangeTypeCorrection = range.lowerBoundType() == BoundType.CLOSED ? 0 : Double.MIN_VALUE;
        return range.lowerEndpoint() + rangeTypeCorrection;
    }

    public static long getMaxLong(RangeSet<Long> rangeSet) {
        return getMaxLong(Iterables.getLast(rangeSet.asRanges()));
    }

    public static long getMaxLong(Range<Long> range) {
        if (!range.hasUpperBound()) {
            return Long.MAX_VALUE;
        }
        int rangeTypeCorrection = range.upperBoundType() == BoundType.CLOSED ? 1 : 0;
        return range.upperEndpoint() + rangeTypeCorrection;
    }

    public static double getMaxDouble(Range<Double> range) {
        if (!range.hasUpperBound()) {
            return Double.MAX_VALUE;
        }
        return range.upperEndpoint();
    }
}
