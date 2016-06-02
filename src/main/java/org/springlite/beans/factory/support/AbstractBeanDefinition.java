package org.springlite.beans.factory.support;


import org.springlite.beans.factory.config.BeanDefinition;
import org.springlite.beans.ConstructorArgumentValues;
import org.springlite.beans.MutablePropertyValues;
import org.springlite.beans.factory.AutowireCapableBeanFactory;
import org.springlite.util.ClassUtils;
import org.springlite.util.ObjectUtils;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.util.*;


@SuppressWarnings("serial")
public abstract class AbstractBeanDefinition implements BeanDefinition, Cloneable {

    /**
     * Constant for the default scope name: "", equivalent to singleton status
     * but to be overridden from a parent bean definition (if applicable).
     */
    public static final String SCOPE_DEFAULT = "";

    /**
     * Constant that indicates no autowiring at all.
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;

    /**
     * Constant that indicates autowiring bean properties by name.
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    /**
     * Constant that indicates autowiring bean properties by type.
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

    /**
     * Constant that indicates autowiring a constructor.
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

    /**
     * Constant that indicates determining an appropriate autowire strategy
     * through introspection of the bean class.
     * @see #setAutowireMode
     * @deprecated as of Spring 3.0: If you are using mixed autowiring strategies,
     * use annotation-based autowiring for clearer demarcation of autowiring needs.
     */
    @Deprecated
    public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;

    /**
     * Constant that indicates no dependency check at all.
     * @see #setDependencyCheck
     */
    public static final int DEPENDENCY_CHECK_NONE = 0;

    /**
     * Constant that indicates dependency checking for object references.
     * @see #setDependencyCheck
     */
    public static final int DEPENDENCY_CHECK_OBJECTS = 1;

    /**
     * Constant that indicates dependency checking for "simple" properties.
     * @see #setDependencyCheck
     */
    public static final int DEPENDENCY_CHECK_SIMPLE = 2;

    /**
     * Constant that indicates dependency checking for all properties
     * (object references as well as "simple" properties).
     * @see #setDependencyCheck
     */
    public static final int DEPENDENCY_CHECK_ALL = 3;

    public static final String INFER_METHOD = "(inferred)";

    private String beanName;

    private volatile Object beanClass;

    private String scope = SCOPE_DEFAULT;

    private boolean singleton = true;

    private boolean prototype = false;

    private boolean abstractFlag = false;

    private boolean lazyInit = false;

    private int autowireMode = AUTOWIRE_NO;

    private int dependencyCheck = DEPENDENCY_CHECK_NONE;

    private String[] dependsOn;

    private boolean autowireCandidate = true;

    private boolean primary = false;

//    private final Map<String, AutowireCandidateQualifier> qualifiers =
//            new LinkedHashMap<String, AutowireCandidateQualifier>(0);

    //是否允许访问非public的方法
    private boolean nonPublicAccessAllowed = true;

    private ConstructorArgumentValues constructorArgumentValues;

    private MutablePropertyValues propertyValues;
//
//    private String factoryBeanName;
//
//    private String factoryMethodName;

    private String initMethodName;

    private String destroyMethodName;

    private boolean synthetic = false;

    private String description;

    private Resource resource;


    /**
     * Create a new AbstractBeanDefinition with default settings.
     */
    protected AbstractBeanDefinition() {
        this(null, null);
    }

    /**
     * Create a new AbstractBeanDefinition with the given
     * constructor argument values and property values.
     */
    protected AbstractBeanDefinition(ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        setConstructorArgumentValues(cargs);
        setPropertyValues(pvs);
    }

