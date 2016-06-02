package org.springlite.beans.factory.support;

import org.springlite.beans.exception.BeansException;
import org.springlite.beans.factory.FactoryBean;

import java.security.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/26
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

    /** Cache of singleton objects created by FactoryBeans: FactoryBean name --> object */
    private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>(16);


    /**
     * Determine the type for the given FactoryBean.
     * @param factoryBean the FactoryBean instance to check
     * @return the FactoryBean's object type,
     * or {@code null} if the type cannot be determined yet
     */
    protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
        try {
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
                    public Class<?> run() {
                        return factoryBean.getObjectType();
                    }
                }, getAccessControlContext());
            }
            else {
                return factoryBean.getObjectType();
            }
        }
        catch (Throwable ex) {
            // Thrown from the FactoryBean's getObjectType implementation.
            logger.warn("FactoryBean threw exception from getObjectType, despite the contract saying " +
                    "that it should return null if the type of its object cannot be determined yet", ex);
            return null;
        }
    }

    /**
     * Obtain an object to expose from the given FactoryBean, if available
     * in cached form. Quick check for minimal synchronization.
     * @param beanName the name of the bean
     * @return the object obtained from the FactoryBean,
     * or {@code null} if not available
     */
    protected Object getCachedObjectForFactoryBean(String beanName) {
        Object object = this.factoryBeanObjectCache.get(beanName);
        return (object != NULL_OBJECT ? object : null);
    }

    /**
     * Obtain an object to expose from the given FactoryBean.
     * @param factory the FactoryBean instance
     * @param beanName the name of the bean
     * @param shouldPostProcess whether the bean is subject to post-processing
     * @return the object obtained from the FactoryBean
     * @throws BeansException if FactoryBean object creation failed
     * @see org.springlite.beans.factory.FactoryBean#getObject()
     */
    protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
        if (factory.isSingleton() && containsSingleton(beanName)) {
            synchronized (getSingletonMutex()) {
                Object object = this.factoryBeanObjectCache.get(beanName);
                if (object == null) {
                    object = doGetObjectFromFactoryBean(factory, beanName);
                    // Only post-process and store if not put there already during getObject() call above
                    // (e.g. because of circular reference processing triggered by custom getBean calls)
                    Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                    if (alreadyThere != null) {
                        object = alreadyThere;
                    }
                    else {
                        if (object != null && shouldPostProcess) {
                            try {
                                object = postProcessObjectFromFactoryBean(object, beanName);
                            }
                            catch (Throwable ex) {
                                throw new BeansException(beanName,
                                        "Post-processing of FactoryBean's singleton object failed", ex);
                            }
                        }
                        this.factoryBeanObjectCache.put(beanName, (object != null ? object : NULL_OBJECT));
                    }
                }
                return (object != NULL_OBJECT ? object : null);
            }
        }
        else {
            Object object = doGetObjectFromFactoryBean(factory, beanName);
            if (object != null && shouldPostProcess) {
                try {
                    object = postProcessObjectFromFactoryBean(object, beanName);
                }
                catch (Throwable ex) {
                    throw new BeansException(beanName, "Post-processing of FactoryBean's object failed", ex);
                }
            }
            return object;
        }
    }

    /**
     * Obtain an object to expose from the given FactoryBean.
     * @param factory the FactoryBean instance
     * @param beanName the name of the bean
     * @return the object obtained from the FactoryBean
     * @throws BeansException if FactoryBean object creation failed
     * @see org.springlite.beans.factory.FactoryBean#getObject()
     */
    private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName)
            throws BeansException {

        Object object;
        try {
            if (System.getSecurityManager() != null) {
                AccessControlContext acc = getAccessControlContext();
                try {
                    object = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                        public Object run() throws Exception {
                            return factory.getObject();
                        }
                    }, acc);
                }
                catch (PrivilegedActionException pae) {
                    throw pae.getException();
                }
            }
            else {
                object = factory.getObject();
            }
        }
        catch (BeansException ex) {
            throw new BeansException(beanName, ex.toString());
        }
        catch (Throwable ex) {
            throw new BeansException(beanName, "FactoryBean threw exception on object creation", ex);
        }

        // Do not accept a null value for a FactoryBean that's not fully
        // initialized yet: Many FactoryBeans just return null then.
        if (object == null && isSingletonCurrentlyInCreation(beanName)) {
            throw new BeansException(
                    beanName, "FactoryBean which is currently in creation returned null from getObject");
        }
        return object;
    }

    /**
     * Post-process the given object that has been obtained from the FactoryBean.
     * The resulting object will get exposed for bean references.
     * <p>The default implementation simply returns the given object as-is.
     * Subclasses may override this, for example, to apply post-processors.
     * @param object the object obtained from the FactoryBean.
     * @param beanName the name of the bean
     * @return the object to expose
     * @throws org.springlite.beans.exception.BeansException if any post-processing failed
     */
    protected Object postProcessObjectFromFactoryBean(Object object, String beanName) throws BeansException {
        return object;
    }

    /**
     * Get a FactoryBean for the given bean if possible.
     * @param beanName the name of the bean
     * @param beanInstance the corresponding bean instance
     * @return the bean instance as FactoryBean
     * @throws BeansException if the given bean cannot be exposed as a FactoryBean
     */
    protected FactoryBean<?> getFactoryBean(String beanName, Object beanInstance) throws BeansException {
        if (!(beanInstance instanceof FactoryBean)) {
            throw new BeansException(beanName,
                    "Bean instance of type [" + beanInstance.getClass() + "] is not a FactoryBean");
        }
        return (FactoryBean<?>) beanInstance;
    }

    /**
     * Overridden to clear the FactoryBean object cache as well.
     */
    @Override
    protected void removeSingleton(String beanName) {
        super.removeSingleton(beanName);
        this.factoryBeanObjectCache.remove(beanName);
    }

    /**
     * Returns the security context for this bean factory. If a security manager
     * is set, interaction with the user code will be executed using the privileged
     * of the security context returned by this method.
     * @see AccessController#getContext()
     */
    protected AccessControlContext getAccessControlContext() {
        return AccessController.getContext();
    }

}
