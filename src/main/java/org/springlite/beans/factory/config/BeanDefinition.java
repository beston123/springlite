package org.springlite.beans.factory.config;


import org.springlite.beans.ConstructorArgumentValues;
import org.springlite.beans.MutablePropertyValues;

public interface BeanDefinition {

    String SCOPE_SINGLETON = "singleton";

    String SCOPE_PROTOTYPE = "prototype";

    String getBeanName();

    String getBeanClassName();

//    String getFactoryBeanName();

//    String getFactoryMethodName();

    /**
     * Return the name of the current target scope for this bean,
     * or {@code null} if not known yet.
     */
    String getScope();

    /**
     * Return whether this bean should be lazily initialized, i.e. not
     * eagerly instantiated on startup. Only applicable to a singleton bean.
     */
    boolean isLazyInit();

    /**
     * Return the bean names that this bean depends on.
     */
    String[] getDependsOn();

    /**
     * Return whether this bean is a candidate for getting autowired into some other bean.
     */
    boolean isAutowireCandidate();

    /**
     * Return whether this bean is a primary autowire candidate.
     * If this value is true for exactly one bean among multiple
     * matching candidates, it will serve as a tie-breaker.
     */
    boolean isPrimary();


    /**
     * Return the constructor argument values for this bean.
     * <p>The returned instance can be modified during bean factory post-processing.
     * @return the ConstructorArgumentValues object (never {@code null})
     */
    ConstructorArgumentValues getConstructorArgumentValues();

    /**
     * Return the property values to be applied to a new instance of the bean.
     * <p>The returned instance can be modified during bean factory post-processing.
     * @return the MutablePropertyValues object (never {@code null})
     */
    MutablePropertyValues getPropertyValues();

    /**
     * Return whether this a <b>Singleton</b>, with a single, shared instance
     * returned on all calls.
     * @see #SCOPE_SINGLETON
     */
    boolean isSingleton();

    /**
     * Return whether this a <b>Prototype</b>, with an independent instance
     * returned for each call.
     * @see #SCOPE_PROTOTYPE
     */
    boolean isPrototype();

    /**
     * Return whether this bean is "abstract", that is, not meant to be instantiated.
     */
    boolean isAbstract();

    /**
     * Return a human-readable description of this bean definition.
     */
    String getDescription();


    String getInitMethodName();

    String getDestroyMethodName();

    /**
     * Return a description of the resource that this bean definition
     * came from (for the purpose of showing context in case of errors).
     */
    String getResourceDescription();

    /**
     * Return the originating BeanDefinition, or {@code null} if none.
     * Allows for retrieving the decorated bean definition, if any.
     * <p>Note that this method returns the immediate originator. Iterate through the
     * originator chain to find the original BeanDefinition as defined by the user.
     */
    BeanDefinition getOriginatingBeanDefinition();

}
