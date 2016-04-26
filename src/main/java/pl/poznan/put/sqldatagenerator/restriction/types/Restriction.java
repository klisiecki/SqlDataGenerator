package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;

public abstract class Restriction {
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
