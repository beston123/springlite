package org.springlite.beans.factory;

import org.springlite.beans.exception.BeansException;
import org.springlite.beans.factory.config.BeanPostProcessor;

/**
 * Created by swy on 2016/5/25.
 */
public interface AutowireCapableBeanFactory extends BeanFactory{


    int AUTOWIRE_NO = 0;

    int AUTOWIRE_BY_NAME = 1;

    int AUTOWIRE_BY_TYPE = 2;

    int AUTOWIRE_CONSTRUCTOR = 3;

    int AUTOWIRE_AUTODETECT = 4;

    //-------------------------------------------------------------------------
    // Typical methods for creating and populating external bean instances
    //-------------------------------------------------------------------------

    /**
     * Fully create a new bean instance of the given class.
     * <p>Performs full initialization of the bean, including all applicable
     * {@link BeanPostProcessor BeanPostProcessors}.
     * <p>Note: This is intended for creating a fresh instance, populating annotated
     * fields and methods as well as applying all standard bean initialiation callbacks.
     * It does <i>not</> imply traditional by-name or by-type autowiring of properties;
     * use {@link #createBean(Class, int, boolean)} for that purposes.
     * @param beanClass the class of the bean to create
     * @return the new bean instance
     * @throws BeansException if instantiation or wiring failed
     */
    <T> T createBean(Class<T> beanClass) throws BeansException;

    /**
     * Populate the given bean instance through applying after-instantiation callbacks
     * and bean property post-processing (e.g. for annotation-driven injection).
     * <p>Note: This is essentially intended for (re-)populating annotated fields and
     * methods, either for new instances or for deserialized instances. It does
     * <i>not</i> imply traditional by-name or by-type autowiring of properties;
     * use {@link #autowireBeanProperties} for that purposes.
     * @param existingBean the existing bean instance
     * @throws BeansException if wiring failed
     */
    void autowireBean(Object existingBean) throws BeansException;

    /**
     * Configure the given raw bean: autowiring bean properties, applying
     * bean property values, applying factory callbacks such as {@code setBeanName}
     * and {@code setBeanFactory}, and also applying all bean post processors
     * (including ones which might wrap the given raw bean).
     * <p>This is effectively a superset of what {@link #initializeBean} provides,
     * fully applying the configuration specified by the corresponding bean definition.
     * <b>Note: This method requires a bean definition for the given name!</b>
     * @param existingBean the existing bean instance
     * @param beanName the name of the bean, to be passed to it if necessary
     * (a bean definition of that name has to be available)
     * @return the bean instance to use, either the original or a wrapped one
     * @throws org.springlite.beans.exception.NoSuchBeanDefinitionException
     * if there is no bean definition with the given name
     * @throws BeansException if the initialization failed
     * @see #initializeBean
     */
    Object configureBean(Object existingBean, String beanName) throws BeansException;


    //-------------------------------------------------------------------------
    // Specialized methods for fine-grained control over the bean lifecycle
    //-------------------------------------------------------------------------

    /**
     * Fully create a new bean instance of the given class with the specified
     * autowire strategy. All constants defined in this interface are supported here.
     * <p>Performs full initialization of the bean, including all applicable
     * {@link BeanPostProcessor BeanPostProcessors}. This is effectively a superset
     * of what {@link #autowire} provides, adding {@link #initializeBean} behavior.
     * @param beanClass the class of the bean to create
     * @param autowireMode by name or type, using the constants in this interface
     * @param dependencyCheck whether to perform a dependency check for objects
     * (not applicable to autowiring a constructor, thus ignored there)
     * @return the new bean instance
     * @throws BeansException if instantiation or wiring failed
     * @see #AUTOWIRE_NO
     * @see #AUTOWIRE_BY_NAME
     * @see #AUTOWIRE_BY_TYPE
     * @see #AUTOWIRE_CONSTRUCTOR
     */
    Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

    Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

    void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
            throws BeansException;

    void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;


    Object initializeBean(Object existingBean, String beanName) throws BeansException;

    Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
            throws BeansException;

    Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
            throws BeansException;


}
