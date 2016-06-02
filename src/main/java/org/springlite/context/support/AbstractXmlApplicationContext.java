package org.springlite.context.support;

import org.springlite.beans.exception.BeansException;
import org.springlite.beans.factory.support.DefaultListableBeanFactory;
import org.springlite.core.io.DefaultResourceLoader;
import org.springlite.core.io.ResourceLoader;
import org.springlite.beans.factory.xml.XmlBeanDefinitionReader;
import org.springlite.context.ApplicationContext;
import org.springlite.core.io.Resource;
import org.springlite.util.Assert;
import org.springlite.util.StringUtils;

import java.io.IOException;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableApplicationContext {

    private boolean validating = true;

    private ResourceLoader  defaultResourceLoader = new DefaultResourceLoader();

    private String[] configLocations;

    String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

    /**
     * Create a new AbstractXmlApplicationContext with no parent.
     */
    public AbstractXmlApplicationContext() {
        super();
    }

    /**
     * Create a new AbstractXmlApplicationContext with the given parent context.
     */
    public AbstractXmlApplicationContext(ApplicationContext parent) {
        super(parent);
    }

    /**
     * Set whether to use XML validation. Default is {@code true}.
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }


    /**
     * Loads the bean definitions via an XmlBeanDefinitionReader.
     * @see org.springlite.beans.factory.xml.XmlBeanDefinitionReader
     * @see #initBeanDefinitionReader
     * @see #loadBeanDefinitions
     */
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        // Create a new XmlBeanDefinitionReader for the given BeanFactory.
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory, defaultResourceLoader);

        // Configure the bean definition reader with this context's
        // resource loading environment.
        //beanDefinitionReader.setEnvironment(this.getEnvironment());
        //beanDefinitionReader.setResourceLoader(this);
        //beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

        // Allow a subclass to provide custom initialization of the reader,
        // then proceed with actually loading the bean definitions.
        initBeanDefinitionReader(beanDefinitionReader);
        loadBeanDefinitions(beanDefinitionReader);
    }

    /**
     * Initialize the bean definition reader used for loading the bean
     * definitions of this context. Default implementation is empty.
     * <p>Can be overridden in subclasses, e.g. for turning off XML validation
     * or using a different XmlBeanDefinitionParser implementation.
     * @param reader the bean definition reader used by this context
     */
    protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
        reader.setValidating(this.validating);
    }

    /**
     * Load the bean definitions with the given XmlBeanDefinitionReader.
     * <p>The lifecycle of the bean factory is handled by the {@link #refreshBeanFactory}
     * method; hence this method is just supposed to load and/or register bean definitions.
     * @param reader the XmlBeanDefinitionReader to use
     * @throws BeansException in case of bean registration errors
     * @throws IOException if the required XML document isn't found
     * @see #refreshBeanFactory
     * @see #getConfigLocations
     */
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
        Resource[] configResources = getConfigResources();
        if (configResources != null) {
            reader.loadBeanDefinitions(configResources);
        }
        String[] configLocations = getConfigLocations();
        if (configLocations != null) {
            reader.loadBeanDefinitions(configLocations);
        }
    }

    /**
     * Return an array of Resource objects, referring to the XML bean definition
     * files that this context should be built with.
     * <p>The default implementation returns {@code null}. Subclasses can override
     * this to provide pre-built Resource objects rather than location Strings.
     * @return an array of Resource objects, or {@code null} if none
     * @see #getConfigLocations()
     */
    protected Resource[] getConfigResources() {
        return null;
    }

    /**
     * Set the config locations for this application context in init-param style,
     * i.e. with distinct locations separated by commas, semicolons or whitespace.
     * <p>If not set, the implementation may use a default as appropriate.
     */
    public void setConfigLocation(String location) {
        setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS));
    }

    /**
     * Set the config locations for this application context.
     * <p>If not set, the implementation may use a default as appropriate.
     */
    public void setConfigLocations(String[] locations) {
        if (locations != null) {
            Assert.noNullElements(locations, "Config locations must not be null");
            this.configLocations = new String[locations.length];
            for (int i = 0; i < locations.length; i++) {
                this.configLocations[i] = resolvePath(locations[i]).trim();
            }
        }
        else {
            this.configLocations = null;
        }
    }

    /**
     * Resolve the given path, replacing placeholders with corresponding
     * environment property values if necessary. Applied to config locations.
     * @param path the original file path
     * @return the resolved file path
     */
    protected String resolvePath(String path) {
        //return getEnvironment().resolveRequiredPlaceholders(path);
        return  path;
    }

    /**
     * Return an array of resource locations, referring to the XML bean definition
     * files that this context should be built with. Can also include location
     * patterns, which will get resolved via a ResourcePatternResolver.
     * <p>The default implementation returns {@code null}. Subclasses can override
     * this to provide a set of resource locations to load bean definitions from.
     * @return an array of resource locations, or {@code null} if none
     */
    protected String[] getConfigLocations() {
        return (this.configLocations != null ? this.configLocations : getDefaultConfigLocations());
    }

    /**
     * Return the default config locations to use, for the case where no
     * explicit config locations have been specified.
     * <p>The default implementation returns {@code null},
     * requiring explicit config locations.
     * @return an array of default config locations, if any
     * @see #setConfigLocations
     */
    protected String[] getDefaultConfigLocations() {
        return null;
    }

}