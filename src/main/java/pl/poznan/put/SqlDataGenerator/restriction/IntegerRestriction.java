package pl.poznan.put.SqlDataGenerator.restriction;


import com.google.common.collect.Range;

import java.util.List;

public class IntegerRestriction extends Restriction {

    public IntegerRestriction(boolean full) {
        super();
        if (full) {
            rangeSet.add(Range.closed(Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2));
        }
    }
}
