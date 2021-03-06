package pl.poznan.put.sqldatagenerator.sql;

import com.bpodgursky.jbool_expressions.*;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import pl.poznan.put.sqldatagenerator.restriction.RestrictionFactory;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;

public class RestrictionFinder extends AbstractFinder {
    private Expression<Restriction> result;

    private RestrictionFinder() {
    }

    public RestrictionFinder(Select select) {
        RestrictionFinder finder = new RestrictionFinder();
        select.getSelectBody().accept(finder);
        result = finder.getResult();
    }

    public Expression<Restriction> getResult() {
        return result;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getWhere() != null) {
//            RestrictionFinder joinFinder = new RestrictionFinder();
//            plainSelect.getWhere().accept(joinFinder);
            RestrictionFinder whereFinder = new RestrictionFinder();
            plainSelect.getWhere().accept(whereFinder);
//            result = And.of(joinFinder.getResult(), whereFinder.getResult());
            result = whereFinder.getResult();
        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        RestrictionFinder finder1 = new RestrictionFinder();
        andExpression.getLeftExpression().accept(finder1);
        RestrictionFinder finder2 = new RestrictionFinder();
        andExpression.getRightExpression().accept(finder2);
        result = And.of(finder1.getResult(), finder2.getResult());
        if (andExpression.isNot()) {
            result = Not.of(result);
        }
    }

    @Override
    public void visit(OrExpression orExpression) {
        RestrictionFinder finder1 = new RestrictionFinder();
        orExpression.getLeftExpression().accept(finder1);
        RestrictionFinder finder2 = new RestrictionFinder();
        orExpression.getRightExpression().accept(finder2);
        result = Or.of(finder1.getResult(), finder2.getResult());
        if (orExpression.isNot()) {
            result = Not.of(result);
        }
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        RestrictionFinder finder = new RestrictionFinder();
        parenthesis.getExpression().accept(finder);
        result = finder.getResult();
        if (parenthesis.isNot()) {
            result = Not.of(result);
        }
    }

    private void processExpression(net.sf.jsqlparser.expression.Expression expression) {
        result = Variable.of(RestrictionFactory.createRestriction(expression));
        boolean not = false;
        if (expression instanceof BinaryExpression) {
            not = ((BinaryExpression) expression).isNot();
        } else if (expression instanceof InExpression) {
            not = ((InExpression) expression).isNot();
        } else if (expression instanceof Between) {
            not = ((Between) expression).isNot();
        } else if (expression instanceof IsNullExpression) {
            not = ((IsNullExpression) expression).isNot();
        }
        if (not) {
            result = Not.of(result);
        }
    }


    @Override
    public void visit(Between between) {
        processExpression(between);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        processExpression(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        processExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        processExpression(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {
        processExpression(inExpression);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        processExpression(isNullExpression);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        processExpression(likeExpression);
    }

    @Override
    public void visit(MinorThan minorThan) {
        processExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        processExpression(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        processExpression(notEqualsTo);
    }


}
