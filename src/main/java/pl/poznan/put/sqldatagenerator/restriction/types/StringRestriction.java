package pl.poznan.put.sqldatagenerator.restriction.types;

import com.google.common.collect.Range;
import com.mifmif.common.regex.Generex;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(StringRestriction.class);

    private static final Configuration configuration = Configuration.getInstance();

    private static final Integer DEFAULT_MIN_LENGTH = configuration.getIntegerProperty(MIN_STRING_LENGTH, 5);

    private static final Integer DEFAULT_MAX_LENGTH = configuration.getIntegerProperty(MAX_STRING_LENGTH, 20);
    private Range<Integer> allowedLength;

    private List<LikeProperty> likeProperties;
    private List<AllowedValue> allowedValues;
    Generex generex = null;

    public StringRestriction(Attribute attribute, Range<Integer> allowedLength,
                             List<LikeProperty> likeProperties, List<AllowedValue> allowedValues) {
        this(attribute);
        this.allowedLength = allowedLength;
        this.likeProperties = likeProperties;
        this.allowedValues = allowedValues;
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
        return likeProperties != null && likeProperties.size()>0;
    }

    public List<AllowedValue> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<AllowedValue> allowedValues) {
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

    @Override
    public Restriction reverse() {
        logger.warn("REVERSING STRING RESTRICTION!!!!!!!!!!!!!!!!!!!!!!!");
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
        // TODO deep copy of elements needed?
        return clone;
    }

    public static StringRestriction fromEquals(EqualsTo equalsTo) {
        return fromEqualsInternal(equalsTo, false);
    }

    public static StringRestriction fromNotEquals(NotEqualsTo notEqualsTo) {
        return (StringRestriction) fromEqualsInternal(notEqualsTo, true);
    }

    private static StringRestriction fromEqualsInternal(BinaryExpression expression, boolean isNegated) {
        Column column = getColumn(expression);
        DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();

        StringRestriction restriction = new StringRestriction(expression, column);
        AllowedValue allowedValues = new AllowedValue(getInternalString(getValueExpression(expression), databaseType), isNegated);
        restriction.setAllowedValues(singletonList(allowedValues));
        return restriction;
    }

    public static StringRestriction fromIn(InExpression inExpression) {
        Column column = (Column) inExpression.getLeftExpression();
        DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();
        List<Expression> expressionValues = ((ExpressionList) inExpression.getRightItemsList()).getExpressions();
        List<AllowedValue> allowedValues = expressionValues.stream().map(a -> new AllowedValue(getInternalString(a, databaseType), inExpression.isNot())).collect(toList());

        StringRestriction restriction = new StringRestriction(inExpression, column);
        restriction.setAllowedValues(allowedValues);
        return restriction;
    }

    public static StringRestriction fromLikeExpression(LikeExpression likeExpression) {
        Column column = (Column) likeExpression.getLeftExpression();
        String value = ((StringValue) likeExpression.getRightExpression()).getValue();
        LikeProperty property = new LikeProperty(value, likeExpression.getEscape(), likeExpression.isNot());

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
                (allowedValues == null ? "" : ", allowedValues=" + allowedValues) + '}';
    }

    public static class LikeProperty {
        private final String escapeCharacter;
        //TODO do czego ten escapeCharacter potrzebny, co to jest?
        private final String like;
        private boolean isNegated;


        public LikeProperty(String like, String escapeCharacter, boolean isNegated) {
            this.like = like;
            this.escapeCharacter = escapeCharacter;
            this.isNegated = isNegated;
        }

        public LikeProperty(LikeProperty likeProperty) {
            this(likeProperty.getLike(), likeProperty.getEscapeCharacter(), likeProperty.isNegated());
        }

        public String getLike() {
            return like;
        }

        public String getEscapeCharacter() {
            return escapeCharacter;
        }

        public boolean isNegated() {
            return isNegated;
        }

        public LikeProperty reverse() {
            this.isNegated = !isNegated;
            return this;
        }
    }

    public static class AllowedValue {
        private final String value;
        private boolean isNegated;

        public AllowedValue(String value, boolean isNegated) {
            this.value = value;
            this.isNegated = isNegated;
        }

        public String getValue() {
            return value;
        }

        public boolean isNegated() {
            return isNegated;
        }

        public AllowedValue reverse() {
            this.isNegated = !isNegated;
            return this;
        }
    }
}
