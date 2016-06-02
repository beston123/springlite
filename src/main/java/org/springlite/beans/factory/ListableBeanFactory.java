package org.springlite.beans.factory;

import org.springlite.beans.exception.BeansException;

import java.util.Map;

/**
 * ListableBeanFactory<p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/27
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface ListableBeanFactory extends BeanFactory {

    boolean containsBeanDefinition(String beanName);

    int getBeanDefinitionCount();

    String[] getBeanDefinitionNames();

    String[] getBeanNamesForType(Class<?> type);

    String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;

    <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
            throws BeansException;

}
