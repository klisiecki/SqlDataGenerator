package pl.poznan.put.SqlDataGenerator.restriction;

import com.google.common.collect.Range;
import org.apache.commons.lang3.StringUtils;

public class StringRestriction extends Restriction {
    public StringRestriction(boolean full) {
        super();
        if (full) {
            rangeSet.add(Range.closed(MyString.MIN_VALUE, MyString.MAX_VALUE));
        }
    }
}
