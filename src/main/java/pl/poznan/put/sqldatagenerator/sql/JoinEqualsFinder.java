package pl.poznan.put.sqldatagenerator.sql;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import pl.poznan.put.sqldatagenerator.exception.SQLSyntaxNotSupportedException;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;

import java.util.ArrayList;
import java.util.List;

public class JoinEqualsFinder extends AbstractFinder {
    private final List<AttributesPair> result = new ArrayList<>();

    public List<AttributesPair> findEquals(Select select) {
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
//        if (plainSelect.getWhere() != null) {
//            plainSelect.getWhere().accept(this);
//        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        throw new SQLSyntaxNotSupportedException("OR not allowed in JOIN ON clause");
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        Expression leftExpression = equalsTo.getLeftExpression();
        Expression rightExpression = equalsTo.getRightExpression();
        if (leftExpression instanceof Column && rightExpression instanceof Column) {
            Column leftColumn = (Column) leftExpression;
            Column rightColumn = (Column) rightExpression;
            result.add(new AttributesPair(leftColumn, rightColumn));
        }
    }
}