    /**
     * Create a new AbstractBeanDefinition as a deep copy of the given
     * bean definition.
     * @param original the original bean definition to copy from
     */
    protected AbstractBeanDefinition(BeanDefinition original) {
        //setParentName(original.getParentName());
        setBeanName(original.getBeanName());
        setBeanClassName(original.getBeanClassName());
//        setFactoryBeanName(original.getFactoryBeanName());
//        setFactoryMethodName(original.getFactoryMethodName());
        setScope(original.getScope());
        setAbstract(original.isAbstract());
        setLazyInit(original.isLazyInit());
        //setRole(original.getRole());
        setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
        setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
        //setSource(original.getSource());
        //copyAttributesFrom(original);

        if (original instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
            if (originalAbd.hasBeanClass()) {
                setBeanClass(originalAbd.getBeanClass());
            }
            setAutowireMode(originalAbd.getAutowireMode());
            setDependencyCheck(originalAbd.getDependencyCheck());
            setDependsOn(originalAbd.getDependsOn());
            setAutowireCandidate(originalAbd.isAutowireCandidate());
            //copyQualifiersFrom(originalAbd);
            setPrimary(originalAbd.isPrimary());
            //setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
            //setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
            setInitMethodName(originalAbd.getInitMethodName());
            //setEnforceInitMethod(originalAbd.isEnforceInitMethod());
            setDestroyMethodName(originalAbd.getDestroyMethodName());
            //setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
            //setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
            setSynthetic(originalAbd.isSynthetic());
            setResource(originalAbd.getResource());
        }
        else {
            //setResourceDescription(original.getResourceDescription());
        }
    }

    public String getBeanName(){
        return this.beanName;
    }

    public void setBeanName(String beanName){
        this.beanName = beanName;
    }

    /**
     * Return whether this definition specifies a bean class.
     */
    public boolean hasBeanClass() {
        return (this.beanClass instanceof Class);
    }

