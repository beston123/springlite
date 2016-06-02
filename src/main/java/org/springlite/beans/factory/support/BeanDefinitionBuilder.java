package org.springlite.beans.factory.support;

import org.springlite.beans.BeanReference;
import org.springlite.util.ObjectUtils;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */

public class BeanDefinitionBuilder {

    /**
     * The {@code BeanDefinition} instance we are creating.
     */
    private AbstractBeanDefinition beanDefinition;

    /**
     * Our current position with respect to constructor args.
     */
    private int constructorArgIndex;



//    /**
//     * Create a new {@code BeanDefinitionBuilder} used to construct a {@link GenericBeanDefinition}.
//     */
//    public static BeanDefinitionBuilder genericBeanDefinition() {
//        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
//        builder.beanDefinition = new GenericBeanDefinition();
//        return builder;
//    }
//
//    /**
//     * Create a new {@code BeanDefinitionBuilder} used to construct a {@link GenericBeanDefinition}.
//     * @param beanClass the {@code Class} of the bean that the definition is being created for
//     */
//    public static BeanDefinitionBuilder genericBeanDefinition(Class beanClass) {
//        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
//        builder.beanDefinition = new GenericBeanDefinition();
//        builder.beanDefinition.setBeanClass(beanClass);
//        return builder;
//    }
//
//    /**
//     * Create a new {@code BeanDefinitionBuilder} used to construct a {@link GenericBeanDefinition}.
//     * @param beanClassName the class name for the bean that the definition is being created for
//     */
//    public static BeanDefinitionBuilder genericBeanDefinition(String beanClassName) {
//        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
//        builder.beanDefinition = new GenericBeanDefinition();
//        builder.beanDefinition.setBeanClassName(beanClassName);
//        return builder;
//    }

    /**
     * Create a new {@code BeanDefinitionBuilder} used to construct a {@link RootBeanDefinition}.
     * @param beanClass the {@code Class} of the bean that the definition is being created for
     */
    public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass) {
        return rootBeanDefinition(beanClass, null);
    }

    /**
     * Create a new {@code BeanDefinitionBuilder} used to construct a {@link RootBeanDefinition}.
     * @param beanClass the {@code Class} of the bean that the definition is being created for
     * @param factoryMethodName the name of the method to use to construct the bean instance
     */
    public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass, String factoryMethodName) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
        builder.beanDefinition = new RootBeanDefinition();
        builder.beanDefinition.setBeanClass(beanClass);
        //builder.beanDefinition.setFactoryMethodName(factoryMethodName);
        return builder;
    }

    /**
     * Create a new {@code BeanDefinitionBuilder} used to construct a {@link RootBeanDefinition}.
     * @param beanClassName the class name for the bean that the definition is being created for
     */
    public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName) {
        return rootBeanDefinition(beanClassName, null);
    }

    /**
     * Create a new {@code BeanDefinitionBuilder} used to construct a {@link RootBeanDefinition}.
     * @param beanClassName the class name for the bean that the definition is being created for
     * @param factoryMethodName the name of the method to use to construct the bean instance
     */
    public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName, String factoryMethodName) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
        builder.beanDefinition = new RootBeanDefinition();
        builder.beanDefinition.setBeanClassName(beanClassName);
        //builder.beanDefinition.setFactoryMethodName(factoryMethodName);
        return builder;
    }

//    /**
//     * Create a new {@code BeanDefinitionBuilder} used to construct a {@link ChildBeanDefinition}.
//     * @param parentName the name of the parent bean
//     */
//    public static BeanDefinitionBuilder childBeanDefinition(String parentName) {
//        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
//        builder.beanDefinition = new ChildBeanDefinition(parentName);
//        return builder;
//    }



    /**
     * Enforce the use of factory methods.
     */
    private BeanDefinitionBuilder() {
    }

    /**
     * Return the current BeanDefinition object in its raw (unvalidated) form.
     * @see #getBeanDefinition()
     */
    public AbstractBeanDefinition getRawBeanDefinition() {
        return this.beanDefinition;
    }

    /**
     * Validate and return the created BeanDefinition object.
     */
    public AbstractBeanDefinition getBeanDefinition() {
        //this.beanDefinition.validate();
        return this.beanDefinition;
    }


//    /**
//     * Set the name of the parent definition of this bean definition.
//     */
//    public BeanDefinitionBuilder setParentName(String parentName) {
//        this.beanDefinition.setParentName(parentName);
//        return this;
//    }
//
//    /**
//     * Set the name of the factory method to use for this definition.
//     */
//    public BeanDefinitionBuilder setFactoryMethod(String factoryMethod) {
//        this.beanDefinition.setFactoryMethodName(factoryMethod);
//        return this;
//    }

