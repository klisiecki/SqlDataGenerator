package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import pl.poznan.put.sqldatagenerator.generator.Attribute;

import java.util.List;

public abstract class Restriction {
    protected List<Attribute> attributes;
    protected final Expression expression;

    protected Restriction(Expression expression) {
        this.expression = expression;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public abstract Restriction reverse();

    public abstract Restriction clone() throws CloneNotSupportedException;

    @Override
    public String toString() {
        return "Restriction{" +
                "expression=" + expression +
                '}';
    }
}
