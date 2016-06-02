package org.springlite.beans.factory.support;

import org.apache.commons.lang.Validate;
import org.springlite.beans.exception.*;
import org.springlite.beans.factory.BeanFactory;
import org.springlite.beans.factory.ConfigurableBeanFactory;
import org.springlite.beans.factory.ConfigurableListableBeanFactory;
import org.springlite.beans.factory.ListableBeanFactory;
import org.springlite.beans.factory.config.BeanDefinition;
import org.springlite.util.Assert;
import org.springlite.util.ClassUtils;
import org.springlite.util.StringUtils;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
        implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

    /** Map of bean definition objects, keyed by bean name */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(64);

    /** Map of singleton and non-singleton bean names keyed by dependency type */
    private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<Class<?>, String[]>(64);

    /** Map of singleton-only bean names keyed by dependency type */
    private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<Class<?>, String[]>(64);

    /** List of bean definition names, in registration order */
    private final List<String> beanDefinitionNames = new ArrayList<String>();

    /**********************************************************************
     * Implementation of ConfigurableBeanFactory interface start
     * @see ConfigurableBeanFactory
     * @see AbstractBeanFactory#getBeanDefinition(String)
     *********************************************************************/

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        BeanDefinition bd = this.beanDefinitionMap.get(beanName);
        if (bd == null) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("No bean named '" + beanName + "' found in " + this);
            }
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return bd;
    }

    @Override
    public void preInstantiateSingletons() throws BeansException {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Pre-instantiating singletons in " + this);
        }

        List<String> beanNames;
        synchronized (this.beanDefinitionMap) {
            // Iterate over a copy to allow for init methods which in turn register new bean definitions.
            // While this may not be part of the regular factory bootstrap, it does otherwise work fine.
            beanNames = new ArrayList<String>(this.beanDefinitionNames);
        }

        // Trigger initialization of all non-lazy singleton beans...
        for (String beanName : beanNames) {
            RootBeanDefinition bd = (RootBeanDefinition)getBeanDefinition(beanName);
            if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                getBean(beanName);
            }
        }
    }
    /**********************************************************************
     * Implementation of ConfigurableBeanFactory interface end
     *********************************************************************/


    /**********************************************************************
     * Implementation of BeanDefinitionRegistry interface start
     * @see org.springlite.beans.factory.support.BeanDefinitionRegistry
     *********************************************************************/

    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        Validate.notEmpty(beanName, "Bean name must not be empty");
        Validate.notNull(beanDefinition, "BeanDefinition must not be null");

        synchronized (this.beanDefinitionMap) {
            Object oldBeanDefinition = this.beanDefinitionMap.get(beanName);
            if (oldBeanDefinition != null) {
                throw new BeanDefinitionStoreException("Cannot register bean definition [" + beanDefinition + "] for bean '" + beanName +
                        "': There is already [" + oldBeanDefinition + "] bound.");
            }
            else {
                this.beanDefinitionNames.add(beanName);
            }
            this.beanDefinitionMap.put(beanName, beanDefinition);
        }
        resetBeanDefinition(beanName);
    }

    public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        Validate.notEmpty(beanName, "'beanName' must not be empty");

        synchronized (this.beanDefinitionMap) {
            BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
            if (bd == null) {
                System.out.println("No bean named '" + beanName + "' found in " + this);
                throw new NoSuchBeanDefinitionException(beanName);
            }
            this.beanDefinitionNames.remove(beanName);
        }
        resetBeanDefinition(beanName);
    }

    /**
     * Reset all bean definition caches for the given bean,
     * including the caches of beans that are derived from it.
     * @param beanName the name of the bean to reset
     */
    protected void resetBeanDefinition(String beanName) {
        // Remove the merged bean definition for the given bean, if already created.
        clearMergedBeanDefinition(beanName);

        // Remove corresponding bean from singleton cache, if any. Shouldn't usually
        // be necessary, rather just meant for overriding a context's default beans
        // (e.g. the default StaticMessageSource in a StaticApplicationContext).
        destroySingleton(beanName);

        // Remove any assumptions about by-type mappings.
        clearByTypeCache();

//        //Reset all bean definitions that have the given bean as parent (recursively).
//        for (String bdName : this.beanDefinitionNames) {
//            if (!beanName.equals(bdName)) {
//                BeanDefinition bd = this.beanDefinitionMap.get(bdName);
//                if (beanName.equals(bd.getParentName())) {
//                    resetBeanDefinition(bdName);
//                }
//            }
//        }
    }

    @Override
    public boolean isBeanNameInUse(String beanName) {
        return containsBeanDefinition(beanName);
    }

    /**********************************************************************
     * Implementation of BeanDefinitionRegistry interface end
     * @see org.springlite.beans.factory.support.BeanDefinitionRegistry
     *********************************************************************/



    /**********************************************************************
     * SingletonBeanRegistry && DefaultSingletonBeanRegistry
     * @see org.springlite.beans.factory.config.SingletonBeanRegistry
     * @see org.springlite.beans.factory.support.DefaultSingletonBeanRegistry
     *********************************************************************/

    @Override
    public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
        super.registerSingleton(beanName, singletonObject);
        clearByTypeCache();
    }

    @Override
    public void destroySingleton(String beanName) {
        super.destroySingleton(beanName);
        clearByTypeCache();
    }

    /**********************************************************************
     * SingletonBeanRegistry DefaultSingletonBeanRegistry end
     *********************************************************************/

    private void clearByTypeCache() {
        this.allBeanNamesByType.clear();
        this.singletonBeanNamesByType.clear();
    }

    /**********************************************************************
     * Implementation of ListableBeanFactory interface start
     * @see ListableBeanFactory
     *********************************************************************/

    public <T> T getBean(Class<T> requiredType) throws BeansException {
        Assert.notNull(requiredType, "Required type must not be null");
        String[] beanNames = getBeanNamesForType(requiredType);
        if (beanNames.length > 1) {
            ArrayList<String> autowireCandidates = new ArrayList<String>();
            for (String beanName : beanNames) {
                if (!containsBeanDefinition(beanName) || getBeanDefinition(beanName).isAutowireCandidate()) {
                    autowireCandidates.add(beanName);
                }
            }
            if (autowireCandidates.size() > 0) {
                beanNames = autowireCandidates.toArray(new String[autowireCandidates.size()]);
            }
        }
        if (beanNames.length == 1) {
            return getBean(beanNames[0], requiredType);
        }
        else if (beanNames.length > 1) {
            T primaryBean = null;
            for (String beanName : beanNames) {
                T beanInstance = getBean(beanName, requiredType);
                if (isPrimary(beanName, beanInstance)) {
                    if (primaryBean != null) {
                        throw new NoUniqueBeanDefinitionException(requiredType, beanNames.length,
                                "more than one 'primary' bean found of required type: " + Arrays.asList(beanNames));
                    }
                    primaryBean = beanInstance;
                }
            }
            if (primaryBean != null) {
                return primaryBean;
            }
            throw new NoUniqueBeanDefinitionException(requiredType, beanNames);
        }
//        else if (getParentBeanFactory() != null) {
//            return getParentBeanFactory().getBean(requiredType);
//        }
        else {
            throw new NoSuchBeanDefinitionException(requiredType);
        }
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        Assert.notNull(beanName, "Bean name must not be null");
        return this.beanDefinitionMap.containsKey(beanName);
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }


    public String[] getBeanDefinitionNames() {
        synchronized (this.beanDefinitionMap) {
            return StringUtils.toStringArray(this.beanDefinitionNames);
        }
    }

    public String[] getBeanNamesForType(Class<?> type) {
        return getBeanNamesForType(type, true, true);
    }

    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        if (type == null || !allowEagerInit) {
            return doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
        }
        Map<Class<?>, String[]> cache =
                (includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType);
        String[] resolvedBeanNames = cache.get(type);
        if (resolvedBeanNames != null) {
            return resolvedBeanNames;
        }
        resolvedBeanNames = doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
        if (ClassUtils.isCacheSafe(type, getBeanClassLoader())) {
            cache.put(type, resolvedBeanNames);
        }
        return resolvedBeanNames;
    }

    private String[] doGetBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        List<String> result = new ArrayList<String>();
        // Check all bean definitions.
        String[] beanDefinitionNames = getBeanDefinitionNames();
        for (String beanName : beanDefinitionNames) {
            try {
                RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                // Only check bean definition if it is complete.
                if (!mbd.isAbstract() && (allowEagerInit ||
                        ((mbd.hasBeanClass() || !mbd.isLazyInit())) ) ) {
                    // In case of FactoryBean, match object created by FactoryBean.
                    boolean isFactoryBean = isFactoryBean(beanName, mbd);
                    boolean matchFound = (allowEagerInit || !isFactoryBean || containsSingleton(beanName)) &&
                            (includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type);
                    if (!matchFound && isFactoryBean) {
                        // In case of FactoryBean, try to match FactoryBean instance itself next.
                        beanName = FACTORY_BEAN_PREFIX + beanName;
                        matchFound = (includeNonSingletons || mbd.isSingleton()) && isTypeMatch(beanName, type);
                    }
                    if (matchFound) {
                        result.add(beanName);
                    }
                }
            }
            catch (CannotLoadBeanClassException ex) {
                if (allowEagerInit) {
                    throw ex;
                }
                // Probably contains a placeholder: let's ignore it for type matching purposes.
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Ignoring bean class loading failure for bean '" + beanName + "'", ex);
                }
                onSuppressedException(ex);
            }
            catch (BeanDefinitionStoreException ex) {
                if (allowEagerInit) {
                    throw ex;
                }
                // Probably contains a placeholder: let's ignore it for type matching purposes.
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Ignoring unresolvable metadata in bean definition '" + beanName + "'", ex);
                }
                onSuppressedException(ex);
            }

        }

        // Check singletons too, to catch manually registered singletons.
        String[] singletonNames = getSingletonNames();
        for (String beanName : singletonNames) {
            // Only check if manually registered.
            if (!containsBeanDefinition(beanName)) {
                // In case of FactoryBean, match object created by FactoryBean.
                if (isFactoryBean(beanName)) {
                    if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
                        result.add(beanName);
                        // Match found for this bean: do not match FactoryBean itself anymore.
                        continue;
                    }
                    // In case of FactoryBean, try to match FactoryBean itself next.
                    beanName = FACTORY_BEAN_PREFIX + beanName;
                }
                // Match raw bean instance (might be raw FactoryBean).
                if (isTypeMatch(beanName, type)) {
                    result.add(beanName);
                }
            }
        }

        return StringUtils.toStringArray(result);
    }


    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return getBeansOfType(type, true, true);
    }

    public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
            throws BeansException {

        String[] beanNames = getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
        Map<String, T> result = new LinkedHashMap<String, T>(beanNames.length);
        for (String beanName : beanNames) {
            try {
                result.put(beanName, getBean(beanName, type));
            }
            catch (BeanCreationException ex) {
//                Throwable rootCause = ex.getMostSpecificCause();
//                if (rootCause instanceof BeanCurrentlyInCreationException) {
//                    BeanCreationException bce = (BeanCreationException) rootCause;
//                    if (isCurrentlyInCreation(bce.getBeanName())) {
//                        if (this.logger.isDebugEnabled()) {
//                            this.logger.debug("Ignoring match to currently created bean '" + beanName + "': " +
//                                    ex.getMessage());
//                        }
//                        onSuppressedException(ex);
//                        // Ignore: indicates a circular reference when autowiring constructors.
//                        // We want to find matches other than the currently created bean itself.
//                        continue;
//                    }
//                }
                throw ex;
            }
        }
        return result;
    }


    /**********************************************************************
     * Implementation of ListableBeanFactory interface end
     *********************************************************************/

    /**
     * Return whether the bean definition for the given bean name has been
     * marked as a primary bean.
     * @param beanName the name of the bean
     * @param beanInstance the corresponding bean instance
     * @return whether the given bean qualifies as primary
     */
    protected boolean isPrimary(String beanName, Object beanInstance) {
        if (containsBeanDefinition(beanName)) {
            return getMergedLocalBeanDefinition(beanName).isPrimary();
        }
        return false;
//        BeanFactory parentFactory = getParentBeanFactory();
//        return (parentFactory instanceof DefaultListableBeanFactory &&
//                ((DefaultListableBeanFactory) parentFactory).isPrimary(beanName, beanInstance));
    }
}
