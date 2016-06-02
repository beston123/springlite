package org.springlite.beans.factory.support;

import org.springlite.beans.ConstructorArgumentValues;
import org.springlite.beans.MutablePropertyValues;
import org.springlite.beans.factory.config.BeanDefinition;

/**
 * Created by swy on 2016/5/25.
 */
public class RootBeanDefinition extends AbstractBeanDefinition {

    private volatile Class<?> targetType;

    /**
     * Create a new RootBeanDefinition, to be configured through its bean
     * properties and configuration methods.
     * @see #setBeanClass
     * @see #setBeanClassName
     * @see #setScope
     * @see #setAutowireMode
     * @see #setDependencyCheck
     * @see #setConstructorArgumentValues
     * @see #setPropertyValues
     */
    public RootBeanDefinition() {
        super();
    }

    /**
     * Create a new RootBeanDefinition for a singleton.
     * @param beanClass the class of the bean to instantiate
     */
    public RootBeanDefinition(Class<?> beanClass) {
        super();
        setBeanClass(beanClass);
    }

    /**
     * Create a new RootBeanDefinition for a singleton,
     * using the given autowire mode.
     * @param beanClass the class of the bean to instantiate
     * @param autowireMode by name or type, using the constants in this interface
     * @param dependencyCheck whether to perform a dependency check for objects
     * (not applicable to autowiring a constructor, thus ignored there)
     */
    public RootBeanDefinition(Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
        super();
        setBeanClass(beanClass);
        setAutowireMode(autowireMode);
        if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
            setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
        }
    }

    /**
     * Create a new RootBeanDefinition for a singleton,
     * providing constructor arguments and property values.
     * @param beanClass the class of the bean to instantiate
     * @param cargs the constructor argument values to apply
     * @param pvs the property values to apply
     */
    public RootBeanDefinition(Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        super(cargs, pvs);
        setBeanClass(beanClass);
    }

    /**
     * Create a new RootBeanDefinition for a singleton,
     * providing constructor arguments and property values.
     * <p>Takes a bean class name to avoid eager loading of the bean class.
     * @param beanClassName the name of the class to instantiate
     */
    public RootBeanDefinition(String beanClassName) {
        setBeanClassName(beanClassName);
    }

    /**
     * Create a new RootBeanDefinition for a singleton,
     * providing constructor arguments and property values.
     * <p>Takes a bean class name to avoid eager loading of the bean class.
     * @param beanClassName the name of the class to instantiate
     * @param cargs the constructor argument values to apply
     * @param pvs the property values to apply
     */
    public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        super(cargs, pvs);
        setBeanClassName(beanClassName);
    }

    /**
     * Create a new RootBeanDefinition as deep copy of the given
     * bean definition.
     * @param original the original bean definition to copy from
     */
    public RootBeanDefinition(RootBeanDefinition original) {
        super((BeanDefinition) original);
        //this.allowCaching = original.allowCaching;
        //this.decoratedDefinition = original.decoratedDefinition;
        this.targetType = original.targetType;
        //this.isFactoryMethodUnique = original.isFactoryMethodUnique;
    }

    /**
     * Create a new RootBeanDefinition as deep copy of the given
     * bean definition.
     * @param original the original bean definition to copy from
     */
    public RootBeanDefinition(BeanDefinition original) {
        super(original);
    }

    public String getParentName() {
        return null;
    }

    public void setParentName(String parentName) {
        if (parentName != null) {
            throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
        }
    }

    /**
     * Specify the target type of this bean definition, if known in advance.
     */
    public void setTargetType(Class<?> targetType) {
        this.targetType = targetType;
    }

    /**
     * Return the target type of this bean definition, if known
     * (either specified in advance or resolved on first instantiation).
     */
    public Class<?> getTargetType() {
        return this.targetType;
    }


    @Override
    public RootBeanDefinition cloneBeanDefinition() {
        return new RootBeanDefinition(this);
    }

    @Override
    public BeanDefinition getOriginatingBeanDefinition() {
        return null;
    }
}
