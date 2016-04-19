package pl.poznan.put.sqldatagenerator.sql;


import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import pl.poznan.put.sqldatagenerator.restriction.CustomString;
import pl.poznan.put.sqldatagenerator.restriction.IntegerRestriction;
import pl.poznan.put.sqldatagenerator.restriction.StringRestriction;
import pl.poznan.put.sqldatagenerator.sql.model.AttributeRestriction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleRestrictionFinder extends AbstractFinder {
    private final Map<String, AttributeRestriction> result = new HashMap<>();
    private boolean isAndExpr;

    public List<AttributeRestriction> findRestrictions(Select select) {
        select.getSelectBody().accept(this);
        return new ArrayList<>(result.values());
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
        isAndExpr = true;
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        isAndExpr = false;
        visitBinaryExpression(orExpression);
    }

    private void createMinRestriction(Expression a, Expression b) {
        if (a instanceof Column) {
            if (b instanceof LongValue || b instanceof SignedExpression) {
                Long l = getLong(b);
                putIntegerRestriction((Column) a, Range.closed(l.intValue(), Integer.MAX_VALUE));
            } else if (b instanceof StringValue) {
                putStringRestriction((Column) a, Range.closed(new CustomString(getString(b)), CustomString.MAX_VALUE));
            } else {
                throw new NotImplementedException();
            }
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

    private String getString(Expression e) {
        if (e instanceof StringValue) {
            return ((StringValue) e).getValue();
        } else {
            return null;
        }
    }

    private void createMaxRestriction(Expression a, Expression b) {
        if (a instanceof Column) {
            if (b instanceof LongValue || b instanceof SignedExpression) {
                Long l = getLong(b);
                putIntegerRestriction((Column) a, Range.closed(Integer.MIN_VALUE, l.intValue()));
            } else if (b instanceof StringValue) {
                putStringRestriction((Column) a, Range.closed(CustomString.MIN_VALUE, new CustomString(getString(b))));
            } else {
                throw new NotImplementedException();
            }
        }
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        //TODO change it - temporarily the same as GreaterThanEquals
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


    @Override
    public void visit(Between between) {
        Expression column = between.getLeftExpression();
        Expression min = between.getBetweenExpressionStart();
        Expression max = between.getBetweenExpressionEnd();
        createMinRestriction(column, min);
        createMaxRestriction(column, max);
    }



    @Override
    public void visit(InExpression inExpression) {
        Column column = (Column) inExpression.getLeftExpression();
        TreeRangeSet rangeSet = TreeRangeSet.create();
        if (inExpression.getRightItemsList() instanceof ExpressionList) {
            ExpressionList list = (ExpressionList) inExpression.getRightItemsList();
            Expression first = list.getExpressions().get(0);
            if (first instanceof LongValue || first instanceof SignedExpression) {
                for (Expression e : list.getExpressions()) {
                    Long v = getLong(e);
                    rangeSet.add(Range.closed(v.intValue(), v.intValue()));
                }
                putIntegerRestriction(column, rangeSet);
            } else if (first instanceof StringValue) {
                for (Expression e : list.getExpressions()) {
                    rangeSet.add(Range.closed(new CustomString(getString(e)), new CustomString(getString(e))));
                }
                putStringRestriction(column, rangeSet);
            } else {
                throw new NotImplementedException();
            }
        }
    }

    private void createEqualsRestriction(Expression a, Expression b) {
        if (!(a instanceof Column)) {
            return;
        }
        if (b instanceof SignedExpression || b instanceof LongValue) {
            Long l = getLong(b);
            if (l != null) {
                putIntegerRestriction((Column) a, Range.closed(l.intValue(), l.intValue()));
            }
        } else if (b instanceof StringValue) {
            putStringRestriction((Column) a, Range.closed(new CustomString(((StringValue) b).getValue()), new CustomString(getString(b))));
        } else {
            throw new NotImplementedException();
        }
    }

    private void createNotEqualsRestriction(Expression a, Expression b) {
        if (!(a instanceof Column)) {
            return;
        }
        if (b instanceof SignedExpression || b instanceof LongValue) {
            Long l = getLong(b);
            if (l != null) {
                TreeRangeSet rangeSet = TreeRangeSet.create();
                rangeSet.add(Range.closed(l.intValue(), l.intValue()));
                putIntegerRestriction((Column) a, (TreeRangeSet) rangeSet.complement().subRangeSet(Range.closed(Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2)));
            }
        } else if (b instanceof StringValue) {
            TreeRangeSet rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(new CustomString(((StringValue) b).getValue()), new CustomString(getString(b))));
            putStringRestriction((Column) a, (TreeRangeSet) rangeSet.complement().subRangeSet(Range.closed(CustomString.MIN_VALUE, CustomString.MAX_VALUE)));
        } else {
            throw new NotImplementedException();
        }
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        Expression a = equalsTo.getLeftExpression();
        Expression b = equalsTo.getRightExpression();
        if (a instanceof Column && b instanceof  Column) {
            return;
        }
        createEqualsRestriction(a, b);
        createEqualsRestriction(b, a);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        Expression a = notEqualsTo.getLeftExpression();
        Expression b = notEqualsTo.getRightExpression();
        if (a instanceof Column && b instanceof  Column) {
            return;
        }
        createNotEqualsRestriction(a, b);
        createNotEqualsRestriction(b, a);
    }

    private void putIntegerRestriction(Column c, TreeRangeSet range) {
        if (!result.containsKey(c.toString())) {
            IntegerRestriction r = new IntegerRestriction(isAndExpr);
            r.addAndRangeSet(range);
            result.put(c.toString(), new AttributeRestriction(c, r));
        } else {
            IntegerRestriction r = (IntegerRestriction) result.get(c.toString()).getRestriction();
            r.addAndRangeSet(range);
            result.put(c.toString(), new AttributeRestriction(c, r));
        }
    }

    private void putIntegerRestriction(Column c, Range range) {
        if (!result.containsKey(c.toString())) {
            IntegerRestriction r = new IntegerRestriction(isAndExpr);
            r.addAndRange(range);
            result.put(c.toString(), new AttributeRestriction(c, r));
        } else {
            IntegerRestriction r = (IntegerRestriction) result.get(c.toString()).getRestriction();
            r.addAndRange(range);
            result.put(c.toString(), new AttributeRestriction(c, r));
        }
    }

    private void putStringRestriction(Column c, TreeRangeSet range) {
        if (!result.containsKey(c.toString())) {
            StringRestriction r = new StringRestriction(isAndExpr);
            if (isAndExpr) {
                r.addAndRangeSet(range);
            } else {
                r.addOrRangeSet(range);
            }
            result.put(c.toString(), new AttributeRestriction(c, r));
        } else {
            StringRestriction r = (StringRestriction) result.get(c.toString()).getRestriction();
            if (isAndExpr) {
                r.addAndRangeSet(range);
            } else {
                r.addOrRangeSet(range);
            }
            result.put(c.toString(), new AttributeRestriction(c, r));
        }

    }

    private void putStringRestriction(Column c, Range range) {
        if (!result.containsKey(c.toString())) {
            StringRestriction r = new StringRestriction(isAndExpr);
            if (isAndExpr) {
                r.addAndRange(range);
            } else {
                r.addOrRange(range);
            }
            result.put(c.toString(), new AttributeRestriction(c, r));
        } else {
            StringRestriction r = (StringRestriction) result.get(c.toString()).getRestriction();
            if (isAndExpr) {
                r.addAndRange(range);
            } else {
                r.addOrRange(range);
            }
            result.put(c.toString(), new AttributeRestriction(c, r));
        }
    }

}
