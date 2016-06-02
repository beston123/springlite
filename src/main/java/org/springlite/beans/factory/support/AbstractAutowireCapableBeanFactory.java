package org.springlite.beans.factory.support;

import org.springlite.beans.*;
import org.springlite.beans.exception.BeanCreationException;
import org.springlite.beans.exception.BeansException;
import org.springlite.beans.exception.NoSuchBeanDefinitionException;
import org.springlite.beans.factory.AutowireCapableBeanFactory;
import org.springlite.beans.factory.ObjectFactory;
import org.springlite.beans.factory.config.BeanDefinition;
import org.springlite.beans.factory.config.BeanPostProcessor;
import org.springlite.util.ClassUtils;
import org.springlite.util.ObjectUtils;
import org.springlite.util.ReflectionUtils;

import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * <p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/26
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
        implements AutowireCapableBeanFactory {

    /** Whether to automatically try to resolve circular references between beans */
    private boolean allowCircularReferences = true;

    //-------------------------------------------------------------------------
    // Typical methods for creating and populating external bean instances
    //-------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public <T> T createBean(Class<T> beanClass) throws BeansException {
        // Use prototype bean definition, to avoid registering bean as dependent bean.
        RootBeanDefinition bd = new RootBeanDefinition(beanClass);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        return (T) createBean(beanClass.getName(), bd, null);
    }

    public void autowireBean(Object existingBean) {
        // Use non-singleton bean definition, to avoid registering bean as dependent bean.
        RootBeanDefinition bd = new RootBeanDefinition(ClassUtils.getUserClass(existingBean));
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        populateBean(bd.getBeanClass().getName(), bd, bw);
    }

    public Object configureBean(Object existingBean, String beanName) throws BeansException {
        markBeanAsCreated(beanName);
        BeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        RootBeanDefinition bd = null;
        if (mbd instanceof RootBeanDefinition) {
            RootBeanDefinition rbd = (RootBeanDefinition) mbd;
            bd = rbd.isPrototype() ? rbd : rbd.cloneBeanDefinition();
        }
        if (!mbd.isPrototype()) {
            if (bd == null) {
                bd = new RootBeanDefinition((RootBeanDefinition) mbd);
            }
            bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        }
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        populateBean(beanName, bd, bw);
        return initializeBean(beanName, existingBean, bd);
    }

    /**
     * Initialize the given BeanWrapper with the custom editors registered
     * with this factory. To be called for BeanWrappers that will create
     * and populate bean instances.
     * <p>The default implementation delegates to {@link #registerCustomEditors}.
     * Can be overridden in subclasses.
     * @param bw the BeanWrapper to initialize
     */
    protected void initBeanWrapper(BeanWrapper bw) {
        //bw.setConversionService(getConversionService());
        //registerCustomEditors(bw); TODO
    }

    /**
     * Populate the bean instance in the given BeanWrapper with the property values
     * from the bean definition.
     * @param beanName the name of the bean
     * @param mbd the bean definition for the bean
     * @param bw BeanWrapper with bean instance
     */
    protected void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
        PropertyValues pvs = mbd.getPropertyValues();

        if (bw == null) {
            if (!pvs.isEmpty()) {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
            }
            else {
                // Skip property population phase for null instance.
                return;
            }
        }

        // Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
        // state of the bean before properties are set. This can be used, for example,
        // to support styles of field injection.
//        boolean continueWithPropertyPopulation = true;
//
//        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
//            for (processor bp : getBeanPostProcessors()) {
//                if (bp instanceof InstantiationAwareBeanPostProcessor) {
//                    InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
//                    if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
//                        continueWithPropertyPopulation = false;
//                        break;
//                    }
//                }
//            }
//        }
//
//        if (!continueWithPropertyPopulation) {
//            return;
//        }

        if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
                mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
            MutablePropertyValues newPvs = new MutablePropertyValues(pvs);

            // Add property values based on autowire by name if applicable.
            if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
                autowireByName(beanName, mbd, bw, newPvs);
            }

            // Add property values based on autowire by type if applicable.
            if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
                autowireByType(beanName, mbd, bw, newPvs);
            }

            pvs = newPvs;
        }

