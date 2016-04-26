package pl.poznan.put.sqldatagenerator.restriction.types;

import com.google.common.collect.TreeRangeSet;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.InExpression;

public class RangeRestriction extends OneAttributeRestriction {

    private TreeRangeSet rangeSet;

    public RangeRestriction(Expression expression) {
        super(expression);
    }

    public static RangeRestriction fromGreaterThan(GreaterThan greaterThan) {
        return new RangeRestriction(greaterThan);
    }

    public static RangeRestriction fromBetween(Between between) {
        return new RangeRestriction(between);
    }

    public static RangeRestriction fromIn(InExpression in) {
        return new RangeRestriction(in);
    }

    public static RangeRestriction fromEquals(EqualsTo equalsTo) {
        return new RangeRestriction(equalsTo);
    }


}
