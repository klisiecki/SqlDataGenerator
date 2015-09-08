package pl.poznan.put.SqlDataGenerator.restriction;

import com.google.common.collect.Range;

public class StringRestriction extends Restriction {
    public StringRestriction(boolean full) {
        super();
        if (full) {
            rangeSet.add(Range.closed(CustomString.MIN_VALUE, CustomString.MAX_VALUE));
        }
    }
}
