package pl.poznan.put.sqldatagenerator.util;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RangeUtilsTest {
    //Long
    private Range<Long> toZero;
    private RangeSet<Long> toZeroRS;

    private Range<Long> fromHundred;
    private RangeSet<Long> fromHundredRS;

    private Range<Long> minusTenToTeClosed;
    private Range<Long> twentyToThirtyClosed;
    private Range<Long> minusTenToTenOpen;

    private RangeSet<Long> minusTenToTenOpenRS;
    private RangeSet<Long> minusTenToThirtyClosedRS;

    //Double
    private Range<Double> toZeroDouble;
    private RangeSet<Double> toZeroDoubleRS;

    private Range<Double> fromHunderDouble;
    private RangeSet<Double> fromHunderDoubleRS;

    private Range<Double> minusTenToTenDoubleOpen;
    private Range<Double> minusTenToTenDoubleClosed;

    private RangeSet<Double> minusTenToTenDoubleOpenRS;


    @Before
    public void setUp() throws Exception {
        //Long
        toZero = Range.atMost(0L);
        toZeroRS = TreeRangeSet.create();
        toZeroRS.add(toZero);

        fromHundred = Range.atLeast(100L);
        fromHundredRS = TreeRangeSet.create();
        fromHundredRS.add(fromHundred);

        minusTenToTeClosed = Range.closed(-10L, 10L);
        minusTenToTenOpen = Range.open(-10L, 10L);
        twentyToThirtyClosed = Range.closed(20L, 30L);

        minusTenToTenOpenRS = TreeRangeSet.create();
        minusTenToTenOpenRS.add(minusTenToTenOpen);

        minusTenToThirtyClosedRS = TreeRangeSet.create();
        minusTenToThirtyClosedRS.add(minusTenToTeClosed);
        minusTenToThirtyClosedRS.add(twentyToThirtyClosed);

        //Double
        toZeroDouble = Range.atMost(0.0);
        toZeroDoubleRS = TreeRangeSet.create();
        toZeroDoubleRS.add(toZeroDouble);

        fromHunderDouble = Range.atLeast(100.0);
        fromHunderDoubleRS = TreeRangeSet.create();
        fromHunderDoubleRS.add(fromHunderDouble);

        minusTenToTenDoubleClosed = Range.closed(-10.0, 10.0);
        minusTenToTenDoubleOpen = Range.open(-10.0, 10.0);

        minusTenToTenDoubleOpenRS = TreeRangeSet.create();
        minusTenToTenDoubleOpenRS.add(minusTenToTenDoubleOpen);
    }

    @Test
    public void intersectRangeSets() throws Exception {
        //check empty result range set
        assertEquals(RangeUtils.intersectRangeSets(toZeroRS, fromHundredRS), TreeRangeSet.create());

        RangeSet<Long> rs1 = TreeRangeSet.create(fromHundredRS);
        rs1.add(minusTenToTeClosed);

        RangeSet<Long> rs3 = RangeUtils.intersectRangeSets(toZeroRS, rs1);

        Set<Range<Long>> rs3Ranges = rs3.asRanges();
        assertTrue(rs3Ranges.size() == 1);
        assertTrue(rs3Ranges.contains(Range.closed(-10L, 0L)));

        Range<Long> r4 = Range.openClosed(-10L, 100L);
        RangeSet<Long> rs4 = TreeRangeSet.create();
        rs4.add(r4);

        RangeSet<Long> rs3rs4 = RangeUtils.intersectRangeSets(rs3, rs4);
        Set<Range<Long>> rs3rs4Ranges = rs3rs4.asRanges();
        assertTrue(rs3rs4Ranges.size() == 1);
        assertTrue(rs3rs4Ranges.contains(Range.openClosed(-10L, 0L)));
    }

    @Test
    public void getMinLongFromRangeSet() throws Exception {
        assertEquals(100L, RangeUtils.getMinLong(fromHundredRS));
        assertEquals(Long.MIN_VALUE, RangeUtils.getMinLong(toZeroRS));
        assertEquals(-9, RangeUtils.getMinLong(minusTenToTenOpenRS));
        assertEquals(-10, RangeUtils.getMinLong(minusTenToThirtyClosedRS));
    }

    @Test
    public void getMinLongFromRange() throws Exception {
        assertEquals(100L, RangeUtils.getMinLong(fromHundred));
        assertEquals(Long.MIN_VALUE, RangeUtils.getMinLong(toZero));
        assertEquals(-9, RangeUtils.getMinLong(minusTenToTenOpen));
    }

    @Test
    public void getMaxLongFromRangeSet() throws Exception {
        assertEquals(Long.MAX_VALUE, RangeUtils.getMaxLong(fromHundredRS));
        assertEquals(0, RangeUtils.getMaxLong(toZeroRS));
        assertEquals(9, RangeUtils.getMaxLong(minusTenToTenOpenRS));
        assertEquals(30, RangeUtils.getMaxLong(minusTenToThirtyClosedRS));
    }

    @Test
    public void getMaxLongFromRange() throws Exception {
        assertEquals(Long.MAX_VALUE, RangeUtils.getMaxLong(fromHundred));
        assertEquals(0, RangeUtils.getMaxLong(toZero));
        assertEquals(9, RangeUtils.getMaxLong(minusTenToTenOpen));
    }

    @Test
    public void getMinDoubleFromRangeSet() throws Exception {
        assertEquals(100.0, RangeUtils.getMinDouble(fromHunderDoubleRS), Double.MIN_VALUE);
        assertEquals(-Double.MAX_VALUE, RangeUtils.getMinDouble(toZeroDoubleRS), Double.MIN_VALUE);
        double minDouble = RangeUtils.getMinDouble(minusTenToTenDoubleOpenRS);
        assertEquals(-10 + RangeUtils.EPS, minDouble, Double.MIN_VALUE);
        assertTrue(minDouble + " should be greater than " + -10, minDouble > -10);
    }

    @Test
    public void getMinDoubleFromRange() throws Exception {
        assertEquals(100.0, RangeUtils.getMinDouble(fromHunderDouble), Double.MIN_VALUE);
        assertEquals(-Double.MAX_VALUE, RangeUtils.getMinDouble(toZeroDouble), Double.MIN_VALUE);
        double minDouble = RangeUtils.getMinDouble(minusTenToTenDoubleOpen);
        assertEquals(-10 + RangeUtils.EPS, minDouble, Double.MIN_VALUE);
        assertTrue(minDouble + " should be greater than " + -10, minDouble > -10);
    }

    @Test
    public void getMaxDoubleFromRangeSet() throws Exception {
        assertEquals(Double.MAX_VALUE, RangeUtils.getMaxDouble(fromHunderDoubleRS), Double.MIN_VALUE);
        assertEquals(0.0, RangeUtils.getMaxDouble(toZeroDoubleRS), Double.MIN_VALUE);
        assertEquals(10 - RangeUtils.EPS, RangeUtils.getMaxDouble(minusTenToTenDoubleOpenRS), Double.MIN_VALUE);
    }

    @Test
    public void getMaxDoubleFromRange() throws Exception {
        assertEquals(Double.MAX_VALUE, RangeUtils.getMaxDouble(fromHunderDouble), Double.MIN_VALUE);
        assertEquals(0.0, RangeUtils.getMaxDouble(toZeroDouble), Double.MIN_VALUE);
        assertEquals(10 - RangeUtils.EPS, RangeUtils.getMaxDouble(minusTenToTenDoubleOpen), Double.MIN_VALUE);
    }

}