    /**
     * Specify the class for this bean.
     */
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Return the class of the wrapped bean, if already resolved.
     * @return the bean class, or {@code null} if none defined
     * @throws IllegalStateException if the bean definition does not define a bean class,
     * or a specified bean class name has not been resolved into an actual Class
     */
    public Class<?> getBeanClass() throws IllegalStateException {
        Object beanClassObject = this.beanClass;
        if (beanClassObject == null) {
            throw new IllegalStateException("No bean class specified on bean definition");
        }
        if (!(beanClassObject instanceof Class)) {
            throw new IllegalStateException(
                    "Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
        }
        return (Class<?>) beanClassObject;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClass = beanClassName;
    }

    public String getBeanClassName() {
        Object beanClassObject = this.beanClass;
        if (beanClassObject instanceof Class) {
            return ((Class<?>) beanClassObject).getName();
        }
        else {
            return (String) beanClassObject;
        }
    }

    /**
     * Determine the class of the wrapped bean, resolving it from a
     * specified class name if necessary. Will also reload a specified
     * Class from its name when called with the bean class already resolved.
     * @param classLoader the ClassLoader to use for resolving a (potential) class name
     * @return the resolved bean class
     * @throws ClassNotFoundException if the class name could be resolved
     */
    public Class<?> resolveBeanClass(ClassLoader classLoader) throws ClassNotFoundException {
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }

    /**
     * Set the name of the target scope for the bean.
     * <p>Default is singleton status, although this is only applied once
     * a bean definition becomes active in the containing factory. A bean
     * definition may eventually inherit its scope from a parent bean definitionFor this
     * reason, the default scope name is empty (empty String), with
     * singleton status being assumed until a resolved scope will be set.
     * @see #SCOPE_SINGLETON
     * @see #SCOPE_PROTOTYPE
     */
    public void setScope(String scope) {
        this.scope = scope;
        this.singleton = SCOPE_SINGLETON.equals(scope) || SCOPE_DEFAULT.equals(scope);
        this.prototype = SCOPE_PROTOTYPE.equals(scope);
    }

    /**
     * Return the name of the target scope for the bean.
     */
    public String getScope() {
        return this.scope;
    }

    /**
     * Return whether this a <b>Singleton</b>, with a single shared instance
     * returned from all calls.
     * @see #SCOPE_SINGLETON
     */
    public boolean isSingleton() {
        return this.singleton;
    }

    /**
     * Return whether this a <b>Prototype</b>, with an independent instance
     * returned for each call.
     * @see #SCOPE_PROTOTYPE
     */
    public boolean isPrototype() {
        return this.prototype;
    }

    /**
     * Set if this bean is "abstract", i.e. not meant to be instantiated itself but
     * rather just serving as parent for concrete child bean definitions.
     * <p>Default is "false". Specify true to tell the bean factory to not try to
     * instantiate that particular bean in any case.
     */
    public void setAbstract(boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
    }

    /**
     * Return whether this bean is "abstract", i.e. not meant to be instantiated
     * itself but rather just serving as parent for concrete child bean definitions.
     */
    public boolean isAbstract() {
        return this.abstractFlag;
    }

    /**
     * Set whether this bean should be lazily initialized.
     * <p>If {@code false}, the bean will get instantiated on startup by bean
     * factories that perform eager initialization of singletons.
     */
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    /**
     * Return whether this bean should be lazily initialized, i.e. not
     * eagerly instantiated on startup. Only applicable to a singleton bean.
     */
    public boolean isLazyInit() {
        return this.lazyInit;
    }


    /**
     * Set the autowire mode. This determines whether any automagical detection
     * and setting of bean references will happen. Default is AUTOWIRE_NO,
     * which means there's no autowire.
     * @param autowireMode the autowire mode to set.
     * Must be one of the constants defined in this class.
     * @see #AUTOWIRE_NO
     * @see #AUTOWIRE_BY_NAME
     * @see #AUTOWIRE_BY_TYPE
     * @see #AUTOWIRE_CONSTRUCTOR
     * @see #AUTOWIRE_AUTODETECT
     */
    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    /**
     * Return the autowire mode as specified in the bean definition.
     */
    public int getAutowireMode() {
        return this.autowireMode;
    }

    /**
     * Return the resolved autowire code,
     * (resolving AUTOWIRE_AUTODETECT to AUTOWIRE_CONSTRUCTOR or AUTOWIRE_BY_TYPE).
     * @see #AUTOWIRE_AUTODETECT
     * @see #AUTOWIRE_CONSTRUCTOR
     * @see #AUTOWIRE_BY_TYPE
     */
    public int getResolvedAutowireMode() {
        if (this.autowireMode == AUTOWIRE_AUTODETECT) {
            // Work out whether to apply setter autowiring or constructor autowiring.
            // If it has a no-arg constructor it's deemed to be setter autowiring,
            // otherwise we'll try constructor autowiring.
            Constructor<?>[] constructors = getBeanClass().getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterTypes().length == 0) {
                    return AUTOWIRE_BY_TYPE;
                }
            }
            return AUTOWIRE_CONSTRUCTOR;
        }
        else {
            return this.autowireMode;
        }
    }

    /**
     * Set the dependency check code.
     * @param dependencyCheck the code to set.
     * Must be one of the four constants defined in this class.
     * @see #DEPENDENCY_CHECK_NONE
     * @see #DEPENDENCY_CHECK_OBJECTS
     * @see #DEPENDENCY_CHECK_SIMPLE
     * @see #DEPENDENCY_CHECK_ALL
     */
    public void setDependencyCheck(int dependencyCheck) {
        this.dependencyCheck = dependencyCheck;
    }

    /**
     * Return the dependency check code.
     */
    public int getDependencyCheck() {
        return this.dependencyCheck;
    }

    /**
     * Set the names of the beans that this bean depends on being initialized.
     * The bean factory will guarantee that these beans get initialized first.
     * <p>Note that dependencies are normally expressed through bean properties or
     * constructor arguments. This property should just be necessary for other kinds
     * of dependencies like statics (*ugh*) or database preparation on startup.
     */
    public void setDependsOn(String[] dependsOn) {
        this.dependsOn = dependsOn;
    }

    /**
     * Return the bean names that this bean depends on.
     */
    public String[] getDependsOn() {
        return this.dependsOn;
    }

    /**
     * Set whether this bean is a candidate for getting autowired into some other bean.
     */
    public void setAutowireCandidate(boolean autowireCandidate) {
        this.autowireCandidate = autowireCandidate;
    }

    /**
     * Return whether this bean is a candidate for getting autowired into some other bean.
     */
    public boolean isAutowireCandidate() {
        return this.autowireCandidate;
    }

    /**
     * Set whether this bean is a primary autowire candidate.
     * If this value is true for exactly one bean among multiple
     * matching candidates, it will serve as a tie-breaker.
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * Return whether this bean is a primary autowire candidate.
     * If this value is true for exactly one bean among multiple
     * matching candidates, it will serve as a tie-breaker.
     */
    public boolean isPrimary() {
        return this.primary;
    }

