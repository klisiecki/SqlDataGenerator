package pl.poznan.put.sqldatagenerator.restriction;

import net.sf.jsqlparser.expression.Expression;

public class Restriction {
    private Expression expression;

    public Restriction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "Restriction{" +
                "expression=" + expression +
                '}';
    }
}
