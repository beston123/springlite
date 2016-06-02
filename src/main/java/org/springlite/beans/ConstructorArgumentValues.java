package org.springlite.beans;

import org.apache.commons.lang.Validate;
import org.springlite.util.ClassUtils;
import org.springlite.util.ObjectUtils;

import java.util.*;

/**
 * Created by swy on 2016/5/25.
 */
public class ConstructorArgumentValues {

    public static final int BY_INDEX = 0;

    public static final int BY_NAME = 1;

    public static final int BY_TYPE = 2;

    //private final Map<Integer, ValueHolder> indexedArgumentValues = new LinkedHashMap<Integer, ValueHolder>(0);
    private final Map<Integer, ValueHolder> indexedArgumentValues = new TreeMap<Integer, ValueHolder>(new Comparator<Integer>() {
        @Override
        public int compare(Integer t1, Integer t2) {
            return t1.compareTo(t2);
        }
    });

    private final List<ValueHolder> genericArgumentValues = new LinkedList<ValueHolder>();

    /**
     * 构造参数绑定方式
     */
    private int autowrieBy  = BY_INDEX;


    public int getAutowrieBy() {
        return autowrieBy;
    }

    public void setAutowrieBy(int autowrieBy) {
        this.autowrieBy = autowrieBy;
    }


    /**
     * Create a new empty ConstructorArgumentValues object.
     */
    public ConstructorArgumentValues() {

    }


    /**
     * Deep copy constructor.
     * @param original the ConstructorArgumentValues to copy
     */
    public ConstructorArgumentValues(ConstructorArgumentValues original) {
        addArgumentValues(original);
        this.autowrieBy = original.getAutowrieBy();
    }


    /**
     * Copy all given argument values into this object, using separate holder
     * instances to keep the values independent from the original object.
     * <p>Note: Identical ValueHolder instances will only be registered once,
     * to allow for merging and re-merging of argument value definitions. Distinct
     * ValueHolder instances carrying the same content are of course allowed.
     */
    public void addArgumentValues(ConstructorArgumentValues other) {
        if (other != null) {
            for (Map.Entry<Integer, ValueHolder> entry : other.indexedArgumentValues.entrySet()) {
                this.indexedArgumentValues.put(entry.getKey(), entry.getValue().copy());
            }
            for (ValueHolder valueHolder : other.genericArgumentValues) {
                if (!this.genericArgumentValues.contains(valueHolder)) {
                    this.genericArgumentValues.add(valueHolder.copy());
                }
            }
        }
    }

    /**
     * Add an argument value for the given index in the constructor argument list.
     * @param index the index in the constructor argument list
     * @param value the argument value
     */
    public void addIndexedArgumentValue(int index, Object value) {
        this.indexedArgumentValues.put(index, new ValueHolder(value));
    }

    /**
     * Add an argument value for the given index in the constructor argument list.
     * @param index the index in the constructor argument list
     * @param value the argument value
     * @param type the type of the constructor argument
     */
    public void addIndexedArgumentValue(int index, Object value, String type) {
        this.indexedArgumentValues.put(index, new ValueHolder(value, type));
    }

    /**
     * Check whether an argument value has been registered for the given index.
     * @param index the index in the constructor argument list
     */
    public boolean hasIndexedArgumentValue(int index) {
        return this.indexedArgumentValues.containsKey(index);
    }

    /**
     * Get argument value for the given index in the constructor argument list.
     * @param index the index in the constructor argument list
     * @param requiredType the type to match (can be {@code null} to match
     * untyped values only)
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getIndexedArgumentValue(int index, Class<?> requiredType) {
        return getIndexedArgumentValue(index, requiredType, null);
    }

    /**
     * Get argument value for the given index in the constructor argument list.
     * @param index the index in the constructor argument list
     * @param requiredType the type to match (can be {@code null} to match
     * untyped values only)
     * @param requiredName the type to match (can be {@code null} to match
     * unnamed values only)
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getIndexedArgumentValue(int index, Class<?> requiredType, String requiredName) {
        Validate.isTrue(index >= 0, "Index must not be negative");
        ValueHolder valueHolder = this.indexedArgumentValues.get(index);
        if (valueHolder != null &&
                (valueHolder.getType() == null ||
                        (requiredType != null && ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) &&
                (valueHolder.getName() == null ||
                        (requiredName != null && requiredName.equals(valueHolder.getName())))) {
            return valueHolder;
        }
        return null;
    }

    /**
     * Return the map of indexed argument values.
     * @return unmodifiable Map with Integer index as key and ValueHolder as value
     * @see ValueHolder
     */
    public Map<Integer, ValueHolder> getIndexedArgumentValues() {
        return Collections.unmodifiableMap(this.indexedArgumentValues);
    }


