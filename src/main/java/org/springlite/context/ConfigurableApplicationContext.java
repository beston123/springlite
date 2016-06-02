package org.springlite.context;

import org.springlite.beans.exception.BeansException;

/**
 * <p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/27
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface ConfigurableApplicationContext extends ApplicationContext{

    /**
     * Load or refresh the persistent representation of the configuration,
     * which might an XML file, properties file, or relational database schema.
     * <p>As this is a startup method, it should destroy already created singletons
     * if it fails, to avoid dangling resources. In other words, after invocation
     * of that method, either all or no singletons at all should be instantiated.
     * @throws BeansException if the bean factory could not be initialized
     * @throws IllegalStateException if already initialized and multiple refresh
     * attempts are not supported
     */
    void refresh() throws BeansException, IllegalStateException;
}
