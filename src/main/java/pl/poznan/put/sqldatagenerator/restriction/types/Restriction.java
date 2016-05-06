package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import pl.poznan.put.sqldatagenerator.generator.Attribute;

import java.util.List;

public abstract class Restriction {
    protected List<Attribute> attributes;
    protected Expression expression;

    public Restriction(Expression expression) {
        this.expression = expression;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public abstract Restriction reverse();

    @Override
    public String toString() {
        return "Restriction{" +
                "expression=" + expression +
                '}';
    }
}
