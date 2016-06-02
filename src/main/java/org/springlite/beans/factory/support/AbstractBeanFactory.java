package org.springlite.beans.factory.support;

import com.sun.corba.se.impl.io.TypeMismatchException;
import org.springlite.beans.exception.*;
import org.springlite.beans.factory.*;
import org.springlite.beans.factory.config.BeanDefinition;
import org.springlite.beans.PropertyEditorRegistry;
import org.springlite.beans.factory.config.BeanPostProcessor;
import org.springlite.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springlite.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springlite.core.NamedThreadLocal;
import org.springlite.util.Assert;
import org.springlite.util.ClassUtils;

import java.beans.PropertyEditor;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by swy on 2016/5/25.
 */
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {

    /** ClassLoader to resolve bean class names with, if necessary */
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    //private TypeConverter typeConverter;

    /** Custom PropertyEditors to apply to the beans of this factory */
    private final Map<Class<?>, Class<? extends PropertyEditor>> customEditors = new HashMap<Class<?>, Class<? extends PropertyEditor>>(4);

    /** BeanPostProcessors to apply in createBean */
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<BeanPostProcessor>();

    /** Indicates whether any InstantiationAwareBeanPostProcessors have been registered */
    private boolean hasInstantiationAwareBeanPostProcessors;

    /** Indicates whether any DestructionAwareBeanPostProcessors have been registered */
    private boolean hasDestructionAwareBeanPostProcessors;

    /** Map from bean name to merged RootBeanDefinition */
    private final Map<String, RootBeanDefinition> mergedBeanDefinitions =
            new ConcurrentHashMap<String, RootBeanDefinition>(64);

    /** Names of beans that have already been created at least once */
    private final Map<String, Boolean> alreadyCreated = new ConcurrentHashMap<String, Boolean>(64);

    /** Names of beans that are currently in creation */
    private final ThreadLocal<Object> prototypesCurrentlyInCreation =
            new NamedThreadLocal<Object>("Prototype beans currently in creation");

    /**
     * Create a new AbstractBeanFactory.
     */
    public AbstractBeanFactory() {
    }

    //---------------------------------------------------------------------
    // Implementation of BeanFactory interface
    //---------------------------------------------------------------------

    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null, null, false);
    }

    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return doGetBean(name, requiredType, null, false);
    }

    public Object getBean(String name, Object... args) throws BeansException {
        return doGetBean(name, null, args, false);
    }

    /**
     * Return an instance, which may be shared or independent, of the specified bean.
     * @param name the name of the bean to retrieve
     * @param requiredType the required type of the bean to retrieve
     * @param args arguments to use if creating a prototype using explicit arguments to a
     * static factory method. It is invalid to use a non-null args value in any other case.
     * @return an instance of the bean
     * @throws BeansException if the bean could not be created
     */
    public <T> T getBean(String name, Class<T> requiredType, Object... args) throws BeansException {
        return doGetBean(name, requiredType, args, false);
    }

    /**
     * Return an instance, which may be shared or independent, of the specified bean.
     * @param name the name of the bean to retrieve
     * @param requiredType the required type of the bean to retrieve
     * @param args arguments to use if creating a prototype using explicit arguments to a
     * static factory method. It is invalid to use a non-null args value in any other case.
     * @param typeCheckOnly whether the instance is obtained for a type check,
     * not for actual use
     * @return an instance of the bean
     * @throws BeansException if the bean could not be created
     */
    @SuppressWarnings("unchecked")
    protected <T> T doGetBean(
            final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly)
            throws BeansException {

        final String beanName = transformedBeanName(name);
        Object bean;

        // Eagerly check singleton cache for manually registered singletons.
        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null && args == null) {
            if (logger.isDebugEnabled()) {
                if (isSingletonCurrentlyInCreation(beanName)) {
                    logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
                            "' that is not fully initialized yet - a consequence of a circular reference");
                }
                else {
                    logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
                }
            }
            bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
        }

        else {
            if (!typeCheckOnly) {
                markBeanAsCreated(beanName);
            }

            try {
                final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

                // Guarantee initialization of beans that the current bean depends on.
                String[] dependsOn = mbd.getDependsOn();
                if (dependsOn != null) {
                    for (String dependsOnBean : dependsOn) {
                        getBean(dependsOnBean);
                        registerDependentBean(dependsOnBean, beanName);
                    }
                }

                // Create bean instance.
                if (mbd.isSingleton()) {
                    sharedInstance = getSingleton(beanName, new ObjectFactory<Object>(){
                        public Object getObject() throws BeansException {
                            try {
                                return createBean(beanName, mbd, args);
                            } catch (BeansException ex) {
                                // Explicitly remove instance from singleton cache: It might have been put there
                                // eagerly by the creation process, to allow for circular reference resolution.
                                // Also remove any beans that received a temporary reference to the bean.
                                destroySingleton(beanName);
                                throw ex;
                            }
                        }
                    });
                    bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
                }

                else if (mbd.isPrototype()) {
                    // It's a prototype -> create a new instance.
                    Object prototypeInstance = null;
                    try {
                        beforePrototypeCreation(beanName);
                        prototypeInstance = createBean(beanName, mbd, args);
                    }
                    finally {
                        afterPrototypeCreation(beanName);
                    }
                    bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
                }
                else {
                    //TODO other scopes
                    throw new BeansException(requiredType +
                            " bean named '"+ beanName +"' use unsupported scope '"+ mbd.getScope() +"', please check!");
                }
            }
            catch (BeansException ex) {
                cleanupAfterBeanCreationFailure(beanName);
                throw ex;
            }
        }

        // Check if required type matches the type of the actual bean instance.
        if (requiredType != null && bean != null && !requiredType.isAssignableFrom(bean.getClass())) {
            try {
                //return getTypeConverter().convertIfNecessary(bean, requiredType);
            }
            catch (TypeMismatchException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to convert bean '" + name + "' to required type [" +
                            ClassUtils.getQualifiedName(requiredType) + "]", ex);
                }
                throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
            }
        }
        return (T) bean;
    }

    @SuppressWarnings("unchecked")
    protected void beforePrototypeCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        if (curVal == null) {
            this.prototypesCurrentlyInCreation.set(beanName);
        }
        else if (curVal instanceof String) {
            Set<String> beanNameSet = new HashSet<String>(2);
            beanNameSet.add((String) curVal);
            beanNameSet.add(beanName);
            this.prototypesCurrentlyInCreation.set(beanNameSet);
        }
        else {
            Set<String> beanNameSet = (Set<String>) curVal;
            beanNameSet.add(beanName);
        }
    }
    @SuppressWarnings("unchecked")
    protected void afterPrototypeCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        if (curVal instanceof String) {
            this.prototypesCurrentlyInCreation.remove();
        }
        else if (curVal instanceof Set) {
            Set<String> beanNameSet = (Set<String>) curVal;
            beanNameSet.remove(beanName);
            if (beanNameSet.isEmpty()) {
                this.prototypesCurrentlyInCreation.remove();
            }
        }
    }

    protected void cleanupAfterBeanCreationFailure(String beanName) {
        this.alreadyCreated.remove(beanName);
    }

    protected String transformedBeanName(String name) {
        return BeanFactoryUtils.transformedBeanName(name);
    }

    protected void markBeanAsCreated(String beanName) {
        if (!this.alreadyCreated.containsKey(beanName)) {
            this.alreadyCreated.put(beanName, Boolean.TRUE);
        }
    }

    protected Object getObjectForBeanInstance(
            Object beanInstance, String name, String beanName, RootBeanDefinition mbd) {

        // Now we have the bean instance, which may be a normal bean or a FactoryBean.
        // If it's a FactoryBean, we use it to create a bean instance, unless the
        // caller actually wants a reference to the factory.
        if (!(beanInstance instanceof FactoryBean)) {
            return beanInstance;
        }

        Object object = null;
        if (mbd == null) {
            object = getCachedObjectForFactoryBean(beanName);
        }
        if (object == null) {
            // Return bean instance from factory.
            FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
            // Caches object obtained from FactoryBean if it is a singleton.
            if (mbd == null && containsBeanDefinition(beanName)) {
                mbd = getMergedLocalBeanDefinition(beanName);
            }
            boolean synthetic = (mbd != null && mbd.isSynthetic());
            object = getObjectFromFactoryBean(factory, beanName, !synthetic);
        }
        return object;
    }


    protected void registerCustomEditors(PropertyEditorRegistry registry){
        //TODO
    }

    /**
     * 从本地mergedBeanDefinitions中取RootBeanDefinition，减少lock时间
     * @param beanName
     * @return
     * @throws BeansException
     */
    protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
        RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
        if (mbd != null) {
            return mbd;
        }
        BeanDefinition tempbd = getBeanDefinition(beanName);
        if(tempbd != null){
            synchronized (this.mergedBeanDefinitions) {
                mbd = this.mergedBeanDefinitions.get(beanName);
                if(mbd != null){
                    return mbd;
                }else{
                    // Use copy of given root bean definition.
                    if (tempbd instanceof RootBeanDefinition) {
                        mbd = ((RootBeanDefinition) tempbd).cloneBeanDefinition();
                    } else {
                        mbd = new RootBeanDefinition(tempbd);
                    }
                    this.mergedBeanDefinitions.put(beanName, mbd);
                }
            }
            return mbd;
        }
        throw new NoSuchBeanDefinitionException(beanName);
    }

    /**********************************************************************
     * Implementation of ConfigurableBeanFactory interface start
     * @see ConfigurableBeanFactory
     **********************************************************************/

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = (beanClassLoader != null ? beanClassLoader : ClassUtils.getDefaultClassLoader());
    }

    @Override
    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException{
        return getMergedLocalBeanDefinition(beanName);
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        Assert.notNull(beanPostProcessor, "processor must not be null");
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
        if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
            this.hasInstantiationAwareBeanPostProcessors = true;
        }
        if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
            this.hasDestructionAwareBeanPostProcessors = true;
        }
    }

    @Override
    public int getBeanPostProcessorCount() {
        return this.beanPostProcessors.size();
    }

    @Override
    public void destroyBean(String beanName, Object beanInstance) {
        destroyBean(beanName, beanInstance, getMergedLocalBeanDefinition(beanName));
    }

    /**
     * Destroy the given bean instance (usually a prototype instance
     * obtained from this factory) according to the given bean definition.
     * @param beanName the name of the bean definition
     * @param beanInstance the bean instance to destroy
     * @param mbd the merged bean definition
     */
    protected void destroyBean(String beanName, Object beanInstance, RootBeanDefinition mbd) {
        new DisposableBeanAdapter(beanInstance, beanName, mbd, getBeanPostProcessors(), getAccessControlContext()).destroy();
    }

    @Override
    public void destroyScopedBean(String beanName) {
//        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
//        if (mbd.isSingleton() || mbd.isPrototype()) {
//            throw new IllegalArgumentException(
//                    "Bean name '" + beanName + "' does not correspond to an object in a mutable scope");
//        }
//        String scopeName = mbd.getScope();
//        Scope scope = this.scopes.get(scopeName);
//        if (scope == null) {
//            throw new IllegalStateException("No Scope SPI registered for scope '" + scopeName + "'");
//        }
//        Object bean = scope.remove(beanName);
//        if (bean != null) {
//            destroyBean(beanName, bean, mbd);
//        }
    }


    /**********************************************************************
     * Implementation of ConfigurableBeanFactory interface end
     **********************************************************************/

    /**
     * Return the list of BeanPostProcessors that will get applied
     * to beans created with this factory.
     */
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    /**
     * Return whether this factory holds a InstantiationAwareBeanPostProcessor
     * that will get applied to singleton beans on shutdown.
     * @see #addBeanPostProcessor
     * @see org.springlite.beans.factory.config.InstantiationAwareBeanPostProcessor
     */
    protected boolean hasInstantiationAwareBeanPostProcessors() {
        return this.hasInstantiationAwareBeanPostProcessors;
    }

    /**
     * Return whether this factory holds a DestructionAwareBeanPostProcessor
     * that will get applied to singleton beans on shutdown.
     * @see #addBeanPostProcessor
     * @see org.springlite.beans.factory.config.DestructionAwareBeanPostProcessor
     */
    protected boolean hasDestructionAwareBeanPostProcessors() {
        return this.hasDestructionAwareBeanPostProcessors;
    }


    /**
     * Remove the merged bean definition for the specified bean,
     * recreating it on next access.
     * @param beanName the bean name to clear the merged definition for
     */
    protected void clearMergedBeanDefinition(String beanName) {
        this.mergedBeanDefinitions.remove(beanName);
    }


    public boolean containsBean(String name) {
        String beanName = transformedBeanName(name);
        if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
            //return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(name));
            return true;
        }
