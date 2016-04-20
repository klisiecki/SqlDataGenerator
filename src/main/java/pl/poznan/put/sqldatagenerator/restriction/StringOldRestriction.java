package pl.poznan.put.sqldatagenerator.restriction;

import com.google.common.collect.Range;

@Deprecated
public class StringOldRestriction extends OldRestriction {
    public StringOldRestriction(boolean full) {
        super();
        if (full) {
            rangeSet.add(Range.closed(CustomString.MIN_VALUE, CustomString.MAX_VALUE));
        }
    }
}