//        boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
//        boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);
//
//        if (hasInstAwareBpps || needsDepCheck) {
//            PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
//            if (hasInstAwareBpps) {
//                for (processor bp : getBeanPostProcessors()) {
//                    if (bp instanceof InstantiationAwareBeanPostProcessor) {
//                        InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
//                        pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
//                        if (pvs == null) {
//                            return;
//                        }
//                    }
//                }
//            }
//            if (needsDepCheck) {
//                checkDependencies(beanName, mbd, filteredPds, pvs);
//            }
//        }

        applyPropertyValues(beanName, mbd, bw, pvs);
    }

    /**
     * Fill in any missing property values with references to
     * other beans in this factory if autowire is set to "byName".
     * @param beanName the name of the bean we're wiring up.
     * Useful for debugging messages; not used functionally.
     * @param mbd bean definition to update through autowiring
     * @param bw BeanWrapper from which we can obtain information about the bean
     * @param pvs the PropertyValues to register wired objects with
     */
    protected void autowireByName(
            String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

//        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
//        for (String propertyName : propertyNames) {
//            if (containsBean(propertyName)) {
//                Object bean = getBean(propertyName);
//                pvs.add(propertyName, bean);
//                registerDependentBean(propertyName, beanName);
//                if (logger.isDebugEnabled()) {
//                    logger.debug("Added autowiring by name from bean name '" + beanName +
//                            "' via property '" + propertyName + "' to bean named '" + propertyName + "'");
//                }
//            }
//            else {
//                if (logger.isTraceEnabled()) {
//                    logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName +
//                            "' by name: no matching bean found");
//                }
//            }
//        }
    }

    /**
     * Abstract method defining "autowire by type" (bean properties by type) behavior.
     * <p>This is like PicoContainer default, in which there must be exactly one bean
     * of the property type in the bean factory. This makes bean factories simple to
     * configure for small namespaces, but doesn't work as well as standard Spring
     * behavior for bigger applications.
     * @param beanName the name of the bean to autowire by type
     * @param mbd the merged bean definition to update through autowiring
     * @param bw BeanWrapper from which we can obtain information about the bean
     * @param pvs the PropertyValues to register wired objects with
     */
    protected void autowireByType(
            String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

//        TypeConverter converter = getCustomTypeConverter();
//        if (converter == null) {
//            converter = bw;
//        }

//        Set<String> autowiredBeanNames = new LinkedHashSet<String>(4);
//        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
//        for (String propertyName : propertyNames) {
//            try {
//                PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
//                // Don't try autowiring by type for type Object: never makes sense,
//                // even if it technically is a unsatisfied, non-simple property.
//                if (!Object.class.equals(pd.getPropertyType())) {
//                    MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
//                    // Do not allow eager init for type matching in case of a prioritized post-processor.
//                    boolean eager = !PriorityOrdered.class.isAssignableFrom(bw.getWrappedClass());
//                    DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
//                    Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, converter);
//                    if (autowiredArgument != null) {
//                        pvs.add(propertyName, autowiredArgument);
//                    }
//                    for (String autowiredBeanName : autowiredBeanNames) {
//                        registerDependentBean(autowiredBeanName, beanName);
//                        if (logger.isDebugEnabled()) {
//                            logger.debug("Autowiring by type from bean name '" + beanName + "' via property '" +
//                                    propertyName + "' to bean named '" + autowiredBeanName + "'");
//                        }
//                    }
//                    autowiredBeanNames.clear();
//                }
//            }
//            catch (BeansException ex) {
//                throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, propertyName, ex);
//            }
//        }
    }


    /**
     * Create a new instance for the specified bean, using an appropriate instantiation strategy:
     * factory method, constructor autowiring, or simple instantiation.
     * @param beanName the name of the bean
     * @param mbd the bean definition for the bean
     * @param args arguments to use if creating a prototype using explicit arguments to a
     * static factory method. It is invalid to use a non-null args value in any other case.
     * @return BeanWrapper for the new instance
     * @see #
     * @see #autowireConstructor
     * @see #instantiateBean
     */
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {
        // Make sure bean class is actually resolved at this point.
        Class<?> beanClass = resolveBeanClass(mbd, beanName);

        if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
        }
        //无参数的构造方法
        if(mbd.getConstructorArgumentValues().getArgumentCount() == 0){
            return instantiateBean(beanName, mbd);
        }
