package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import org.apache.commons.lang.NotImplementedException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.*;

public class StringRestriction extends OneAttributeRestriction {

    public class LikeExpression {
        private String like;
        private boolean leftOpen;
        private boolean rightOpen;

        public LikeExpression(String like, boolean leftOpen, boolean rightOpen) {
            this.like = like;
            this.leftOpen = leftOpen;
            this.rightOpen = rightOpen;
        }

        public LikeExpression(LikeExpression likeExpression) {
            this(likeExpression.getLike(), likeExpression.isLeftOpen(), likeExpression.isRightOpen());
        }

        public String getLike() {
            return like;
        }

        public boolean isLeftOpen() {
            return leftOpen;
        }

        public boolean isRightOpen() {
            return rightOpen;
        }

    }

    private int minLength = 0;
    private int maxLength = 20; //TODO max string length
    private LikeExpression likeExpression;
    private List<String> allowedValues;
    private boolean isNegated;

    protected StringRestriction(Expression expression, Column column) {
        super(expression, column);
        isNegated = false;
    }

    public StringRestriction(Attribute attribute) {
        super(attribute);
        isNegated = false;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public LikeExpression getLikeExpression() {
        return likeExpression;
    }

    public void setLikeExpression(LikeExpression likeExpression) {
        this.likeExpression = likeExpression;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public boolean isNegated() {
        return isNegated;
    }

    public void negate() {
        this.isNegated = !this.isNegated;
    }

    @Override
    public Restriction reverse() {
        StringRestriction reversed = (StringRestriction) this.clone();
        reversed.negate();
        return reversed;
    }

    @Override
    public Restriction clone() {
        StringRestriction clone = new StringRestriction(this.getAttribute());
        clone.setMinLength(minLength);
        clone.setMaxLength(maxLength);
        clone.setAllowedValues(new ArrayList<>(allowedValues));
        if (likeExpression != null) {
            clone.setLikeExpression(new LikeExpression(likeExpression));
        }
        return clone;
    }

    public static StringRestriction fromEquals(EqualsTo equalsTo) {
        StringRestriction restriction = new StringRestriction(equalsTo, getColumn(equalsTo));
        restriction.setAllowedValues(singletonList(getString(getValueExpression(equalsTo))));
        return restriction;
    }

    public static StringRestriction fromNotEquals(NotEqualsTo notEqualsTo) {
        throw new NotImplementedException();
    }

    public static StringRestriction fromIn(InExpression inExpression) {
        StringRestriction restriction = new StringRestriction(inExpression, (Column) inExpression.getLeftExpression());
        List<Expression> expressions = ((ExpressionList) inExpression.getRightItemsList()).getExpressions();
        List<String> values = expressions.stream().map(SQLExpressionsUtils::getString).collect(Collectors.toList());
        restriction.setAllowedValues(values);
        return restriction;
    }

    public static StringRestriction fromLikeExpression(LikeExpression likeExpression) {
        throw new NotImplementedException();
    }


}
