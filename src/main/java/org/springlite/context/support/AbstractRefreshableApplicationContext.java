package org.springlite.context.support;

import org.springlite.beans.InitializingBean;
import org.springlite.beans.exception.BeansException;
import org.springlite.beans.factory.ConfigurableListableBeanFactory;
import org.springlite.beans.factory.support.DefaultListableBeanFactory;
import org.springlite.context.ApplicationContext;
import org.springlite.context.ApplicationContextException;

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
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext implements InitializingBean{

    private Boolean allowBeanDefinitionOverriding;

    private Boolean allowCircularReferences;

    /** Bean factory for this context */
    private DefaultListableBeanFactory beanFactory;

    /** Synchronization monitor for the internal BeanFactory */
    private final Object beanFactoryMonitor = new Object();


    /**
     * Create a new AbstractRefreshableApplicationContext with no parent.
     */
    public AbstractRefreshableApplicationContext() {
    }

    /**
     * Create a new AbstractRefreshableApplicationContext with the given parent context.
     * @param parent the parent context
     */
    public AbstractRefreshableApplicationContext(ApplicationContext parent) {
        super(parent);
    }


    /**
     * Set whether it should be allowed to override bean definitions by registering
     * a different definition with the same name, automatically replacing the former.
     * If not, an exception will be thrown. Default is "true".
     */
    public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
        this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
    }

    /**
     * Set whether to allow circular references between beans - and automatically
     * try to resolve them.
     * <p>Default is "true". Turn this off to throw an exception when encountering
     * a circular reference, disallowing them completely.
     */
    public void setAllowCircularReferences(boolean allowCircularReferences) {
        this.allowCircularReferences = allowCircularReferences;
    }


    /**
     * This implementation performs an actual refresh of this context's underlying
     * bean factory, shutting down the previous bean factory (if any) and
     * initializing a fresh bean factory for the next phase of the context's lifecycle.
     */
    @Override
    protected final void refreshBeanFactory() throws BeansException {
        if (hasBeanFactory()) {
            destroyBeans();
            closeBeanFactory();
        }
        try {
            DefaultListableBeanFactory beanFactory = createBeanFactory();
            //beanFactory.setSerializationId(getId());
            customizeBeanFactory(beanFactory);
            loadBeanDefinitions(beanFactory);
            synchronized (this.beanFactoryMonitor) {
                this.beanFactory = beanFactory;
            }
        }
        catch (IOException ex) {
            throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
        }
    }

    @Override
    protected void cancelRefresh(BeansException ex) {
        synchronized (this.beanFactoryMonitor) {
            if (this.beanFactory != null){
                //this.beanFactory.setSerializationId(null);
            }
        }
        super.cancelRefresh(ex);
    }

    @Override
    protected final void closeBeanFactory() {
        synchronized (this.beanFactoryMonitor) {
            //this.beanFactory.setSerializationId(null);
            this.beanFactory = null;
        }
    }

    /**
     * Determine whether this context currently holds a bean factory,
     * i.e. has been refreshed at least once and not been closed yet.
     */
    protected final boolean hasBeanFactory() {
        synchronized (this.beanFactoryMonitor) {
            return (this.beanFactory != null);
        }
    }

    @Override
    public final ConfigurableListableBeanFactory getBeanFactory() {
        synchronized (this.beanFactoryMonitor) {
            if (this.beanFactory == null) {
                throw new IllegalStateException("BeanFactory not initialized or already closed - " +
                        "call 'refresh' before accessing beans via the ApplicationContext");
            }
            return this.beanFactory;
        }
    }


    /**
     * Create an internal bean factory for this context.
     * Called for each {@link #refresh()} attempt.
     * <p>The default implementation creates a
     * {@link DefaultListableBeanFactory}
     * with the {@linkplain #() internal bean factory} of this
     * context's parent as parent bean factory. Can be overridden in subclasses,
     * for example to customize DefaultListableBeanFactory's settings.
     * @return the bean factory for this context
     */
    protected DefaultListableBeanFactory createBeanFactory() {
        //return new DefaultListableBeanFactory(getInternalParentBeanFactory());
        return new DefaultListableBeanFactory();
    }

    /**
     * Customize the internal bean factory used by this context.
     * Called for each {@link #refresh()} attempt.
     * <p>The default implementation applies this context's
     * {@linkplain #setAllowBeanDefinitionOverriding "allowBeanDefinitionOverriding"}
     * and {@linkplain #setAllowCircularReferences "allowCircularReferences"} settings,
     * if specified. Can be overridden in subclasses to customize any of
     * {@link DefaultListableBeanFactory}'s settings.
     * @param beanFactory the newly created bean factory for this context
     */
    protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
//        if (this.allowBeanDefinitionOverriding != null) {
//            beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
//        }
        if (this.allowCircularReferences != null) {
            beanFactory.setAllowCircularReferences(this.allowCircularReferences);
        }
//        beanFactory.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
    }

    /**
     * Load bean definitions into the given bean factory, typically through
     * delegating to one or more bean definition readers.
     * @param beanFactory the bean factory to load bean definitions into
     * @throws BeansException if parsing of the bean definitions failed
     * @throws IOException if loading of bean definition files failed

     */
    protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory)
            throws BeansException, IOException;

    @Override
    public void afterPropertiesSet() {
        if(!isActive()){
            refresh();
        }
    }
}
