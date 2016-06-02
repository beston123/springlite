package org.springlite.beans.factory.config;

import org.springlite.beans.exception.BeansException;
import org.springlite.beans.factory.support.AbstractBeanDefinition;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

    /**
     * Apply this processor to the given bean instance before
     * its destruction. Can invoke custom destruction callbacks.
     * <p>Like DisposableBean's {@code destroy} and a custom destroy method,
     * this callback just applies to singleton beans in the factory (including
     * inner beans).
     * @param bean the bean instance to be destroyed
     * @param beanName the name of the bean
     * @throws org.springlite.beans.exception.BeansException in case of errors
     * @see org.springlite.beans.factory.DisposableBean
     * @see AbstractBeanDefinition#setDestroyMethodName
     */
    void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;

}
