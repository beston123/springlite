package org.springlite.beans;

import org.apache.commons.lang.Validate;
import org.springlite.util.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.io.Serializable;

/**
 * <p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/26
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
@SuppressWarnings("serial")
public class PropertyValue implements Serializable {

    private final String name;

    private final Object value;

    private boolean optional = false;

    private boolean converted = false;

    private Object convertedValue;

//    /** Package-visible field that indicates whether conversion is necessary */
//    volatile Boolean conversionNecessary;
//
//    /** Package-visible field for caching the resolved property path tokens */
//    volatile Object resolvedTokens;
//
//    /** Package-visible field for caching the resolved PropertyDescriptor */
//    volatile PropertyDescriptor resolvedDescriptor;


    /**
     * Create a new PropertyValue instance.
     * @param name the name of the property (never {@code null})
     * @param value the value of the property (possibly before type conversion)
     */
    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Copy constructor.
     * @param original the PropertyValue to copy (never {@code null})
     */
    public PropertyValue(PropertyValue original) {
        Validate.notNull(original, "Original must not be null");
        this.name = original.getName();
        this.value = original.getValue();
        this.optional = original.isOptional();
        this.converted = original.converted;
        this.convertedValue = original.convertedValue;
//        this.conversionNecessary = original.conversionNecessary;
//        this.resolvedTokens = original.resolvedTokens;
//        this.resolvedDescriptor = original.resolvedDescriptor;
    }

    /**
     * Constructor that exposes a new value for an original value holder.
     * The original holder will be exposed as source of the new holder.
     * @param original the PropertyValue to link to (never {@code null})
     * @param newValue the new value to apply
     */
    public PropertyValue(PropertyValue original, Object newValue) {
        Validate.notNull(original, "Original must not be null");
        this.name = original.getName();
        this.value = newValue;
        this.optional = original.isOptional();
//        this.conversionNecessary = original.conversionNecessary;
//        this.resolvedTokens = original.resolvedTokens;
//        this.resolvedDescriptor = original.resolvedDescriptor;
//        copyAttributesFrom(original);
    }


    /**
     * Return the name of the property.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return the value of the property.
     * <p>Note that type conversion will <i>not</i> have occurred here.
     * It is the responsibility of the BeanWrapper implementation to
     * perform type conversion.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Return the original PropertyValue instance for this value holder.
     * @return the original PropertyValue (either a source of this
     * value holder or this value holder itself).
     */
    public PropertyValue getOriginalPropertyValue() {
        PropertyValue original = this;

        return original;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isOptional() {
        return this.optional;
    }

    /**
     * Return whether this holder contains a converted value already ({@code true}),
     * or whether the value still needs to be converted ({@code false}).
     */
    public synchronized boolean isConverted() {
        return this.converted;
    }

    /**
     * Set the converted value of the constructor argument,
     * after processed type conversion.
     */
    public synchronized void setConvertedValue(Object value) {
        this.converted = true;
        this.convertedValue = value;
    }

    /**
     * Return the converted value of the constructor argument,
     * after processed type conversion.
     */
    public synchronized Object getConvertedValue() {
        return this.convertedValue;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PropertyValue)) {
            return false;
        }
        PropertyValue otherPv = (PropertyValue) other;
        return (this.name.equals(otherPv.name) &&
                ObjectUtils.nullSafeEquals(this.value, otherPv.value));
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
    }

    @Override
    public String toString() {
        return "bean property '" + this.name + "'";
    }


}
