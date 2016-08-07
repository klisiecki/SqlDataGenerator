package pl.poznan.put.sqldatagenerator.util;

import com.google.common.collect.*;
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys;

import java.util.Set;

public class RangeUtils {

    private static final Configuration configuration = Configuration.getInstance();

    public static final double EPS = configuration.getDoubleProperty(ConfigurationKeys.DOUBLE_EPSILON, 0.00001);

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


    public static long getMaxLong(RangeSet<Long> rangeSet) {
        return getMaxLong(Iterables.getLast(rangeSet.asRanges()));
    }

    public static long getMaxLong(Range<Long> range) {
        if (!range.hasUpperBound()) {
            return Long.MAX_VALUE;
        }
        int rangeTypeCorrection = range.upperBoundType() == BoundType.OPEN ? -1 : 0;
        return range.upperEndpoint() + rangeTypeCorrection;
    }

    public static double getMinDouble(RangeSet<Double> rangeSet) {
        return getMinDouble(rangeSet.asRanges().iterator().next());
    }

    public static double getMinDouble(Range<Double> range) {
        if (!range.hasLowerBound()) {
            return -Double.MAX_VALUE;
        }
        double rangeTypeCorrection = range.lowerBoundType() == BoundType.CLOSED ? 0 : EPS;
        return range.lowerEndpoint() + rangeTypeCorrection;
    }

    public static double getMaxDouble(RangeSet<Double> rangeSet) {
        return getMaxDouble(Iterables.getLast(rangeSet.asRanges()));
    }

    public static double getMaxDouble(Range<Double> range) {
        if (!range.hasUpperBound()) {
            return Double.MAX_VALUE;
        }
        double rangeTypeCorrection = range.upperBoundType() == BoundType.CLOSED ? 0 : -EPS;
        return range.upperEndpoint() + rangeTypeCorrection;
    }

    public static RangeSet<Long> removeEmptyRanges(RangeSet<Long> rangeSet) {
        rangeSet.getClass().getTypeParameters();
        RangeSet<Long> result = TreeRangeSet.create();
        rangeSet.asRanges().stream().filter(longRange -> !isEmpty(longRange)).forEach(result::add);
        return result;
    }

    private static boolean isEmpty(Range<Long> longRange) {
        if (!longRange.hasLowerBound() || !longRange.hasUpperBound()) {
            return false;
        }
        long width = longRange.upperEndpoint() - longRange.lowerEndpoint();
        if (width > 1) {
            return false;
        } else if (width == 0) {
            return longRange.lowerBoundType() == BoundType.OPEN || longRange.upperBoundType() == BoundType.OPEN;
        } else if (width == 1) {
            return longRange.lowerBoundType() == BoundType.OPEN && longRange.upperBoundType() == BoundType.OPEN;
        }
        return true;
    }
}
