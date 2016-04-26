package pl.poznan.put.sqldatagenerator.restriction;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.NExpression;
import com.bpodgursky.jbool_expressions.Variable;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class Restrictions {
    private final Collection<Restriction> restrictions;

    public Restrictions(Collection<Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    public void add(Restrictions restrictions) {
        this.restrictions.addAll(restrictions.getCollection());
    }

    public Collection<Restriction> getCollection() {
        return restrictions;
    }

    /**
     * @param exp must be and and and
     * @return
     */
    public static Restrictions fromExpression(Expression<Restriction> exp) {
        if (exp instanceof NExpression) {
            List<Expression<Restriction>> children = ((NExpression<Restriction>) exp).getChildren();
            List<Restriction> list = children.stream().map(e -> ((Variable<Restriction>) e).getValue()).collect(toList());
            return new Restrictions(list);
        } else if (exp instanceof Variable) {
            return new Restrictions(singletonList(((Variable<Restriction>) exp).getValue()));
        } else {
            throw new RuntimeException(exp.getClass() + " not supported here");
        }
    }

    @Override
    public String toString() {
        return restrictions.toString();
    }
}
