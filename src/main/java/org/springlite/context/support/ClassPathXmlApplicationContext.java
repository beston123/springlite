package org.springlite.context.support;

import org.springlite.beans.exception.BeansException;
import org.springlite.context.ApplicationContext;
import org.springlite.context.support.AbstractXmlApplicationContext;
import org.springlite.core.io.ClassPathResource;
import org.springlite.core.io.Resource;
import org.springlite.util.Assert;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {


    private Resource[] configResources;


    /**
     * Create a new ClassPathXmlApplicationContext for bean-style configuration.
     * @see #setConfigLocation
     * @see #setConfigLocations
     * @see #afterPropertiesSet()
     */
    public ClassPathXmlApplicationContext() {
    }

    /**
     * Create a new ClassPathXmlApplicationContext for bean-style configuration.
     * @param parent the parent context
     * @see #setConfigLocation
     * @see #setConfigLocations
     * @see #afterPropertiesSet()
     */
    public ClassPathXmlApplicationContext(ApplicationContext parent) {
        super(parent);
    }

    /**
     * Create a new ClassPathXmlApplicationContext, loading the definitions
     * from the given XML file and automatically refreshing the context.
     * @param configLocation resource location
     * @throws BeansException if context creation failed
     */
    public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
        this(new String[] {configLocation}, true, null);
    }

    /**
     * Create a new ClassPathXmlApplicationContext, loading the definitions
     * from the given XML files and automatically refreshing the context.
     * @param configLocations array of resource locations
     * @throws BeansException if context creation failed
     */
    public ClassPathXmlApplicationContext(String... configLocations) throws BeansException {
        this(configLocations, true, null);
    }

    /**
     * Create a new ClassPathXmlApplicationContext with the given parent,
     * loading the definitions from the given XML files and automatically
     * refreshing the context.
     * @param configLocations array of resource locations
     * @param parent the parent context
     * @throws BeansException if context creation failed
     */
    public ClassPathXmlApplicationContext(String[] configLocations, ApplicationContext parent) throws BeansException {
        this(configLocations, true, parent);
    }

    /**
     * Create a new ClassPathXmlApplicationContext, loading the definitions
     * from the given XML files.
     * @param configLocations array of resource locations
     * @param refresh whether to automatically refresh the context,
     * loading all bean definitions and creating all singletons.
     * Alternatively, call refresh manually after further configuring the context.
     * @throws BeansException if context creation failed
     * @see #refresh()
     */
    public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh) throws BeansException {
        this(configLocations, refresh, null);
    }

    /**
     * Create a new ClassPathXmlApplicationContext with the given parent,
     * loading the definitions from the given XML files.
     * @param configLocations array of resource locations
     * @param refresh whether to automatically refresh the context,
     * loading all bean definitions and creating all singletons.
     * Alternatively, call refresh manually after further configuring the context.
     * @param parent the parent context
     * @throws BeansException if context creation failed
     * @see #refresh()
     */
    public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
            throws BeansException {

        super(parent);
        setConfigLocations(configLocations);
        if (refresh) {
            refresh();
        }
    }


    /**
     * Create a new ClassPathXmlApplicationContext, loading the definitions
     * from the given XML file and automatically refreshing the context.
     * <p>This is a convenience method to load class path resources relative to a
     * given Class. For full flexibility, consider using a GenericApplicationContext
     * with an XmlBeanDefinitionReader and a ClassPathResource argument.
     * @param path relative (or absolute) path within the class path
     * @param clazz the class to load resources with (basis for the given paths)
     * @throws BeansException if context creation failed
     * @see org.springlite.core.io.ClassPathResource#ClassPathResource(String, Class)
     * @see org.springlite.beans.factory.xml.XmlBeanDefinitionReader
     */
    public ClassPathXmlApplicationContext(String path, Class clazz) throws BeansException {
        this(new String[] {path}, clazz);
    }

    /**
     * Create a new ClassPathXmlApplicationContext, loading the definitions
     * from the given XML files and automatically refreshing the context.
     * @param paths array of relative (or absolute) paths within the class path
     * @param clazz the class to load resources with (basis for the given paths)
     * @throws BeansException if context creation failed
     * @see org.springlite.core.io.ClassPathResource#ClassPathResource(String, Class)
     * @see org.springlite.beans.factory.xml.XmlBeanDefinitionReader
     */
    public ClassPathXmlApplicationContext(String[] paths, Class clazz) throws BeansException {
        this(paths, clazz, null);
    }

    /**
     * Create a new ClassPathXmlApplicationContext with the given parent,
     * loading the definitions from the given XML files and automatically
     * refreshing the context.
     * @param paths array of relative (or absolute) paths within the class path
     * @param clazz the class to load resources with (basis for the given paths)
     * @param parent the parent context
     * @throws BeansException if context creation failed
     * @see org.springlite.core.io.ClassPathResource#ClassPathResource(String, Class)
     * @see org.springlite.beans.factory.xml.XmlBeanDefinitionReader
     */
    public ClassPathXmlApplicationContext(String[] paths, Class clazz, ApplicationContext parent)
            throws BeansException {

        super(parent);
        Assert.notNull(paths, "Path array must not be null");
        Assert.notNull(clazz, "Class argument must not be null");
        this.configResources = new Resource[paths.length];
        for (int i = 0; i < paths.length; i++) {
            this.configResources[i] = new ClassPathResource(paths[i], clazz);
        }
        refresh();
    }


    @Override
    protected Resource[] getConfigResources() {
        return this.configResources;
    }

}