//        // Not found -> check parent.
//        BeanFactory parentBeanFactory = getParentBeanFactory();
//        return (parentBeanFactory != null && parentBeanFactory.containsBean(originalBeanName(name)));
        return false;
    }

    public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            return (beanInstance instanceof FactoryBean);
        }
        else if (containsSingleton(beanName)) {
            // null instance registered
            return false;
        }

//        // No singleton instance found -> check bean definition.
//        if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
//            // No bean definition found in this factory -> delegate to parent.
//            return ((ConfigurableBeanFactory) getParentBeanFactory()).isFactoryBean(name);
//        }

        return isFactoryBean(beanName, getMergedLocalBeanDefinition(beanName));
    }

    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean) {
                return ((FactoryBean<?>) beanInstance).isSingleton();
            }
        } else if (containsSingleton(beanName)) {
            return true;
        } else {
            RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

            // In case of FactoryBean, return singleton status of created object if not a dereference.
            if (mbd.isSingleton()) {
                return true;
            }
        }
        return false;
    }

    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        return !isSingleton(name);
    }

    public boolean isTypeMatch(String name, Class<?> targetType) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);
        Class<?> typeToMatch = (targetType != null ? targetType : Object.class);

        // Check manually registered singletons.
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean) {
                if (!BeanFactoryUtils.isFactoryDereference(name)) {
                    Class<?> type = getTypeForFactoryBean((FactoryBean<?>) beanInstance);
                    return (type != null && ClassUtils.isAssignable(typeToMatch, type));
                }
                else {
                    return ClassUtils.isAssignableValue(typeToMatch, beanInstance);
                }
            }
            else {
                return !BeanFactoryUtils.isFactoryDereference(name) &&
                        ClassUtils.isAssignableValue(typeToMatch, beanInstance);
            }
        }
        else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            // null instance registered
            return false;
        }

        else {

            // Retrieve corresponding bean definition.
            RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

            Class<?>[] typesToMatch = (FactoryBean.class.equals(typeToMatch) ?
                    new Class<?>[] {typeToMatch} : new Class<?>[] {FactoryBean.class, typeToMatch});

//            // Check decorated bean definition, if any: We assume it'll be easier
//            // to determine the decorated bean's type than the proxy's type.
//            BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
//            if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
//                RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
//                Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd, typesToMatch);
//                if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
//                    return typeToMatch.isAssignableFrom(targetClass);
//                }
//            }

            Class<?> beanType = predictBeanType(beanName, mbd, typesToMatch);
            if (beanType == null) {
                return false;
            }

            // Check bean class whether we're dealing with a FactoryBean.
            if (FactoryBean.class.isAssignableFrom(beanType)) {
                if (!BeanFactoryUtils.isFactoryDereference(name)) {
                    // If it's a FactoryBean, we want to look at what it creates, not the factory class.
                    beanType = getTypeForFactoryBean(beanName, mbd);
                    if (beanType == null) {
                        return false;
                    }
                }
            }
            else if (BeanFactoryUtils.isFactoryDereference(name)) {
                // Special case: A SmartInstantiationAwareBeanPostProcessor returned a non-FactoryBean
                // type but we nevertheless are being asked to dereference a FactoryBean...
                // Let's check the original bean class and proceed with it if it is a FactoryBean.
                beanType = predictBeanType(beanName, mbd, FactoryBean.class);
                if (beanType == null || !FactoryBean.class.isAssignableFrom(beanType)) {
                    return false;
                }
            }

            return typeToMatch.isAssignableFrom(beanType);
        }
    }

    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

        // Check manually registered singletons.
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
                return getTypeForFactoryBean((FactoryBean<?>) beanInstance);
            }
            else {
                return beanInstance.getClass();
            }
        }
        else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            // null instance registered
            return null;
        }

        else {
            // No singleton instance found -> check bean definition.
//            BeanFactory parentBeanFactory = getParentBeanFactory();
//            if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
//                // No bean definition found in this factory -> delegate to parent.
//                return parentBeanFactory.getType(originalBeanName(name));
//            }

            RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

            // Check decorated bean definition, if any: We assume it'll be easier
            // to determine the decorated bean's type than the proxy's type.
//            BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
//            if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
//                RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
//                Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd);
//                if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
//                    return targetClass;
//                }
//            }

            Class<?> beanClass = predictBeanType(beanName, mbd);

            // Check bean class whether we're dealing with a FactoryBean.
            if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass)) {
                if (!BeanFactoryUtils.isFactoryDereference(name)) {
                    // If it's a FactoryBean, we want to look at what it creates, not at the factory class.
                    return getTypeForFactoryBean(beanName, mbd);
                }
                else {
                    return beanClass;
                }
            }
            else {
                return (!BeanFactoryUtils.isFactoryDereference(name) ? beanClass : null);
            }
        }
    }

    /**
     * Predict the eventual bean type (of the processed bean instance) for the
     * specified bean. Called by {@link #getType} and {@link #isTypeMatch}.
     * Does not need to handle FactoryBeans specifically, since it is only
     * supposed to operate on the raw bean type.
     * <p>This implementation is simplistic in that it is not able to
     * handle factory methods and InstantiationAwareBeanPostProcessors.
     * It only predicts the bean type correctly for a standard bean.
     * To be overridden in subclasses, applying more sophisticated type detection.
     * @param beanName the name of the bean
     * @param mbd the merged bean definition to determine the type for
     * @param typesToMatch the types to match in case of internal type matching purposes
     * (also signals that the returned {@code Class} will never be exposed to application code)
     * @return the type of the bean, or {@code null} if not predictable
     */
    protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
