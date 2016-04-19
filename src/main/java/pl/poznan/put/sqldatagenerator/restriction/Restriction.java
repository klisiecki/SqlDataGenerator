package pl.poznan.put.sqldatagenerator.restriction;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import pl.poznan.put.sqldatagenerator.Utils;

public abstract class Restriction {
    protected TreeRangeSet rangeSet;

    public TreeRangeSet getRangeSet() {
        return rangeSet;
    }

    public Restriction() {
        rangeSet = TreeRangeSet.create();
    }

    public void addOrRange(Range range) {
        rangeSet.add(range);
    }

    public void addAndRange(Range range) {
        TreeRangeSet t = TreeRangeSet.create();
        t.add(range);
        rangeSet = Utils.intersectRangeSets(rangeSet, t);
    }

    public void addOrRangeSet(TreeRangeSet set) {
        rangeSet.addAll(set);
    }

    public void addAndRangeSet(TreeRangeSet set) {
        rangeSet = Utils.intersectRangeSets(rangeSet, set);
    }
}
