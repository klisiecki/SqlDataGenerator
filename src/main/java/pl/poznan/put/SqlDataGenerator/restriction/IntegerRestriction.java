package pl.poznan.put.SqlDataGenerator.restriction;


import com.google.common.collect.Range;

import java.util.List;

public class IntegerRestriction extends Restriction {

    public IntegerRestriction() {
        super();
        rangeSet.add(Range.closed(Integer.MIN_VALUE/2, Integer.MAX_VALUE/2));
    }

    public void setValues(List<Integer> values) {
//        if (this.values == null) {
//            this.values = values;
//        } else {
//            //TODO
//        }
    }
}