//        if (mbd.getFactoryMethodName() != null) {
//            return null;
//        }
        return resolveBeanClass(mbd, beanName, typesToMatch);
    }

    /**
     * Check whether the given bean is defined as a {@link FactoryBean}.
     * @param beanName the name of the bean
     * @param mbd the corresponding bean definition
     */
    protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {
        Class<?> beanType = predictBeanType(beanName, mbd, FactoryBean.class);
        return (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
    }

    /**
     * Determine the bean type for the given FactoryBean definition, as far as possible.
     * Only called if there is no singleton instance registered for the target bean already.
     * <p>The default implementation creates the FactoryBean via {@code getBean}
     * to call its {@code getObjectType} method. Subclasses are encouraged to optimize
     * this, typically by just instantiating the FactoryBean but not populating it yet,
     * trying whether its {@code getObjectType} method already returns a type.
     * If no type found, a full FactoryBean creation as performed by this implementation
     * should be used as fallback.
     * @param beanName the name of the bean
     * @param mbd the merged bean definition for the bean
     * @return the type for the bean if determinable, or {@code null} else
     * @see org.springlite.beans.factory.FactoryBean#getObjectType()
     * @see #getBean(String)
     */
    protected Class<?> getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
        if (!mbd.isSingleton()) {
            return null;
        }
        try {
            FactoryBean<?> factoryBean = doGetBean(FACTORY_BEAN_PREFIX + beanName, FactoryBean.class, null, true);
            return getTypeForFactoryBean(factoryBean);
        }
        catch (BeanCreationException ex) {
            // Can only happen when getting a FactoryBean.
            if (logger.isDebugEnabled()) {
                logger.debug("Ignoring bean creation exception on FactoryBean type check: " + ex);
            }
            onSuppressedException(ex);
            return null;
        }
    }

    /**
     * Resolve the bean class for the specified bean definition,
     * resolving a bean class name into a Class reference (if necessary)
     * and storing the resolved Class in the bean definition for further use.
     * @param mbd the merged bean definition to determine the class for
     * @param beanName the name of the bean (for error handling purposes)
     * @param typesToMatch the types to match in case of internal type matching purposes
     * (also signals that the returned {@code Class} will never be exposed to application code)
     * @return the resolved bean class (or {@code null} if none)
     * @throws CannotLoadBeanClassException if we failed to load the class
     */
    protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch)
            throws CannotLoadBeanClassException {
        try {
            if (mbd.hasBeanClass()) {
                return mbd.getBeanClass();
            }
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
                    public Class<?> run() throws Exception {
                        return doResolveBeanClass(mbd, typesToMatch);
                    }
                }, getAccessControlContext());
            }
            else {
                return doResolveBeanClass(mbd, typesToMatch);
            }
        }
        catch (PrivilegedActionException pae) {
            ClassNotFoundException ex = (ClassNotFoundException) pae.getException();
            throw new CannotLoadBeanClassException(beanName, mbd.getBeanClassName(), ex);
        }
        catch (ClassNotFoundException ex) {
            throw new CannotLoadBeanClassException(beanName, mbd.getBeanClassName(), ex);
        }
        catch (LinkageError err) {
            throw new CannotLoadBeanClassException(beanName, mbd.getBeanClassName(), err);
        }
    }

    private Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch) throws ClassNotFoundException {
//        if (!ObjectUtils.isEmpty(typesToMatch)) {
//            ClassLoader tempClassLoader = getTempClassLoader();
//            if (tempClassLoader != null) {
//                if (tempClassLoader instanceof DecoratingClassLoader) {
//                    DecoratingClassLoader dcl = (DecoratingClassLoader) tempClassLoader;
//                    for (Class<?> typeToMatch : typesToMatch) {
//                        dcl.excludeClass(typeToMatch.getName());
//                    }
//                }
//                String className = mbd.getBeanClassName();
//                return (className != null ? ClassUtils.forName(className, tempClassLoader) : null);
//            }
//        }
        return mbd.resolveBeanClass(getBeanClassLoader());
    }

    //---------------------------------------------------------------------
    // Abstract methods to be implemented by subclasses
    //---------------------------------------------------------------------

    protected abstract boolean containsBeanDefinition(String beanName);

    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args)
            throws BeansException;

    // Abstract methods to be implemented by subclasses END

    protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
        AccessControlContext acc = (System.getSecurityManager() != null ? getAccessControlContext() : null);
        if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
            if (mbd.isSingleton()) {
                // Register a DisposableBean implementation that performs all destruction
                // work for the given bean: DestructionAwareBeanPostProcessors,
                // DisposableBean interface, custom destroy method.
                synchronized (super.disposableBeans) {
                    this.disposableBeans.put(beanName, new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
                }
//                registerDisposableBean(beanName,
//                        new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
            } else {
                // A bean with a custom scope...
            }
        }
    }

    protected boolean requiresDestruction(Object bean, RootBeanDefinition mbd) {
        return (bean != null &&
                (DisposableBeanAdapter.hasDestroyMethod(bean, mbd) || hasDestructionAwareBeanPostProcessors()));
    }

}