    /**
     * Add a generic argument value to be matched by type.
     * <p>Note: A single generic argument value will just be used once,
     * rather than matched multiple times.
     * @param value the argument value
     */
    public void addGenericArgumentValue(Object value) {
        this.genericArgumentValues.add(new ValueHolder(value));
    }

    /**
     * Add a generic argument value to be matched by type.
     * <p>Note: A single generic argument value will just be used once,
     * rather than matched multiple times.
     * @param value the argument value
     * @param type the type of the constructor argument
     */
    public void addGenericArgumentValue(Object value, String type) {
        this.genericArgumentValues.add(new ValueHolder(value, type));
    }


    public void addGenericArgumentValue(Object value, String name, String type) {
        this.genericArgumentValues.add(new ValueHolder(value, type, name));
    }


    /**
     * Look for a generic argument value that matches the given type.
     * @param requiredType the type to match
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getGenericArgumentValue(Class<?> requiredType) {
        return getGenericArgumentValue(requiredType, null, null);
    }

    /**
     * Look for a generic argument value that matches the given type.
     * @param requiredType the type to match
     * @param requiredName the name to match
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getGenericArgumentValue(Class<?> requiredType, String requiredName) {
        return getGenericArgumentValue(requiredType, requiredName, null);
    }

    /**
     * Look for the next generic argument value that matches the given type,
     * ignoring argument values that have already been used in the current
     * resolution process.
     * @param requiredType the type to match (can be {@code null} to find
     * an arbitrary next generic argument value)
     * @param requiredName the name to match (can be {@code null} to not
     * match argument values by name)
     * @param usedValueHolders a Set of ValueHolder objects that have already been used
     * in the current resolution process and should therefore not be returned again
     * @return the ValueHolder for the argument, or {@code null} if none found
     */
    public ValueHolder getGenericArgumentValue(Class<?> requiredType, String requiredName, Set<ValueHolder> usedValueHolders) {
        for (ValueHolder valueHolder : this.genericArgumentValues) {
            if (usedValueHolders != null && usedValueHolders.contains(valueHolder)) {
                continue;
            }
            if (valueHolder.getName() != null &&
                    (requiredName == null || !valueHolder.getName().equals(requiredName))) {
                continue;
            }
            if (valueHolder.getType() != null &&
                    (requiredType == null || !ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) {
                continue;
            }
            if (requiredType != null && valueHolder.getType() == null && valueHolder.getName() == null &&
                    !ClassUtils.isAssignableValue(requiredType, valueHolder.getValue())) {
                continue;
            }
            return valueHolder;
        }
        return null;
    }

    /**
     * Return the list of generic argument values.
     * @return unmodifiable List of ValueHolders
     * @see ValueHolder
     */
    public List<ValueHolder> getGenericArgumentValues() {
        return Collections.unmodifiableList(this.genericArgumentValues);
    }


    /**
     * Look for an argument value that either corresponds to the given index
     * in the constructor argument list or generically matches by type.
     * @param index the index in the constructor argument list
     * @param requiredType the parameter type to match
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getArgumentValue(int index, Class<?> requiredType) {
        return getArgumentValue(index, requiredType, null, null);
    }

    /**
     * Look for an argument value that either corresponds to the given index
     * in the constructor argument list or generically matches by type.
     * @param index the index in the constructor argument list
     * @param requiredType the parameter type to match
     * @param requiredName the parameter name to match
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName) {
        return getArgumentValue(index, requiredType, requiredName, null);
    }

    /**
     * Look for an argument value that either corresponds to the given index
     * in the constructor argument list or generically matches by type.
     * @param index the index in the constructor argument list
     * @param requiredType the parameter type to match (can be {@code null}
     * to find an untyped argument value)
     * @param requiredName the parameter name to match (can be {@code null}
     * to find an unnamed argument value)
     * @param usedValueHolders a Set of ValueHolder objects that have already
     * been used in the current resolution process and should therefore not
     * be returned again (allowing to return the next generic argument match
     * in case of multiple generic argument values of the same type)
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName, Set<ValueHolder> usedValueHolders) {
        Validate.isTrue(index >= 0, "Index must not be negative");
        ValueHolder valueHolder = getIndexedArgumentValue(index, requiredType, requiredName);
        if (valueHolder == null) {
            valueHolder = getGenericArgumentValue(requiredType, requiredName, usedValueHolders);
        }
        return valueHolder;
    }

    /**
     * Return the number of argument values held in this instance,
     * counting both indexed and generic argument values.
     */
    public int getArgumentCount() {
        return (this.indexedArgumentValues.size() + this.genericArgumentValues.size());
    }

    /**
     * Return if this holder does not contain any argument values,
     * neither indexed ones nor generic ones.
     */
    public boolean isEmpty() {
        return (this.indexedArgumentValues.isEmpty() && this.genericArgumentValues.isEmpty());
    }

    /**
     * Clear this holder, removing all argument values.
     */
    public void clear() {
        this.indexedArgumentValues.clear();
        this.genericArgumentValues.clear();
    }

    /**
     * Holder for a constructor argument value, with an optional type
     * attribute indicating the target type of the actual constructor argument.
     */
    public static class ValueHolder {

        private Object value;

        private String type;

        private String name;

        private boolean converted = false;

        private Object convertedValue;

        /**
         * Create a new ValueHolder for the given value.
         * @param value the argument value
         */
        public ValueHolder(Object value) {
            this.value = value;
        }

        /**
         * Create a new ValueHolder for the given value and type.
         * @param value the argument value
         * @param type the type of the constructor argument
         */
        public ValueHolder(Object value, String type) {
            this.value = value;
            this.type = type;
        }

        /**
         * Create a new ValueHolder for the given value, type and name.
         * @param value the argument value
         * @param type the type of the constructor argument
         * @param name the name of the constructor argument
         */
        public ValueHolder(Object value, String type, String name) {
            this.value = value;
            this.type = type;
            this.name = name;
        }

        /**
         * Set the value for the constructor argument.
         * @see
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * Return the value for the constructor argument.
         */
        public Object getValue() {
            return this.value;
        }

        /**
         * Set the type of the constructor argument.
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Return the type of the constructor argument.
         */
        public String getType() {
            return this.type;
        }

        /**
         * Set the name of the constructor argument.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Return the name of the constructor argument.
         */
        public String getName() {
            return this.name;
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

        /**
         * Determine whether the content of this ValueHolder is equal
         * to the content of the given other ValueHolder.
         * <p>Note that ValueHolder does not implement {@code equals}
         * directly, to allow for multiple ValueHolder instances with the
         * same content to reside in the same Set.
         */
        private boolean contentEquals(ValueHolder other) {
            return (this == other ||
                    (ObjectUtils.nullSafeEquals(this.value, other.value) && ObjectUtils.nullSafeEquals(this.type, other.type)));
        }

        /**
         * Determine whether the hash code of the content of this ValueHolder.
         * <p>Note that ValueHolder does not implement {@code hashCode}
         * directly, to allow for multiple ValueHolder instances with the
         * same content to reside in the same Set.
         */
        private int contentHashCode() {
            return ObjectUtils.nullSafeHashCode(this.value) * 29 + ObjectUtils.nullSafeHashCode(this.type);
        }

        /**
         * Create a copy of this ValueHolder: that is, an independent
         * ValueHolder instance with the same contents.
         */
        public ValueHolder copy() {
            ValueHolder copy = new ValueHolder(this.value, this.type, this.name);
            return copy;
        }
    }
}
