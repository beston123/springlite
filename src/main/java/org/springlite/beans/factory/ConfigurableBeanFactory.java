package org.springlite.beans.factory;

import org.springlite.beans.factory.config.BeanPostProcessor;

/**
 * ConfigurableBeanFactory<p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/27
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface ConfigurableBeanFactory extends BeanFactory{

    void setBeanClassLoader(ClassLoader beanClassLoader) ;

    ClassLoader getBeanClassLoader();

    /**
     * Add a new processor that will get applied to beans created
     * by this factory. To be invoked during factory configuration.
     * <p>Note: Post-processors submitted here will be applied in the order of
     * registration;
     * @param beanPostProcessor the post-processor to register
     */
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    /**
     * Return the current number of registered BeanPostProcessors, if any.
     */
    int getBeanPostProcessorCount();

    /**
     * Destroy the given bean instance (usually a prototype instance
     * obtained from this factory) according to its bean definition.
     * <p>Any exception that arises during destruction should be caught
     * and logged instead of propagated to the caller of this method.
     * @param beanName the name of the bean definition
     * @param beanInstance the bean instance to destroy
     */
    void destroyBean(String beanName, Object beanInstance);

    /**
     * Destroy the specified scoped bean in the current target scope, if any.
     * <p>Any exception that arises during destruction should be caught
     * and logged instead of propagated to the caller of this method.
     * @param beanName the name of the scoped bean
     */
    void destroyScopedBean(String beanName);

    /**
     * Destroy all singleton beans in this factory, including inner beans that have
     * been registered as disposable. To be called on shutdown of a factory.
     * <p>Any exception that arises during destruction should be caught
     * and logged instead of propagated to the caller of this method.
     */
    void destroySingletons();

}
