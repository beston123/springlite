package org.springlite.beans.factory.support;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springlite.beans.factory.config.BeanDefinition;
import org.springlite.beans.exception.BeanDefinitionStoreException;
import org.springlite.beans.factory.BeanNameGenerator;
import org.springlite.core.io.ResourceLoader;
import org.springlite.core.io.Resource;
import org.springlite.util.Assert;

import java.util.Set;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {

    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    private final BeanDefinitionRegistry registry;

    private ResourceLoader resourceLoader;

    private ClassLoader beanClassLoader;

    //private Environment environment;

    private BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();


    protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
        this.registry = registry;
        this.resourceLoader = resourceLoader;
    }


    public final BeanDefinitionRegistry getBeanFactory() {
        return this.registry;
    }

    public final BeanDefinitionRegistry getRegistry() {
        return this.registry;
    }

    /**
     * Set the ResourceLoader to use for resource locations.
     * If specifying a ResourcePatternResolver, the bean definition reader
     * will be capable of resolving resource patterns to Resource arrays.
     * <p>Default is PathMatchingResourcePatternResolver, also capable of
     * resource pattern resolving through the ResourcePatternResolver interface.
     * <p>Setting this to {@code null} suggests that absolute resource loading
     * is not available for this bean definition reader.
     */
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ResourceLoader getResourceLoader() {
        return this.resourceLoader;
    }

    /**
     * Set the ClassLoader to use for bean classes.
     * <p>Default is {@code null}, which suggests to not load bean classes
     * eagerly but rather to just register bean definitions with class names,
     * with the corresponding Classes to be resolved later (or never).
     * @see Thread#getContextClassLoader()
     */
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }

    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

//    /**
//     * Set the Environment to use when reading bean definitions. Most often used
//     * for evaluating profile information to determine which bean definitions
//     * should be read and which should be omitted.
//     */
//    public void setEnvironment(Environment environment) {
//        this.environment = environment;
//    }
//
//    public Environment getEnvironment() {
//        return this.environment;
//    }

    /**
     * Set the BeanNameGenerator to use for anonymous beans
     * (without explicit bean name specified).
     */
    public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
        this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new DefaultBeanNameGenerator());
    }

    public BeanNameGenerator getBeanNameGenerator() {
        return this.beanNameGenerator;
    }


    public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
        Assert.notNull(resources, "Resource array must not be null");
        int counter = 0;
        for (Resource resource : resources) {
            counter += loadBeanDefinitions(resource);
        }
        return counter;
    }

    public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
        return loadBeanDefinitions(location, null);
    }

    /**
     * Load bean definitions from the specified resource location.
     * <p>The location can also be a location pattern, provided that the
     * ResourceLoader of this bean definition reader is a ResourcePatternResolver.
     * @param location the resource location, to be loaded with the ResourceLoader
     * (or ResourcePatternResolver) of this bean definition reader
     * @param actualResources a Set to be filled with the actual Resource objects
     * that have been resolved during the loading process. May be {@code null}
     * to indicate that the caller is not interested in those Resource objects.
     * @return the number of bean definitions found
     * @throws BeanDefinitionStoreException in case of loading or parsing errors
     * @see #getResourceLoader()
     * @see #loadBeanDefinitions(org.springlite.core.io.Resource)
     * @see #loadBeanDefinitions(org.springlite.core.io.Resource[])
     */
    public int loadBeanDefinitions(String location, Set<Resource> actualResources) throws BeanDefinitionStoreException {
        ResourceLoader resourceLoader = getResourceLoader();
        if (resourceLoader == null) {
            throw new BeanDefinitionStoreException(
                    "Cannot import bean definitions from location [" + location + "]: no ResourceLoader available");
        }

        /*if (resourceLoader instanceof ResourcePatternResolver) {
            // Resource pattern matching available.
            try {
                Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
                int loadCount = loadBeanDefinitions(resources);
                if (actualResources != null) {
                    for (Resource resource : resources) {
                        actualResources.add(resource);
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Loaded " + loadCount + " bean definitions from location pattern [" + location + "]");
                }
                return loadCount;
            }
            catch (IOException ex) {
                throw new BeanDefinitionStoreException(
                        "Could not resolve bean definition resource pattern [" + location + "]", ex);
            }
        }
        else */
        {
            // Can only load single resources by absolute URL.
            Resource resource = resourceLoader.getResource(location);
            int loadCount = loadBeanDefinitions(resource);
            if (actualResources != null) {
                actualResources.add(resource);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded " + loadCount + " bean definitions from location [" + location + "]");
            }
            return loadCount;
        }
    }

    public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
        Assert.notNull(locations, "Location array must not be null");
        int counter = 0;
        for (String location : locations) {
            counter += loadBeanDefinitions(location);
        }
        return counter;
    }


    static class DefaultBeanNameGenerator implements BeanNameGenerator{

        @Override
        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            if(StringUtils.isNotBlank(definition.getBeanName())){
                return definition.getBeanName();
            }else{
                String className = definition.getBeanClassName();
                String beanName = StringUtils.substringAfterLast(className, ".");
                if(StringUtils.isNotBlank(beanName)){
                    return org.springlite.util.StringUtils.toLowerCaseFirstOne(beanName);
                }
            }
            throw new BeanDefinitionStoreException("Can not generate beanName, define in "+definition.getDescription()
                    +", beanClass "+definition.getBeanClassName());
        }
    }
}
