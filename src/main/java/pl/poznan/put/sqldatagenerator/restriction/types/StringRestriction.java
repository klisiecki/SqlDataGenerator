package pl.poznan.put.sqldatagenerator.restriction.types;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.exception.NotImplementedException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.*;

public class StringRestriction extends OneAttributeRestriction {

    public class LikeExpressionProperties {
        private String like;
        private boolean leftOpen;
        private boolean rightOpen;

        public LikeExpressionProperties(String like, boolean leftOpen, boolean rightOpen) {
            this.like = like;
            this.leftOpen = leftOpen;
            this.rightOpen = rightOpen;
        }

        public LikeExpressionProperties(LikeExpressionProperties likeExpressionProperties) {
            this(likeExpressionProperties.getLike(), likeExpressionProperties.isLeftOpen(), likeExpressionProperties.isRightOpen());
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
    private LikeExpressionProperties likeExpressionProperties;
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

    public StringRestriction(Attribute attribute, int minLength, int maxLength, LikeExpressionProperties likeExpressionProperties, List<String> allowedValues, boolean isNegated) {
        this(attribute);
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.likeExpressionProperties = likeExpressionProperties;
        this.allowedValues = allowedValues;
        this.isNegated = isNegated;
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

    public LikeExpressionProperties getLikeExpressionProperties() {
        return likeExpressionProperties;
    }

    public void setLikeExpressionProperties(LikeExpressionProperties likeExpressionProperties) {
        this.likeExpressionProperties = likeExpressionProperties;
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

    @Override
    public Restriction reverse() {
        this.isNegated = !this.isNegated;
        return this;
    }

    @Override
    public Restriction clone() {
        StringRestriction clone = new StringRestriction(this.getAttribute());
        clone.setMinLength(minLength);
        clone.setMaxLength(maxLength);
        clone.setAllowedValues(new ArrayList<>(allowedValues));
        if (likeExpressionProperties != null) {
            clone.setLikeExpressionProperties(new LikeExpressionProperties(likeExpressionProperties));
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

    @Override
    public String toString() {
        return "StringRestriction{" +
                getAttribute() +
                ", length=[" + minLength + "," + maxLength + "]" +
                (likeExpressionProperties == null ? "" : ", likeExpressionProperties=" + likeExpressionProperties) +
                (allowedValues == null ? "" : ", allowedValues=" + allowedValues) +
                ", isNegated=" + isNegated + '}';
    }
}
