package org.springlite.beans.factory.support;

import org.springlite.beans.exception.BeanDefinitionStoreException;
import org.springlite.beans.factory.BeanNameGenerator;
import org.springlite.beans.factory.support.BeanDefinitionRegistry;
import org.springlite.core.io.Resource;
import org.springlite.core.io.ResourceLoader;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface BeanDefinitionReader {

    /**
     * Return the bean factory to register the bean definitions with.
     * <p>The factory is exposed through the BeanDefinitionRegistry interface,
     * encapsulating the methods that are relevant for bean definition handling.
     */
    BeanDefinitionRegistry getRegistry();

    /**
     * Return the resource loader to use for resource locations.
     * Can be checked for the <b>ResourcePatternResolver</b> interface and cast
     * accordingly, for loading multiple resources for a given resource pattern.
     * <p>Null suggests that absolute resource loading is not available
     * for this bean definition reader.
     * <p>This is mainly meant to be used for importing further resources
     * from within a bean definition resource, for example via the "import"
     * tag in XML bean definitions. It is recommended, however, to apply
     * such imports relative to the defining resource; only explicit full
     * resource locations will trigger absolute resource loading.
     * <p>There is also a {@code loadBeanDefinitions(String)} method available,
     * for loading bean definitions from a resource location (or location pattern).
     * This is a convenience to avoid explicit ResourceLoader handling.
     * @see #loadBeanDefinitions(String)
     */
    ResourceLoader getResourceLoader();

    /**
     * Return the class loader to use for bean classes.
     * <p>{@code null} suggests to not load bean classes eagerly
     * but rather to just register bean definitions with class names,
     * with the corresponding Classes to be resolved later (or never).
     */
    ClassLoader getBeanClassLoader();

    /**
     * Return the BeanNameGenerator to use for anonymous beans
     * (without explicit bean name specified).
     */
    BeanNameGenerator getBeanNameGenerator();


    /**
     * Load bean definitions from the specified resource.
     * @param resource the resource descriptor
     * @return the number of bean definitions found
     * @throws BeanDefinitionStoreException in case of loading or parsing errors
     */
    int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;

    /**
     * Load bean definitions from the specified resources.
     * @param resources the resource descriptors
     * @return the number of bean definitions found
     * @throws BeanDefinitionStoreException in case of loading or parsing errors
     */
    int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;

    /**
     * Load bean definitions from the specified resource location.
     * <p>The location can also be a location pattern, provided that the
     * ResourceLoader of this bean definition reader is a ResourcePatternResolver.
     * @param location the resource location, to be loaded with the ResourceLoader
     * (or ResourcePatternResolver) of this bean definition reader
     * @return the number of bean definitions found
     * @throws BeanDefinitionStoreException in case of loading or parsing errors
     * @see #getResourceLoader()
     * @see #loadBeanDefinitions(org.springlite.core.io.Resource)
     * @see #loadBeanDefinitions(org.springlite.core.io.Resource[])
     */
    int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;

    /**
     * Load bean definitions from the specified resource locations.
     * @param locations the resource locations, to be loaded with the ResourceLoader
     * (or ResourcePatternResolver) of this bean definition reader
     * @return the number of bean definitions found
     * @throws BeanDefinitionStoreException in case of loading or parsing errors
     */
    int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;

}
