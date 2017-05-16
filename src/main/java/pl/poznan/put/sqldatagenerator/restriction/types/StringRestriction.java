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

import static java.util.Collections.emptyList;
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
    private List<String> allowedValues;
    private List<String> notAllowedValues;
    private Generex generex = null;

    public StringRestriction(Attribute attribute, Range<Integer> allowedLength,
                             List<LikeProperty> likeProperties, List<String> allowedValues,
                             List<String> notAllowedValues) {
        this(attribute);
        this.allowedLength = allowedLength;
        this.likeProperties = likeProperties;
        this.allowedValues = allowedValues;
        this.notAllowedValues = notAllowedValues;
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

    public boolean containsNonNegatedLikeProperties() {
        return likeProperties != null && likeProperties.size()>0 && !likeProperties.get(0).isNegated();
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public boolean setAllowedValues(List<String> allowedValues) {
        if (this.allowedValues == null && allowedValues == null) {
            return false;
        }
        if (this.allowedValues != null && this.allowedValues.equals(allowedValues)) {
            return false;
        }
        this.allowedValues = allowedValues;
        return true;
    }

    public boolean containsAllowedValues() {
        return allowedValues != null && allowedValues.size() > 0;
    }

    public List<String> getNotAllowedValues() {
        return notAllowedValues;
    }

    public void setNotAllowedValues(List<String> notAllowedValues) {
        this.notAllowedValues = notAllowedValues;
    }

    public boolean addNotAllowedValue(String value) {
        if (notAllowedValues == null) {
            notAllowedValues = new ArrayList<>();
        }
        if (notAllowedValues.contains(value)) {
            return false;
        }
        this.notAllowedValues.add(value);
        return true;
    }

    public boolean containsNotAllowedValues() {
        return notAllowedValues != null && notAllowedValues.size() > 0;
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
        if(likeProperties != null) {
            likeProperties.stream().forEach(a -> a.reverse());
        }
        List<String> tempList = allowedValues;
        allowedValues = notAllowedValues;
        notAllowedValues = tempList;

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
            clone.setLikeProperties(likeProperties.stream().map(LikeProperty::new).collect(toList()));
        }
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
        if(isNegated) {
            restriction.setNotAllowedValues(singletonList(getInternalString(getValueExpression(expression), databaseType)));
        } else {
            restriction.setAllowedValues(singletonList(getInternalString(getValueExpression(expression), databaseType)));
        }
        return restriction;
    }

    public static StringRestriction fromIn(InExpression inExpression) {
        Column column = (Column) inExpression.getLeftExpression();
        DatabaseType databaseType = AttributesMap.get(column).getDatabaseType();
        List<String> values = ((ExpressionList) inExpression.getRightItemsList()).getExpressions()
                .stream().map(a -> getInternalString(a, databaseType)).collect(toList());

        StringRestriction restriction = new StringRestriction(inExpression, column);
        restriction.setAllowedValues(values);

        return restriction;
    }

    public static StringRestriction fromLikeExpression(LikeExpression likeExpression) {
        Column column = (Column) likeExpression.getLeftExpression();
        String value = ((StringValue) likeExpression.getRightExpression()).getValue();
        LikeProperty property = new LikeProperty(value, likeExpression.isNot());

        StringRestriction restriction = new StringRestriction(likeExpression, column);
        restriction.setLikeProperties(Collections.singletonList(property));

        return restriction;
    }

    private void prepareGenerex() {
        if (likeProperties.size() > 1) {
            throw new NotImplementedException("LikeProperties size >1 is not allowed");
        }

        generex = new Generex(likeProperties.get(0).getRegex());
    }

    @Override
    public String toString() {
        return "StringRestriction{" +
                getAttribute() +
                ", length=[" + allowedLength + "]" +
                (likeProperties == null ? "" : ", likeProperties=" + likeProperties) +
                (allowedValues == null ? "" : ", allowedValues=" + allowedValues) +
                (notAllowedValues == null ? "" : ", notAllowedValues=" + notAllowedValues) + '}';
    }

    public static class LikeProperty {
        private final String like;
        private boolean isNegated;


        public LikeProperty(String like, boolean isNegated) {
            this.like = like;
            this.isNegated = isNegated;
        }

        public LikeProperty(LikeProperty likeProperty) {
            this(likeProperty.getLike(), likeProperty.isNegated());
        }

        public String getLike() {
            return like;
        }

        public String getRegex() {
            return like.replace("%", "[a-zA-Z]*?").replace("_", "[a-zA-Z]");
        }

        public boolean isNegated() {
            return isNegated;
        }

        public LikeProperty reverse() {
            this.isNegated = !isNegated;
            return this;
        }

        @Override
        public String toString() {
            return "LikeProperty{" +
                    "like=" + like +
                    ", isNegated=" + isNegated + '}';
        }
    }
}