//        if (mbd.getFactoryMethodName() != null)  {
//            return instantiateUsingFactoryMethod(beanName, mbd, args);
//        }
//
        // Shortcut when re-creating the same bean...
//        boolean resolved = false;
//        boolean autowireNecessary = false;
//        if (args == null) {
//            synchronized (mbd.constructorArgumentLock) {
//                if (mbd.resolvedConstructorOrFactoryMethod != null) {
//                    resolved = true;
//                    autowireNecessary = mbd.constructorArgumentsResolved;
//                }
//            }
//        }
//        if (resolved) {
//            if (autowireNecessary) {
//                return autowireConstructor(beanName, mbd, null, null);
//            }
//            else {
//                return instantiateBean(beanName, mbd);
//            }
//        }

        // Need to determine the constructor...
        Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
        if (ctors != null ||
                mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
                mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args))  {
            return autowireConstructor(beanName, mbd, ctors, args);
        }

        // No special handling: simply use no-arg constructor.
        return instantiateBean(beanName, mbd);
    }

    protected BeanWrapper autowireConstructor(
            String beanName, RootBeanDefinition mbd, Constructor<?>[] ctors, Object[] explicitArgs) {

        return new ConstructorResolver(this, beanName, mbd).autowireConstructor(ctors, explicitArgs);
    }


    /**
     * Determine candidate constructors to use for the given bean, checking all registered
     * @param beanClass the raw class of the bean
     * @param beanName the name of the bean
     * @return the candidate constructors, or {@code null} if none specified
     * @throws org.springlite.beans.exception.BeansException in case of errors
     */
    protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(Class<?> beanClass, String beanName)
            throws BeansException {

//        if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
//            for (processor bp : getBeanPostProcessors()) {
//                if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
//                    SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
//                    Constructor<?>[] ctors = ibp.determineCandidateConstructors(beanClass, beanName);
//                    if (ctors != null) {
//                        return ctors;
//                    }
//                }
//            }
//        }
        return beanClass.getConstructors();
    }

    /**
     * Instantiate the given bean using its default constructor.
     * @param beanName the name of the bean
     * @param mbd the bean definition for the bean
     * @return BeanWrapper for the new instance
     */
    protected BeanWrapper instantiateBean(final String beanName, final RootBeanDefinition mbd) {

        try {
            Object beanInstance;
//            final BeanFactory parent = this;
//            if (System.getSecurityManager() != null) {
//                beanInstance = AccessController.doPrivileged(new PrivilegedAction<Object>() {
//                    public Object run() {
//                        return getInstantiationStrategy().instantiate(mbd, beanName, parent);
//                    }
//                }, getAccessControlContext());
//            }
//            else {
//                beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
//            }
            beanInstance = mbd.getBeanClass().newInstance();
            BeanWrapper bw = new BeanWrapperImpl(beanInstance);
            initBeanWrapper(bw);
            return bw;
        }
        catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Instantiation of bean failed", ex);
        }
    }


    //-------------------------------------------------------------------------
    // Specialized methods for fine-grained control over the bean lifecycle
    //-------------------------------------------------------------------------

    public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
        // Use non-singleton bean definition, to avoid registering bean as dependent bean.
        RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        return createBean(beanClass.getName(), bd, null);
    }

    public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
        // Use non-singleton bean definition, to avoid registering bean as dependent bean.
        final RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        if (bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
            return null;//autowireConstructor(beanClass.getName(), bd, null, null).getWrappedInstance();
        }
        else {
            Object bean = null;
//            final BeanFactory parent = this;
//            if (System.getSecurityManager() != null) {
//                bean = AccessController.doPrivileged(new PrivilegedAction<Object>() {
//                    public Object run() {
//                        return getInstantiationStrategy().instantiate(bd, null, parent);
//                    }
//                }, getAccessControlContext());
//            }
//            else {
//                bean = getInstantiationStrategy().instantiate(bd, null, parent);
//            }
//            populateBean(beanClass.getName(), bd);
            return bean;
        }
    }

    public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
            throws BeansException {

        if (autowireMode == AUTOWIRE_CONSTRUCTOR) {
            throw new IllegalArgumentException("AUTOWIRE_CONSTRUCTOR not supported for existing bean instance");
        }
        // Use non-singleton bean definition, to avoid registering bean as dependent bean.
        RootBeanDefinition bd =
                new RootBeanDefinition(ClassUtils.getUserClass(existingBean), autowireMode, dependencyCheck);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        populateBean(bd.getBeanClass().getName(), bd, bw);
    }

    public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
        markBeanAsCreated(beanName);
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        applyPropertyValues(beanName, mbd, bw, mbd.getPropertyValues());
    }

    /**
     * Apply the given property values, resolving any runtime references
     * to other beans in this bean factory. Must use deep copy, so we
     * don't permanently modify this property.
     * @param beanName the bean name passed for better exception information
     * @param mbd the merged bean definition
     * @param bw the BeanWrapper wrapping the target object
     * @param pvs the new property values
     */
    protected void applyPropertyValues(String beanName, RootBeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) throws BeanCreationException{
        if (pvs == null || pvs.isEmpty()) {
            return;
        }
        Object bean = bw.getWrappedInstance();
        PropertyResolver propertyResolver = new PropertyResolver(this, beanName, mbd);
        for (PropertyValue propertyValue : pvs.getPropertyValues()) {
            propertyResolver.autowireProperty(bean, propertyValue);
        }
    }


    public Object initializeBean(Object existingBean, String beanName) {
        return initializeBean(beanName, existingBean, null);
    }

    /**
     * Initialize the given bean instance, applying factory callbacks
     * as well as init methods and bean post processors.
     * <p>Called from {@link #createBean} for traditionally defined beans,
     * and from {@link #initializeBean} for existing bean instances.
     * @param beanName the bean name in the factory (for debugging purposes)
     * @param bean the new bean instance we may need to initialize
     * @param mbd the bean definition that the bean was created with
     * (can also be {@code null}, if given an existing bean instance)
     * @return the initialized bean instance (potentially wrapped)
     * @see #applyBeanPostProcessorsBeforeInitialization
     * @see #invokeInitMethods
     * @see #applyBeanPostProcessorsAfterInitialization
     */
    protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    invokeAwareMethods(beanName, bean);
                    return null;
                }
            }, getAccessControlContext());
        }
        else {
            invokeAwareMethods(beanName, bean);
        }

        Object wrappedBean = bean;
        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
        }

        try {
            invokeInitMethods(beanName, wrappedBean, mbd);
        }
        catch (Throwable ex) {
            throw new BeansException(beanName, "Invocation of init method failed", ex);
        }

        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }
        return wrappedBean;
    }

    private void invokeAwareMethods(final String beanName, final Object bean) {
//        if (bean instanceof Aware) {
//            if (bean instanceof BeanNameAware) {
//                ((BeanNameAware) bean).setBeanName(beanName);
//            }
//            if (bean instanceof BeanClassLoaderAware) {
//                ((BeanClassLoaderAware) bean).setBeanClassLoader(getBeanClassLoader());
//            }
//            if (bean instanceof BeanFactoryAware) {
//                ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
//            }
//        }
    }

    /**
     * Give a bean a chance to react now all its properties are set,
     * and a chance to know about its owning bean factory (this object).
     * This means checking whether the bean implements InitializingBean or defines
     * a custom init method, and invoking the necessary callback(s) if it does.
     * @param beanName the bean name in the factory (for debugging purposes)
     * @param bean the new bean instance we may need to initialize
     * @param mbd the merged bean definition that the bean was created with
     * (can also be {@code null}, if given an existing bean instance)
     * @throws Throwable if thrown by init methods or by the invocation process
     * @see #invokeCustomInitMethod
     */
    protected void invokeInitMethods(String beanName, final Object bean, RootBeanDefinition mbd)
            throws Throwable {

        boolean isInitializingBean = (bean instanceof InitializingBean);
        if ( isInitializingBean /*&& (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))*/ ) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
            }
            if (System.getSecurityManager() != null) {
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                        public Object run() throws Exception {
                            ((InitializingBean) bean).afterPropertiesSet();
                            return null;
                        }
                    }, getAccessControlContext());
                }
                catch (PrivilegedActionException pae) {
                    throw pae.getException();
                }
            }
            else {
                ((InitializingBean) bean).afterPropertiesSet();
            }
        }

        if (mbd != null) {
            String initMethodName = mbd.getInitMethodName();
            if (initMethodName != null && !(isInitializingBean && "afterPropertiesSet".equals(initMethodName))) {
                invokeCustomInitMethod(beanName, bean, mbd);
            }
        }
    }

    /**
     * Invoke the specified custom init method on the given bean.
     * Called by invokeInitMethods.
     * <p>Can be overridden in subclasses for custom resolution of init
     * methods with arguments.
     * @see #invokeInitMethods
     */
    protected void invokeCustomInitMethod(String beanName, final Object bean, RootBeanDefinition mbd) throws Throwable {
        String initMethodName = mbd.getInitMethodName();
        final Method initMethod = BeanUtils.findMethod(bean.getClass(), initMethodName);
                //ClassUtils.getMethodIfAvailable(bean.getClass(), initMethodName);
        if (initMethod == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No default init method named '" + initMethodName +
                        "' found on bean with name '" + beanName + "'");
            }
            // Ignore non-existent default lifecycle methods.
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Invoking init method  '" + initMethodName + "' on bean with name '" + beanName + "'");
        }

        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    ReflectionUtils.makeAccessible(initMethod);
                    return null;
                }
            });
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    public Object run() throws Exception {
                        initMethod.invoke(bean);
                        return null;
                    }
                }, getAccessControlContext());
            }
            catch (PrivilegedActionException pae) {
                InvocationTargetException ex = (InvocationTargetException) pae.getException();
                throw ex.getTargetException();
            }
        }
        else {
            try {
                ReflectionUtils.makeAccessible(initMethod);
                initMethod.invoke(bean);
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }


    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
            throws BeansException {

        Object result = existingBean;
        for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
            result = beanProcessor.postProcessBeforeInitialization(result, beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }

    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
            throws BeansException {

        Object result = existingBean;
        for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
            result = beanProcessor.postProcessAfterInitialization(result, beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }


    /**********************************************************************
     * Implementation of relevant AbstractBeanFactory template methods
     *********************************************************************/
    /**
     * Central method of this class: creates a bean instance,
     * populates the bean instance, applies post-processors, etc.
     * @see #doCreateBean
     */
    @Override
    protected Object createBean(final String beanName, final RootBeanDefinition mbd, final Object[] args)
            throws BeanCreationException {

        if (logger.isDebugEnabled()) {
            logger.debug("Creating instance of bean '" + beanName + "'");
        }
        // Make sure bean class is actually resolved at this point.
        resolveBeanClass(mbd, beanName);
//
//        // Prepare method overrides.
//        try {
//            mbd.prepareMethodOverrides();
//        }
//        catch (BeanDefinitionValidationException ex) {
//            throw new BeanDefinitionStoreException(mbd.getResourceDescription(),
//                    beanName, "Validation of method overrides failed", ex);
//        }

        try {
            // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
            Object bean = resolveBeforeInstantiation(beanName, mbd);
            if (bean != null) {
                return bean;
            }
        }
        catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "processor before instantiation of bean failed", ex);
        }

        Object beanInstance = doCreateBean(beanName, mbd, args);
        if (logger.isDebugEnabled()) {
            logger.debug("Finished creating instance of bean '" + beanName + "'");
        }
        return beanInstance;
    }

    /**
     * Apply before-instantiation post-processors, resolving whether there is a
     * before-instantiation shortcut for the specified bean.
     * @param beanName the name of the bean
     * @param mbd the bean definition for the bean
     * @return the shortcut-determined bean instance, or {@code null} if none
     */
    protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
        Object bean = null;
//        if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
//            // Make sure bean class is actually resolved at this point.
//            if (mbd.hasBeanClass() && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
//                bean = applyBeanPostProcessorsBeforeInstantiation(mbd.getBeanClass(), beanName);
//                if (bean != null) {
//                    bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
//                }
//            }
//            mbd.beforeInstantiationResolved = (bean != null);
//        }
        return bean;
    }

    /**
     * Actually create the specified bean. Pre-creation processing has already happened
     * at this point, e.g. checking {@code postProcessBeforeInstantiation} callbacks.
     * <p>Differentiates between default bean instantiation, use of a
     * factory method, and autowiring a constructor.
     * @param beanName the name of the bean
     * @param mbd the merged bean definition for the bean
     * @param args arguments to use if creating a prototype using explicit arguments to a
     * static factory method. This parameter must be {@code null} except in this case.
     * @return a new instance of the bean
     * @throws BeanCreationException if the bean could not be created
     */
    protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) {
        // Instantiate the bean.
        BeanWrapper instanceWrapper = null;
//       if (mbd.isSingleton()) {
//            instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
//        }
//        if (instanceWrapper == null) {
            instanceWrapper = createBeanInstance(beanName, mbd, args);
//        }
        final Object bean = (instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null);
        Class<?> beanType = (instanceWrapper != null ? instanceWrapper.getWrappedClass() : null);
//
//        // Allow post-processors to modify the merged bean definition.
//        synchronized (mbd.postProcessingLock) {
//            if (!mbd.postProcessed) {
//                applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
//                mbd.postProcessed = true;
//            }
//        }
//
        // Eagerly cache singletons to be able to resolve circular references
        // even when triggered by lifecycle interfaces like BeanFactoryAware.
        boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
                isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
            if (logger.isDebugEnabled()) {
                logger.debug("Eagerly caching bean '" + beanName +
                        "' to allow for resolving potential circular references");
            }
            addSingletonFactory(beanName, new ObjectFactory<Object>() {
                public Object getObject() throws BeanCreationException {
                    return getEarlyBeanReference(beanName, mbd, bean);
                }
            });
        }

        // Initialize the bean instance.
        Object exposedObject = bean;
        try {
            populateBean(beanName, mbd, instanceWrapper);
            if (exposedObject != null) {
                exposedObject = initializeBean(beanName, exposedObject, mbd);
            }
        }
        catch (Throwable ex) {
            if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
                throw (BeanCreationException) ex;
            }
            else {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
            }
        }

        //构造方法
        if (earlySingletonExposure) {
            Object earlySingletonReference = getSingleton(beanName, false);
            if (earlySingletonReference != null) {
                if (exposedObject == bean) {
                    exposedObject = earlySingletonReference;
                }
//                else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
//                    String[] dependentBeans = getDependentBeans(beanName);
//                    Set<String> actualDependentBeans = new LinkedHashSet<String>(dependentBeans.length);
//                    for (String dependentBean : dependentBeans) {
//                        if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
//                            actualDependentBeans.add(dependentBean);
//                        }
//                    }
//                    if (!actualDependentBeans.isEmpty()) {
//                        throw new BeanCurrentlyInCreationException(beanName,
//                                "Bean with name '" + beanName + "' has been injected into other beans [" +
//                                        StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
//                                        "] in its raw version as part of a circular reference, but has eventually been " +
//                                        "wrapped. This means that said other beans do not use the final version of the " +
//                                        "bean. This is often the result of over-eager type matching - consider using " +
//                                        "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
//                    }
//                }
            }
        }

        // Register bean as disposable.
        try {
            registerDisposableBeanIfNecessary(beanName, bean, mbd);
        }
        catch (Exception ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
        }

        return exposedObject;
    }

    protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
        Object exposedObject = bean;
//        if (bean != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
//            for (processor bp : getBeanPostProcessors()) {
//                if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
//                    SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
//                    exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
//                    if (exposedObject == null) {
//                        return exposedObject;
//                    }
//                }
//            }
//        }
        return exposedObject;
    }


    public boolean isAllowCircularReferences() {
        return allowCircularReferences;
    }

    public void setAllowCircularReferences(boolean allowCircularReferences) {
        this.allowCircularReferences = allowCircularReferences;
    }

}
