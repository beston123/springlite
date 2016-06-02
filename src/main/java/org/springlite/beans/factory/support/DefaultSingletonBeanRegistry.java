package org.springlite.beans.factory.support;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springlite.beans.exception.BeanCreationException;
import org.springlite.beans.exception.BeansException;
import org.springlite.beans.factory.DisposableBean;
import org.springlite.beans.factory.ObjectFactory;
import org.springlite.beans.factory.config.SingletonBeanRegistry;
import org.springlite.util.Assert;
import org.springlite.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by swy on 2016/5/26.
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    /**
     * Internal marker for a null singleton object:
     * used as marker value for concurrent Maps (which don't support null values).
     */
    protected static final Object NULL_OBJECT = new Object();


    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    /** Cache of singleton objects: bean name --> bean instance */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>(64);

    /** Cache of singleton factories: bean name --> ObjectFactory */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<String, ObjectFactory<?>>(16);

    /** Cache of early singleton objects: bean name --> bean instance */
    private final Map<String, Object> earlySingletonObjects = new HashMap<String, Object>(16);

    /** Set of registered singletons, containing the bean names in registration order */
    private final Set<String> registeredSingletons = new LinkedHashSet<String>(64);

    /** Names of beans that are currently in creation (using a ConcurrentHashMap as a Set) */
    private final Map<String, Boolean> singletonsCurrentlyInCreation = new ConcurrentHashMap<String, Boolean>(16);

    /** Names of beans currently excluded from in creation checks (using a ConcurrentHashMap as a Set) */
    private final Map<String, Boolean> inCreationCheckExclusions = new ConcurrentHashMap<String, Boolean>(16);

    /** List of suppressed Exceptions, available for associating related causes */
    private Set<Exception> suppressedExceptions;

    /** Flag that indicates whether we're currently within destroySingletons */
    private boolean singletonsCurrentlyInDestruction = false;

    /** Disposable bean instances: bean name --> disposable instance */
    protected final Map<String, Object> disposableBeans = new LinkedHashMap<String, Object>();

    /** Map between containing bean names: bean name --> Set of bean names that the bean contains */
    private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<String, Set<String>>(16);

    /** Map between dependent bean names: bean name --> Set of dependent bean names */
    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<String, Set<String>>(64);

    /** Map between depending bean names: bean name --> Set of bean names for the bean's dependencies */
    private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<String, Set<String>>(64);


    @Override
    public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
        Validate.notNull(beanName, "'beanName' must not be null");
        synchronized (this.singletonObjects) {
            Object oldObject = this.singletonObjects.get(beanName);
            if (oldObject != null) {
                throw new IllegalStateException("Could not register object [" + singletonObject +
                        "] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
            }
            addSingleton(beanName, singletonObject);
        }
    }

    /**
     * Add the given singleton object to the singleton cache of this factory.
     * <p>To be called for eager registration of singletons.
     * @param beanName the name of the bean
     * @param singletonObject the singleton object
     */
    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, (singletonObject != null ? singletonObject : NULL_OBJECT));
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }

    @Override
    public Object getSingleton(String beanName) {
        return getSingleton(beanName, true);
    }

    /**
     * Return the (raw) singleton object registered under the given name.
     * <p>Checks already instantiated singletons and also allows for an early
     * reference to a currently created singleton (resolving a circular reference).
     * @param beanName the name of the bean to look for
     * @param allowEarlyReference whether early references should be created or not
     * @return the registered singleton object, or {@code null} if none found
     */
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (this.singletonObjects) {
                singletonObject = this.earlySingletonObjects.get(beanName);
                if (singletonObject == null && allowEarlyReference) {
                    ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        singletonObject = singletonFactory.getObject();
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return (singletonObject != NULL_OBJECT ? singletonObject : null);
    }

    /**
     * Return whether the specified singleton bean is currently in creation
     * (within the entire factory).
     * @param beanName the name of the bean
     */
    public boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.containsKey(beanName);
    }

    /**
     * Return the (raw) singleton object registered under the given name,
     * creating and registering a new one if none registered yet.
     * @param beanName the name of the bean
     * with, if necessary
     * @return the registered singleton object
     */
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Validate.notNull(beanName, "'beanName' must not be null");
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                if (this.singletonsCurrentlyInDestruction) {
                    throw new BeansException(beanName,
                            "Singleton bean creation not allowed while the singletons of this factory are in destruction " +
                                    "(Do not request a bean from a BeanFactory in a destroy method implementation!)");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
                }
                beforeSingletonCreation(beanName);
                boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = new LinkedHashSet<Exception>();
                }
                try {
                    singletonObject = singletonFactory.getObject();
                }
                catch (BeanCreationException ex) {
                    if (recordSuppressedExceptions) {
                        for (Exception suppressedException : this.suppressedExceptions) {
                            ex.addRelatedCause(suppressedException);
                        }
                    }
                    throw ex;
                }
                finally {
                    if (recordSuppressedExceptions) {
                        this.suppressedExceptions = null;
                    }
                    afterSingletonCreation(beanName);
                }
                addSingleton(beanName, singletonObject);
            }
            return (singletonObject != NULL_OBJECT ? singletonObject : null);
        }
    }

    /**
     * Callback before singleton creation.
     * <p>The default implementation register the singleton as currently in creation.
     * @param beanName the name of the singleton about to be created
     * @see #isSingletonCurrentlyInCreation
     */
    protected void beforeSingletonCreation(String beanName) {
        if (!this.inCreationCheckExclusions.containsKey(beanName) &&
                this.singletonsCurrentlyInCreation.put(beanName, Boolean.TRUE) != null) {
            throw new BeanCreationException(beanName, "Requested bean is currently in creation: Is there an unresolvable circular reference?");
        }
    }

    /**
     * Callback after singleton creation.
     * <p>The default implementation marks the singleton as not in creation anymore.
     * @param beanName the name of the singleton that has been created
     * @see #isSingletonCurrentlyInCreation
     */
    protected void afterSingletonCreation(String beanName) {
        if (!this.inCreationCheckExclusions.containsKey(beanName) &&
                !this.singletonsCurrentlyInCreation.remove(beanName)) {
            throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
        }
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return (this.singletonObjects.containsKey(beanName));
    }

    @Override
    public String[] getSingletonNames() {
        synchronized (this.singletonObjects) {
            return StringUtils.toStringArray(this.registeredSingletons);
        }
    }

    @Override
    public int getSingletonCount() {
        synchronized (this.singletonObjects) {
            return this.registeredSingletons.size();
        }
    }


    /**
     * Register an Exception that happened to get suppressed during the creation of a
     * singleton bean instance, e.g. a temporary circular reference resolution problem.
     * @param ex the Exception to register
     */
    protected void onSuppressedException(Exception ex) {
        synchronized (this.singletonObjects) {
            if (this.suppressedExceptions != null) {
                this.suppressedExceptions.add(ex);
            }
        }
    }

    protected void addSingletonFactory(String beanName, ObjectFactory singletonFactory) {
        Assert.notNull(singletonFactory, "Singleton factory must not be null");
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
                this.registeredSingletons.add(beanName);
            }
        }
    }

    /**
     * Remove the bean with the given name from the singleton cache of this factory,
     * to be able to clean up eager registration of a singleton if creation failed.
     * @param beanName the name of the bean
     */
    protected void removeSingleton(String beanName) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.remove(beanName);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.remove(beanName);
        }
    }

    public void destroySingletons() {
        if (logger.isInfoEnabled()) {
            logger.info("Destroying singletons in " + this);
        }
        synchronized (this.singletonObjects) {
            this.singletonsCurrentlyInDestruction = true;
        }

        String[] disposableBeanNames;
        synchronized (this.disposableBeans) {
            disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
        }
        for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
            destroySingleton(disposableBeanNames[i]);
        }

        this.containedBeanMap.clear();
        this.dependentBeanMap.clear();
        this.dependenciesForBeanMap.clear();

        synchronized (this.singletonObjects) {
            this.singletonObjects.clear();
            this.singletonFactories.clear();
            this.earlySingletonObjects.clear();
            this.registeredSingletons.clear();
            this.singletonsCurrentlyInDestruction = false;
        }
    }


    /**
     * Destroy the given bean. Delegates to {@code destroyBean}
     * if a corresponding disposable bean instance is found.
     * @param beanName the name of the bean
     * @see #destroyBean
     */
    public void destroySingleton(String beanName) {
        // Remove a registered singleton of the given name, if any.
        removeSingleton(beanName);

        // Destroy the corresponding DisposableBean instance.
        DisposableBean disposableBean;
        synchronized (this.disposableBeans) {
            disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
        }
        destroyBean(beanName, disposableBean);
    }


    /**
     * Destroy the given bean. Must destroy beans that depend on the given
     * bean before the bean itself. Should not throw any exceptions.
     * @param beanName the name of the bean
     * @param bean the bean instance to destroy
     */
    protected void destroyBean(String beanName, DisposableBean bean) {
        // Trigger destruction of dependent beans first...
        Set<String> dependencies = this.dependentBeanMap.remove(beanName);
        if (dependencies != null) {
            for (String dependentBeanName : dependencies) {
                destroySingleton(dependentBeanName);
            }
        }

        //Actually destroy the bean now...
        if (bean != null) {
            try {
                bean.destroy();
            }
            catch (Throwable ex) {
                logger.error("Destroy method on bean with name '" + beanName + "' threw an exception", ex);
            }
        }

        // Trigger destruction of contained beans...
        Set<String> containedBeans = this.containedBeanMap.remove(beanName);
        if (containedBeans != null) {
            for (String containedBeanName : containedBeans) {
                destroySingleton(containedBeanName);
            }
        }

        // Remove destroyed bean from other beans' dependencies.
        synchronized (this.dependentBeanMap) {
            for (Iterator<Map.Entry<String, Set<String>>> it = this.dependentBeanMap.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Set<String>> entry = it.next();
                Set<String> dependenciesToClean = entry.getValue();
                dependenciesToClean.remove(beanName);
                if (dependenciesToClean.isEmpty()) {
                    it.remove();
                }
            }
        }

        // Remove destroyed bean's prepared dependency information.
        this.dependenciesForBeanMap.remove(beanName);
    }


    public void registerDependentBean(String beanName, String dependentBeanName) {
        String canonicalName = beanName;
        synchronized (this.dependentBeanMap) {
            Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
            if (dependentBeans == null) {
                dependentBeans = new LinkedHashSet<String>(8);
                this.dependentBeanMap.put(canonicalName, dependentBeans);
            }
            dependentBeans.add(dependentBeanName);
        }
        synchronized (this.dependenciesForBeanMap) {
            Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(dependentBeanName);
            if (dependenciesForBean == null) {
                dependenciesForBean = new LinkedHashSet<String>(8);
                this.dependenciesForBeanMap.put(dependentBeanName, dependenciesForBean);
            }
            dependenciesForBean.add(canonicalName);
        }
    }

    public final Object getSingletonMutex() {
        return this.singletonObjects;
    }
}