//    /**
//     * Register a qualifier to be used for autowire candidate resolution,
//     * keyed by the qualifier's type name.
//     * @see AutowireCandidateQualifier#getTypeName()
//     */
//    public void addQualifier(AutowireCandidateQualifier qualifier) {
//        this.qualifiers.put(qualifier.getTypeName(), qualifier);
//    }
//
//    /**
//     * Return whether this bean has the specified qualifier.
//     */
//    public boolean hasQualifier(String typeName) {
//        return this.qualifiers.keySet().contains(typeName);
//    }
//
//    /**
//     * Return the qualifier mapped to the provided type name.
//     */
//    public AutowireCandidateQualifier getQualifier(String typeName) {
//        return this.qualifiers.get(typeName);
//    }
//
//    /**
//     * Return all registered qualifiers.
//     * @return the Set of {@link AutowireCandidateQualifier} objects.
//     */
//    public Set<AutowireCandidateQualifier> getQualifiers() {
//        return new LinkedHashSet<AutowireCandidateQualifier>(this.qualifiers.values());
//    }
//
//    /**
//     * Copy the qualifiers from the supplied AbstractBeanDefinition to this bean definition.
//     * @param source the AbstractBeanDefinition to copy from
//     */
//    public void copyQualifiersFrom(AbstractBeanDefinition source) {
//        Validate.notNull(source, "Source must not be null");
//        this.qualifiers.putAll(source.qualifiers);
//    }
//

    /**
     * Return whether to allow access to non-public constructors and methods.
     */
    public boolean isNonPublicAccessAllowed() {
        return this.nonPublicAccessAllowed;
    }

    /**
     * Specify constructor argument values for this bean.
     */
    public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
        this.constructorArgumentValues =
                (constructorArgumentValues != null ? constructorArgumentValues : new ConstructorArgumentValues());
    }

    /**
     * Return constructor argument values for this bean (never {@code null}).
     */
    public ConstructorArgumentValues getConstructorArgumentValues() {
        return this.constructorArgumentValues;
    }

    /**
     * Return if there are constructor argument values defined for this bean.
     */
    public boolean hasConstructorArgumentValues() {
        return !this.constructorArgumentValues.isEmpty();
    }

    /**
     * Specify property values for this bean, if any.
     */
    public void setPropertyValues(MutablePropertyValues propertyValues) {
        this.propertyValues = (propertyValues != null ? propertyValues : new MutablePropertyValues());
    }

    /**
     * Return property values for this bean (never {@code null}).
     */
    public MutablePropertyValues getPropertyValues() {
        return this.propertyValues;
    }


