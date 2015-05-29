package pl.poznan.put.SqlDataGenerator;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

public class TestVisitor implements SelectVisitor, SelectItemVisitor {
    List<String> result = new ArrayList<String>();


    public List<String> getSelectItems(Select select) {
        result.clear();
        select.getSelectBody().accept(this);

        return result;
    }

    @Override
    public void visit(AllColumns allColumns) {

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {

    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        result.add(selectExpressionItem.toString());
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem item : plainSelect.getSelectItems()) {
                item.accept(this);
            }
        }
    }

    @Override
    public void visit(SetOperationList setOpList) {

    }

    @Override
    public void visit(WithItem withItem) {

    }
}
