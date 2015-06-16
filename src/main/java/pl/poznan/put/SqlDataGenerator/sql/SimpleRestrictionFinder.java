package pl.poznan.put.SqlDataGenerator.sql;


import com.google.common.collect.Range;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import pl.poznan.put.SqlDataGenerator.restriction.IntegerRestriction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleRestrictionFinder extends AbstractFinder {
//    private List<AttributeRestriction> result = new ArrayList<>();
    private Map<String, AttributeRestriction> result = new HashMap<>();

    public List<AttributeRestriction> findRestrictions(Select select) {
        select.getSelectBody().accept(this);
        return new ArrayList<AttributeRestriction>(result.values());
    }

    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }


    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    private void createMinRestriction(Expression a, Expression b) {
        Long l = getLong(b);
        if (a instanceof Column && l != null) {
//            IntegerRestriction r = new IntegerRestriction();
//            r.addAndRange(Range.closed(l.intValue(), Integer.MAX_VALUE));
//            result.add(new AttributeRestriction((Column)a, r));
//            result.put((Column)a, new AttributeRestriction((Column)a, r));
            putRestriction((Column)a, Range.closed(l.intValue(), Integer.MAX_VALUE));
        }
    }

    private Long getLong(Expression expression) {
        if (expression instanceof LongValue) {
            return ((LongValue) expression).getValue();
        } else if (expression instanceof SignedExpression) {
            SignedExpression se = (SignedExpression) expression;
            Expression e = se.getExpression();
            return -getLong(e);
        } else {
            return null;
        }
    }

    private void createMaxRestriction(Expression a, Expression b) {
        Long l = getLong(b);
        if (a instanceof Column && l != null) {
//            IntegerRestriction r = new IntegerRestriction();
//            r.addAndRange(Range.closed(Integer.MIN_VALUE, l.intValue()));
//            result.add(new AttributeRestriction((Column) a, r));
//            result.put((Column)a, new AttributeRestriction((Column)a, r));
            putRestriction((Column)a, Range.closed(Integer.MIN_VALUE, l.intValue()));
        }
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        //TODO na razie tak samo jak GreaterThanEquals
        Expression a = greaterThan.getLeftExpression();
        Expression b = greaterThan.getRightExpression();
        createMinRestriction(a, b);
        createMaxRestriction(b, a);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        Expression a = greaterThanEquals.getLeftExpression();
        Expression b = greaterThanEquals.getRightExpression();
        createMinRestriction(a, b);
        createMaxRestriction(b, a);
    }

    @Override
    public void visit(MinorThan minorThan) {
        Expression a = minorThan.getLeftExpression();
        Expression b = minorThan.getRightExpression();
        createMinRestriction(b, a);
        createMaxRestriction(a, b);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        Expression a = minorThanEquals.getLeftExpression();
        Expression b = minorThanEquals.getRightExpression();
        createMinRestriction(b, a);
        createMaxRestriction(a, b);
    }

    private void putRestriction(Column c, Range range) {
        if (!result.containsKey(c.toString())) {
            IntegerRestriction r = new IntegerRestriction();
            r.addAndRange(range);
            result.put(c.toString(), new AttributeRestriction(c, r));
        } else {
            IntegerRestriction r = (IntegerRestriction) result.get(c.toString()).getRestriction();
            r.addAndRange(range);
            result.put(c.toString(), new AttributeRestriction(c, r));
        }
    }

}