//    public void setFactoryBeanName(String factoryBeanName) {
//        this.factoryBeanName = factoryBeanName;
//    }
//
//    public String getFactoryBeanName() {
//        return this.factoryBeanName;
//    }
//
//    public void setFactoryMethodName(String factoryMethodName) {
//        this.factoryMethodName = factoryMethodName;
//    }
//
//    public String getFactoryMethodName() {
//        return this.factoryMethodName;
//    }

    /**
     * Set the name of the initializer method. The default is {@code null}
     * in which case there is no initializer method.
     */
    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    /**
     * Return the name of the initializer method.
     */
    public String getInitMethodName() {
        return this.initMethodName;
    }

    /**
     * Set the name of the destroy method. The default is {@code null}
     * in which case there is no destroy method.
     */
    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    /**
     * Return the name of the destroy method.
     */
    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }

    /**
     * Set whether this bean definition is 'synthetic', that is, not defined
     * by the application itself (for example, an infrastructure bean such
     * as a helper for auto-proxying, created through {@code &ltaop:config&gt;}).
     */
    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    /**
     * Return whether this bean definition is 'synthetic', that is,
     * not defined by the application itself.
     */
    public boolean isSynthetic() {
        return this.synthetic;
    }

    /**
     * Set a human-readable description of this bean definition.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * Set the resource that this bean definition came from
     * (for the purpose of showing context in case of errors).
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * Return the resource that this bean definition came from.
     */
    public Resource getResource() {
        return this.resource;
    }

    public String getResourceDescription(){
        return (this.resource != null?this.resource.description() : this.getBeanClassName());
    }


//    /**
//     * Validate this bean definition.
//     * @throws BeanDefinitionValidationException in case of validation failure
//     */
//    public void validate() throws BeanDefinitionValidationException {
//        if (!getMethodOverrides().isEmpty() && getFactoryMethodName() != null) {
//            throw new BeanDefinitionValidationException(
//                    "Cannot combine static factory method with method overrides: " +
//                            "the static factory method must create the instance");
//        }
//
//        if (hasBeanClass()) {
//            prepareMethodOverrides();
//        }
//    }

    /**
     * Public declaration of Object's {@code clone()} method.
     * Delegates to {@link #cloneBeanDefinition()}.
     * @see Object#clone()
     */
    @Override
    public Object clone() {
        return cloneBeanDefinition();
    }

    /**
     * Clone this bean definition.
     * To be implemented by concrete subclasses.
     * @return the cloned bean definition object
     */
    public abstract AbstractBeanDefinition cloneBeanDefinition();

    public BeanDefinition getOriginatingBeanDefinition() {
//        return (this.resource instanceof BeanDefinitionResource ?
//                ((BeanDefinitionResource) this.resource).getBeanDefinition() : null);
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AbstractBeanDefinition)) {
            return false;
        }

        AbstractBeanDefinition that = (AbstractBeanDefinition) other;

        if (!ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName())) return false;
        if (!ObjectUtils.nullSafeEquals(this.scope, that.scope)) return false;
        if (this.abstractFlag != that.abstractFlag) return false;
        if (this.lazyInit != that.lazyInit) return false;

        if (this.autowireMode != that.autowireMode) return false;
        if (this.dependencyCheck != that.dependencyCheck) return false;
        if (!Arrays.equals(this.dependsOn, that.dependsOn)) return false;
        if (this.autowireCandidate != that.autowireCandidate) return false;
        if (this.primary != that.primary) return false;

        if (!ObjectUtils.nullSafeEquals(this.constructorArgumentValues, that.constructorArgumentValues)) return false;
        if (!ObjectUtils.nullSafeEquals(this.propertyValues, that.propertyValues)) return false;

//        if (!ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName)) return false;
//        if (!ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName)) return false;
        if (!ObjectUtils.nullSafeEquals(this.initMethodName, that.initMethodName)) return false;
//        if (this.enforceInitMethod != that.enforceInitMethod) return false;
        if (!ObjectUtils.nullSafeEquals(this.destroyMethodName, that.destroyMethodName)) return false;
//        if (this.enforceDestroyMethod != that.enforceDestroyMethod) return false;

        if (this.synthetic != that.synthetic) return false;

        return super.equals(other);
    }

    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
//        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
//        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
        hashCode = 29 * hashCode + super.hashCode();
        return hashCode;
    }

}