//    /**
//     * Set the name of the factory bean to use for this definition.
//     * @deprecated since Spring 2.5, in favor of preparing this on the
//     * {@link #getRawBeanDefinition() raw BeanDefinition object}
//     */
//    @Deprecated
//    public BeanDefinitionBuilder setFactoryBean(String factoryBean, String factoryMethod) {
//        this.beanDefinition.setFactoryBeanName(factoryBean);
//        this.beanDefinition.setFactoryMethodName(factoryMethod);
//        return this;
//    }

    public BeanDefinitionBuilder setBeanName(String beanName){
        this.beanDefinition.setBeanName(beanName);
        return this;
    }

    /**
     * Add an indexed constructor arg value. The current index is tracked internally
     * and all additions are at the present point.
     */
    public BeanDefinitionBuilder addConstructorArgValue(Object value) {
        this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
                this.constructorArgIndex++, value);
        return this;
    }

    /**
     * Add a reference to a named bean as a constructor arg.
     * @see #addConstructorArgValue(Object)
     */
    public BeanDefinitionBuilder addConstructorArgReference(String beanName) {
        this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
                this.constructorArgIndex++, new BeanReference(beanName));
        return this;
    }

    public BeanDefinitionBuilder addIndexConstructorArgValue(int index, Object value, String type) {
        this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
                index, value, type);
        return this;
    }

    public BeanDefinitionBuilder addGenericConstructorArgValue(Object value, String name, String type) {
        this.beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(
                value, name, type);
        return this;
    }

    /**
     * Add the supplied property value under the given name.
     */
    public BeanDefinitionBuilder addPropertyValue(String name, Object value) {
        this.beanDefinition.getPropertyValues().add(name, value);
        return this;
    }

    /**
     * Add a reference to the specified bean name under the property specified.
     * @param name the name of the property to add the reference to
     * @param beanName the name of the bean being referenced
     */
    public BeanDefinitionBuilder addPropertyReference(String name, String beanName) {
        this.beanDefinition.getPropertyValues().add(name, new BeanReference(beanName));
        return this;
    }

    /**
     * Set the init method for this definition.
     */
    public BeanDefinitionBuilder setInitMethodName(String methodName) {
        this.beanDefinition.setInitMethodName(methodName);
        return this;
    }

    /**
     * Set the destroy method for this definition.
     */
    public BeanDefinitionBuilder setDestroyMethodName(String methodName) {
        this.beanDefinition.setDestroyMethodName(methodName);
        return this;
    }


    /**
     * Set the scope of this definition.
     * @see org.springlite.beans.factory.config.BeanDefinition#SCOPE_SINGLETON
     * @see org.springlite.beans.factory.config.BeanDefinition#SCOPE_PROTOTYPE
     */
    public BeanDefinitionBuilder setScope(String scope) {
        this.beanDefinition.setScope(scope);
        return this;
    }


    /**
     * Set whether or not this definition is abstract.
     */
    public BeanDefinitionBuilder setAbstract(boolean flag) {
        this.beanDefinition.setAbstract(flag);
        return this;
    }

    /**
     * Set whether beans for this definition should be lazily initialized or not.
     */
    public BeanDefinitionBuilder setLazyInit(boolean lazy) {
        this.beanDefinition.setLazyInit(lazy);
        return this;
    }

    /**
     * Set the autowire mode for this definition.
     */
    public BeanDefinitionBuilder setAutowireMode(int autowireMode) {
        beanDefinition.setAutowireMode(autowireMode);
        return this;
    }

    /**
     * Set the depency check mode for this definition.
     */
    public BeanDefinitionBuilder setDependencyCheck(int dependencyCheck) {
        beanDefinition.setDependencyCheck(dependencyCheck);
        return this;
    }

    /**
     * Append the specified bean name to the list of beans that this definition
     * depends on.
     */
    public BeanDefinitionBuilder addDependsOn(String beanName) {
        if (this.beanDefinition.getDependsOn() == null) {
            this.beanDefinition.setDependsOn(new String[] {beanName});
        }
        else {
            String[] added = ObjectUtils.addObjectToArray(this.beanDefinition.getDependsOn(), beanName);
            this.beanDefinition.setDependsOn(added);
        }
        return this;
    }

//    /**
//     * Set the role of this definition.
//     */
//    public BeanDefinitionBuilder setRole(int role) {
//        this.beanDefinition.setRole(role);
//        return this;
//    }
//
//    /**
//     * Set the source of this definition.
//     * @deprecated since Spring 2.5, in favor of preparing this on the
//     * {@link #getRawBeanDefinition() raw BeanDefinition object}
//     */
//    @Deprecated
//    public BeanDefinitionBuilder setSource(Object source) {
//        this.beanDefinition.setSource(source);
//        return this;
//    }
//
//    /**
//     * Set the description associated with this definition.
//     * @deprecated since Spring 2.5, in favor of preparing this on the
//     * {@link #getRawBeanDefinition() raw BeanDefinition object}
//     */
//    @Deprecated
//    public BeanDefinitionBuilder setResourceDescription(String resourceDescription) {
//        this.beanDefinition.setResourceDescription(resourceDescription);
//        return this;
//    }

}
