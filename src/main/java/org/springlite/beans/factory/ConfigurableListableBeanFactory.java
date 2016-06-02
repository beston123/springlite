package org.springlite.beans.factory;

import org.springlite.beans.factory.config.BeanDefinition;
import org.springlite.beans.exception.BeansException;
import org.springlite.beans.exception.NoSuchBeanDefinitionException;

/**
 * <p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/27
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface ConfigurableListableBeanFactory extends AutowireCapableBeanFactory,
        ConfigurableBeanFactory, ListableBeanFactory {

    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    void preInstantiateSingletons() throws BeansException;
}

