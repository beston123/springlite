package org.springlite.beans;

/**
 * <p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/26
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface PropertyValues {

    /**
     * Return an array of the PropertyValue objects held in this object.
     */
    PropertyValue[] getPropertyValues();

    /**
     * Return the property value with the given name, if any.
     * @param propertyName the name to search for
     * @return the property value, or {@code null}
     */
    PropertyValue getPropertyValue(String propertyName);

    /**
     * Return the changes since the previous PropertyValues.
     * Subclasses should also override {@code equals}.
     * @param old old property values
     * @return PropertyValues updated or new properties.
     * Return empty PropertyValues if there are no changes.
     * @see Object#equals
     */
    PropertyValues changesSince(PropertyValues old);

    /**
     * Is there a property value (or other processing entry) for this property?
     * @param propertyName the name of the property we're interested in
     * @return whether there is a property value for this property
     */
    boolean contains(String propertyName);

    /**
     * Does this holder not contain any PropertyValue objects at all?
     */
    boolean isEmpty();

}

