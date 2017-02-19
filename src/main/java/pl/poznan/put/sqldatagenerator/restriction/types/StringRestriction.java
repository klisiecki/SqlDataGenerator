package pl.poznan.put.sqldatagenerator.restriction.types;

import com.google.common.collect.Range;
import com.mifmif.common.regex.Generex;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.exception.NotImplementedException;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.AttributesMap;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DatabaseType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys.MAX_STRING_LENGTH;
import static pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys.MIN_STRING_LENGTH;
import static pl.poznan.put.sqldatagenerator.generator.datatypes.DataTypesConverter.getInternalString;
import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.getColumn;
import static pl.poznan.put.sqldatagenerator.restriction.SQLExpressionsUtils.getValueExpression;

public class StringRestriction extends OneAttributeRestriction {

    private static final Configuration configuration = Configuration.getInstance();

    private static final Integer DEFAULT_MIN_LENGTH = configuration.getIntegerProperty(MIN_STRING_LENGTH, 5);

    private static final Integer DEFAULT_MAX_LENGTH = configuration.getIntegerProperty(MAX_STRING_LENGTH, 20);
    private Range<Integer> allowedLength;

    private List<LikeProperty> likeProperties;
    private List<String> allowedValues;
    Generex generex = null;
    private boolean isNegated;

    public StringRestriction(Attribute attribute, Range<Integer> allowedLength,
                             List<LikeProperty> likeProperties, List<String> allowedValues, boolean isNegated) {
        this(attribute);
        this.allowedLength = allowedLength;
        this.likeProperties = likeProperties;
        this.allowedValues = allowedValues;
        this.isNegated = isNegated;
    }

    private StringRestriction(Expression expression, Column column) {
        super(expression, column);
        initDefault();
    }

    private StringRestriction(Attribute attribute) {
        super(attribute);
        initDefault();
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

    public List<LikeProperty> getLikeProperties() {
        return likeProperties;
    }

    public void setLikeProperties(List<LikeProperty> likeProperties) {
        this.likeProperties = likeProperties;
        generex = null;
    }

    public boolean containsLikeProperties() {
        return likeProperties != null && likeProperties.size()>0 && !isNegated; // todo !isNegated to taki workaround na obłsugę zanegowanych likePropertiesów. Generujemy losowe i mamy nadzieję że będzie wystaczająco dobrze
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public boolean containsAllowedValues() {
        return allowedValues != null && allowedValues.size() > 0;
    }

    public Generex getGenerex() {
        if(likeProperties.size() == 0) {
            return null;
        }
        if(generex == null) {
            prepareGenerex();
        }
        return generex;
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
    public Restriction copy() {
        StringRestriction clone = new StringRestriction(this.getAttribute());
        clone.setMinLength(getMinLength());
        clone.setMaxLength(getMaxLength());
        if (allowedValues != null) {
            clone.setAllowedValues(new ArrayList<>(allowedValues));
        }
        if (likeProperties != null) {
            clone.setLikeProperties(new ArrayList<>(likeProperties));
        }
        clone.setNegated(isNegated);
        return clone;
    }

    public static StringRestriction fromEquals(EqualsTo equalsTo) {
        return fromEqualsInternal(equalsTo);
    }

    public static StringRestriction fromNotEquals(NotEqualsTo notEqualsTo) {
        return (StringRestriction) fromEqualsInternal(notEqualsTo).reverse();
    }

    private static StringRestriction fromEqualsInternal(BinaryExpression expression) {
        Column column = getColumn(expression);
        DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();

        StringRestriction restriction = new StringRestriction(expression, column);
        restriction.setAllowedValues(singletonList(getInternalString(getValueExpression(expression), databaseType)));
        return restriction;
    }

    public static StringRestriction fromIn(InExpression inExpression) {
        Column column = (Column) inExpression.getLeftExpression();
        DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();
        List<Expression> expressionValues = ((ExpressionList) inExpression.getRightItemsList()).getExpressions();
        List<String> values = expressionValues.stream().map(a -> getInternalString(a, databaseType)).collect(toList());

        StringRestriction restriction = new StringRestriction(inExpression, column);
        restriction.setAllowedValues(values);
        return restriction;
    }

    public static StringRestriction fromLikeExpression(LikeExpression likeExpression) {
        Column column = (Column) likeExpression.getLeftExpression();
        String value = ((StringValue) likeExpression.getRightExpression()).getValue();
        LikeProperty property = new LikeProperty(value, likeExpression.getEscape());

        StringRestriction restriction = new StringRestriction(likeExpression, column);
        restriction.setLikeProperties(Collections.singletonList(property));
//        restriction.setNegated(likeExpression.isNot()); \\ todo NOWE uniknięcie podwójnego NEGATE
        return restriction;
    }

    private void prepareGenerex() {
        if (likeProperties.size() > 1) {
            throw new NotImplementedException("LikeProperties size >1 is not allowed");
        }

        String regexp = likeProperties.get(0).getLike();
        regexp = regexp.replace("%", ".*?").replace("_", ".");

        generex = new Generex(regexp);
    }

    @Override
    public String toString() {
        return "StringRestriction{" +
                getAttribute() +
                ", length=[" + allowedLength + "]" +
                (likeProperties == null ? "" : ", likeProperties=" + likeProperties) +
                (allowedValues == null ? "" : ", allowedValues=" + allowedValues) +
                ", isNegated=" + isNegated + '}';
    }

    public static class LikeProperty {
        private final String escapeCharacter;
        //TODO do czego ten escapeCharacter potrzebny, co to jest?
        private final String like;


        public LikeProperty(String like, String escapeCharacter) {
            this.like = like;
            this.escapeCharacter = escapeCharacter;
        }

        public LikeProperty(LikeProperty likeProperty) {
            this(likeProperty.getLike(), likeProperty.getEscapeCharacter());
        }

        public String getLike() {
            return like;
        }

        public String getEscapeCharacter() {
            return escapeCharacter;
        }
    }
}
