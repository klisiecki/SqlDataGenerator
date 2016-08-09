package pl.poznan.put.sqldatagenerator.restriction.types;

import com.google.common.collect.Range;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DataTypesConverter;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DatabaseType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys.MAX_STRING_LENGTH;
import static pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys.MIN_STRING_LENGTH;
import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.getColumn;
import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.getValueExpression;

public class StringRestriction extends OneAttributeRestriction {

    public static class LikeExpressionProperties {
        private final String like;
        private final String escapeCharacter;


        public LikeExpressionProperties(String like, String escapeCharacter) {
            this.like = like;
            this.escapeCharacter = escapeCharacter;
        }

        public LikeExpressionProperties(LikeExpressionProperties likeExpressionProperties) {
            this(likeExpressionProperties.getLike(), likeExpressionProperties.getEscapeCharacter());
        }

        public String getLike() {
            return like;
        }

        public String getEscapeCharacter() {
            return escapeCharacter;
        }
    }

    private static final Configuration configuration = Configuration.getInstance();

    private static final Integer DEFAULT_MIN_LENGTH = configuration.getIntegerProperty(MIN_STRING_LENGTH, 5);
    private static final Integer DEFAULT_MAX_LENGTH = configuration.getIntegerProperty(MAX_STRING_LENGTH, 20);

    private Range<Integer> allowedLength;
    private LikeExpressionProperties likeExpressionProperties;
    private List<String> allowedValues;
    private boolean isNegated;

    private StringRestriction(Expression expression, Column column) {
        super(expression, column);
        initDefault();
    }

    private StringRestriction(Attribute attribute) {
        super(attribute);
        initDefault();
    }

    public StringRestriction(Attribute attribute, Range<Integer> allowedLength,
                             LikeExpressionProperties likeExpressionProperties, List<String> allowedValues, boolean isNegated) {
        this(attribute);
        this.allowedLength = allowedLength;
        this.likeExpressionProperties = likeExpressionProperties;
        this.allowedValues = allowedValues;
        this.isNegated = isNegated;
    }

    private void initDefault() {
        isNegated = false;
        this.allowedLength = Range.closed(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH);
    }

    public int getMinLength() {
        return allowedLength.lowerEndpoint();
    }

    public void setMinLength(int minLength) {
        this.allowedLength = Range.closed(minLength, getMaxLength());
    }

    public int getMaxLength() {
        return allowedLength.upperEndpoint();
    }

    public void setMaxLength(Integer maxLength) {
        this.allowedLength = Range.closed(getMinLength(), maxLength);
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

    public void setNegated(boolean negated) {
        isNegated = negated;
    }

    @Override
    public Restriction reverse() {
        this.isNegated = !this.isNegated;
        return this;
    }

    @Override
    public Restriction clone() {
        StringRestriction clone = new StringRestriction(this.getAttribute());
        clone.setMinLength(getMinLength());
        clone.setMaxLength(getMaxLength());
        if (allowedValues != null) {
            clone.setAllowedValues(new ArrayList<>(allowedValues));
        }
        if (likeExpressionProperties != null) {
            clone.setLikeExpressionProperties(new LikeExpressionProperties(likeExpressionProperties));
        }
        return clone;
    }

    public static StringRestriction fromEquals(EqualsTo equalsTo) {
        return fromEqualsInternal(equalsTo);
    }

    private static StringRestriction fromEqualsInternal(BinaryExpression expression) {
        Column column = getColumn(expression);
        DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();
        StringRestriction restriction = new StringRestriction(expression, column);
        restriction.setAllowedValues(singletonList(DataTypesConverter.getInternalString(getValueExpression(expression), databaseType)));
        return restriction;
    }

    public static StringRestriction fromNotEquals(NotEqualsTo notEqualsTo) {
        return (StringRestriction) fromEqualsInternal(notEqualsTo).reverse();
    }

    public static StringRestriction fromIn(InExpression inExpression) {
        Column column = (Column) inExpression.getLeftExpression();
        DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();
        StringRestriction restriction = new StringRestriction(inExpression, column);
        List<Expression> expressions = ((ExpressionList) inExpression.getRightItemsList()).getExpressions();
        List<String> values = expressions.stream().map(a -> DataTypesConverter.getInternalString(a, databaseType)).collect(Collectors.toList());
        restriction.setAllowedValues(values);
        return restriction;
    }

    public static StringRestriction fromLikeExpression(LikeExpression likeExpression) {
        Column column = (Column) likeExpression.getLeftExpression();
        String value = ((StringValue) likeExpression.getRightExpression()).getValue();

        LikeExpressionProperties properties = new LikeExpressionProperties(value, likeExpression.getEscape());
        StringRestriction restriction = new StringRestriction(likeExpression, column);
        restriction.setLikeExpressionProperties(properties);
        restriction.setNegated(likeExpression.isNot());
        return restriction;
    }

    @Override
    public String toString() {
        return "StringRestriction{" +
                getAttribute() +
                ", length=[" + allowedLength + "]" +
                (likeExpressionProperties == null ? "" : ", likeExpressionProperties=" + likeExpressionProperties) +
                (allowedValues == null ? "" : ", allowedValues=" + allowedValues) +
                ", isNegated=" + isNegated + '}';
    }
}
