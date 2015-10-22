package pl.poznan.put.SqlDataGenerator.sql;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import pl.poznan.put.SqlDataGenerator.sql.model.RestrictionEquals;

import java.util.ArrayList;
import java.util.List;

//TODO na razie działa, ale trzeba przenieść do SimpleRestrictionFinder, żeby działały ORy
public class EqualsFinder extends AbstractFinder {
    private final List<RestrictionEquals> result = new ArrayList<>();

    public List<RestrictionEquals> findEquals(Select select) {
        select.getSelectBody().accept(this);
        return result;
    }

    private void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getJoins() != null) {
            plainSelect.getJoins().stream().filter(join -> join.getRightItem() instanceof Table).
                    forEach(join -> join.getOnExpression().accept(this));
        }
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        Expression leftExpression = equalsTo.getLeftExpression();
        Expression rightExpression = equalsTo.getRightExpression();
        if (leftExpression instanceof Column && rightExpression instanceof Column) {
            Column leftColumn = (Column) leftExpression;
            Column rightColumn = (Column) rightExpression;
            result.add(new RestrictionEquals(leftColumn, rightColumn));
        }
    }
}
