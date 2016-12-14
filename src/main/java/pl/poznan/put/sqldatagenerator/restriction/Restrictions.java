package pl.poznan.put.sqldatagenerator.restriction;

import com.bpodgursky.jbool_expressions.*;
import pl.poznan.put.sqldatagenerator.exception.InvalidInternalStateException;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;

import java.util.ArrayList;
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
        this.restrictions.addAll(restrictions.asCollection());
    }

    public Collection<Restriction> asCollection() {
        return restrictions;
    }

    public Restrictions clone() {
        return new Restrictions(asCollection().stream().map(Restriction::clone).collect(toList()));
    }

    /**
     * @param expression must be {@link And} or {@link Or}
     * @return {@link Restrictions} object with copy of {@link Restriction}s from given expression
     */
    public static Restrictions fromExpression(Expression<Restriction> expression) {
        if (expression instanceof NExpression) {
            List<Expression<Restriction>> children = ((NExpression<Restriction>) expression).getChildren();
            List<Restriction> list = children.stream().map(Restrictions::getRestriction).collect(toList());
            return new Restrictions(list);
        } else if (expression instanceof Variable || expression instanceof Not) {
            return new Restrictions(new ArrayList<>(singletonList(getRestriction(expression))));
        } else {
            throw new InvalidInternalStateException(expression.getClass() + " not supported here");
        }
    }

    private static Restriction getRestriction(Expression<Restriction> expression) {
        if (expression instanceof Variable) {
            return ((Variable<Restriction>) expression).getValue().clone();
        } else if (expression instanceof Not) {
            Expression<Restriction> e = ((Not<Restriction>) expression).getE();
            return getRestriction(e).reverse();
        } else {
            throw new InvalidInternalStateException(expression.getClass() + " not supported here");
        }
    }

    public void reverserAll() {
        restrictions.forEach(Restriction::reverse);
    }

    @Override
    public String toString() {
        return restrictions.toString();
    }
}
