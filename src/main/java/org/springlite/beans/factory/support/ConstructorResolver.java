package org.springlite.beans.factory.support;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springlite.beans.*;
import org.springlite.beans.ConstructorArgumentValues.ValueHolder;
import org.springlite.beans.exception.BeanCreationException;
import org.springlite.beans.factory.BeanFactory;
import org.springlite.util.Assert;
import org.springlite.util.ByteCodeUtils;
import org.springlite.util.ObjectUtils;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/29
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class ConstructorResolver {

    private static final Log logger = LogFactory.getLog(ConstructorResolver.class);


    private final BeanFactory beanFactory;

    private String beanName;

    private RootBeanDefinition mbd;


    /**
     * Create a new ConstructorResolver for the given factory and instantiation strategy.
     * @param beanFactory the BeanFactory to work with
     */
    public ConstructorResolver(BeanFactory beanFactory, String beanName, RootBeanDefinition mbd) {
        this.beanFactory = beanFactory;
        this.beanName = beanName;
        this.mbd = mbd;
    }

    public BeanWrapper autowireConstructor(Constructor<?>[] chosenCtors, Object[] constructorArgs) throws BeanCreationException {
        if(ObjectUtils.isEmpty(chosenCtors)){
            chosenCtors = mbd.getClass().getConstructors();
        }
        if(chosenCtors.length == 0){
            throw new BeanCreationException(mbd.getResourceDescription(), beanName +" has no constructors.");
        }

        Constructor<?> matchedConstructor = findMatchedConstructor(mbd, chosenCtors);
        constructorArgs = buildConstructorArgs(matchedConstructor, mbd.getConstructorArgumentValues());
        Object beanInstance = BeanUtils.instantiateClass(matchedConstructor, constructorArgs);
        return new BeanWrapperImpl(beanInstance);
    }

    private Constructor<?> findMatchedConstructor(final RootBeanDefinition mbd, Constructor<?>[] chosenCtors){
        ConstructorArgumentValues constructorArgumentValues = mbd.getConstructorArgumentValues();
        int argsCount = constructorArgumentValues.getArgumentCount();

        List<Constructor<?>> possibleCandidates = new ArrayList<Constructor<?>>(4);
        for(Constructor<?> constructor : chosenCtors){
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if(paramTypes.length == argsCount){
                possibleCandidates.add(constructor);
            }
        }
        if(possibleCandidates.size() == 0){
            throw new BeanCreationException(mbd.getResourceDescription(), mbd.getBeanName(), " cannot find a appropriate constructor.");
        }
        List<Constructor<?>> matchedConstructors = new ArrayList<Constructor<?>>(1);

        for(Constructor<?> constructor : possibleCandidates){
            if(matchedConstructorByConstructorArgs(constructor, constructorArgumentValues)){
                matchedConstructors.add(constructor);
            }
        }

        if(matchedConstructors.size() == 0){
            throw new BeanCreationException(mbd.getResourceDescription(), mbd.getBeanName(), " Can not find a appropriate constructor.");
        }else if(matchedConstructors.size() > 1){
            logger.warn("Find "+matchedConstructors.size()+" constructors. It' my fault!");
            throw new BeanCreationException(mbd.getResourceDescription(), mbd.getBeanName(),
                    " Except a appropriate constructor, but matched "+matchedConstructors.size()+" constructors. It' my fault!");
        }
        return matchedConstructors.get(0);
    }

    /***
     *
     * @param constructor
     * @param constructorArgumentValues
     * @return
     */
    private boolean matchedConstructorByConstructorArgs(Constructor<?> constructor, ConstructorArgumentValues constructorArgumentValues) {
        int autowireBy = constructorArgumentValues.getAutowrieBy();
        if(autowireBy == ConstructorArgumentValues.BY_INDEX){
            return matchByIndex(constructor, constructorArgumentValues.getIndexedArgumentValues());
        } else if (autowireBy == ConstructorArgumentValues.BY_NAME){
            return matchByName(constructor, constructorArgumentValues.getGenericArgumentValues());
        } else {
            //return matchByType(constructor, constructorArgumentValues.getGenericArgumentValues());
            throw new BeanCreationException(mbd.getResourceDescription(), mbd.getBeanName(),"Constructor args 不支持 ByType!");
        }
    }

    /**
     * match a constructor by constructor-args class type
     * @param constructor
     * @param genericArgs
     * @return
     */
    private boolean matchByType(Constructor<?> constructor, List<ValueHolder> genericArgs) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        List<Class<?>> paramTypeList = Arrays.asList(paramTypes);
        for(ValueHolder valueHolder : genericArgs){
            String type = valueHolder.getType();
            Assert.hasText(type, "constructor args 'type' should not be null!");
            if(!BeanUtils.isSimpleType(type)){
                return false;
            }
            Class<?> clazz = null;
            try {
                clazz = Class.forName(type);
            } catch (ClassNotFoundException e) {
                throw new BeanCreationException("Class '"+type+"' not found!",e);
            }
            if(!paramTypeList.contains(clazz)){
                return false;
            }else{

            }
        }
        return true;
    }



    /**
     * match a constructor by constructor-args index
     * @param constructor
     * @param indexedArgs
     * @return
     */
    private boolean matchByIndex(Constructor<?> constructor, Map<Integer, ValueHolder> indexedArgs) {
        Class<?>[] paramTypes = constructor.getParameterTypes();

        for(Map.Entry<Integer, ValueHolder> entry : indexedArgs.entrySet()){
            int index = entry.getKey();
            ValueHolder valueHolder = entry.getValue();

            //index超出构造参数数组下标
            if(paramTypes.length-1 < index){
                return false;
            }

            Object value = valueHolder.getValue();
            //是ref，必须是构造方法参数的子类，同类或者实现类
            if(value instanceof BeanReference){
                Object valueRef = getBeanReference((BeanReference) value);
                if( !paramTypes[index].isAssignableFrom(valueRef.getClass()) ){
                    if(logger.isDebugEnabled()){
                        logger.debug("Value class '"+value.getClass().getName()+
                                "' is not assignable from constructor param  '"+paramTypes[index].getName()+"'.");
                    }
                    return false;
                }
            //是普通value，则必须是简单类型的属性
            }else if(!BeanUtils.isSimpleProperty(paramTypes[index]) ){
                if(logger.isDebugEnabled()){
                    logger.debug("Constructor param '"+paramTypes[index].getName()
                            +"' is not simple property, but value is simple property.");
                }
                return false;
            }

        }
        return true;
    }

    /**
     * match a constructor by constructor-args name
     * @param constructor
     * @param genericArgs
     * @return
     */
    private boolean matchByName( Constructor<?> constructor, List<ValueHolder> genericArgs) {
        Map<String, Class<?>> paramNameTypeMap = getParamNameTypeMap(constructor);
        for(ValueHolder valueHolder : genericArgs){
            String name = valueHolder.getName();
            Object value = valueHolder.getValue();
            Assert.hasText(name, "constructor args 'name' should not be null!");
            Class<?> clazz = paramNameTypeMap.get(name);
            if(clazz == null){
                return false;
            }
            if(value instanceof BeanReference){
                Object convertedValue = getBeanReference((BeanReference) value);
                if(!clazz.isAssignableFrom(convertedValue.getClass())){
                    return false;
                }
            }else if(!BeanUtils.isSimpleProperty(value.getClass()) ){
                return false;
            }
        }
        return true;
    }

    private Object[] buildConstructorArgs(Constructor<?> constructor, ConstructorArgumentValues constructorArgumentValues) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        int argsCount = mbd.getConstructorArgumentValues().getArgumentCount();
        Assert.isTrue(paramTypes.length ==  argsCount);
        Object[] constructorArgs = new Object[argsCount];

        Map<Integer, ValueHolder> indexedArgs = constructorArgumentValues.getIndexedArgumentValues();
        List<ValueHolder> genericArgs = constructorArgumentValues.getGenericArgumentValues();

        int autowireBy = constructorArgumentValues.getAutowrieBy();
        //by index
        if( autowireBy == ConstructorArgumentValues.BY_INDEX ){
            for(Map.Entry<Integer, ValueHolder> entry : indexedArgs.entrySet()){
                int index = entry.getKey();
                ValueHolder valueHolder = entry.getValue();
                Object value = valueHolder.getValue();
                constructorArgs[index] = convertValue(value, paramTypes[index]);
            }
            return constructorArgs;
        //by name
        } else if( autowireBy == ConstructorArgumentValues.BY_NAME ){
            List<String> paramNames = getParamNames(constructor);
            Assert.isTrue(paramNames.size() == argsCount);
            for(int i =0; i < argsCount; i++){
                String paramName =  paramNames.get(i);
                Class<?> paramType = paramTypes[i];
                constructorArgs[i] = getValueByParamName(genericArgs, paramName, paramType);
            }
            return constructorArgs;
        //by type
        } else {
            return null;
        }

    }

    private Object getValueByParamName(List<ValueHolder> genericArgs, String paramName, Class<?> paramType){
        for(ValueHolder valueHolder : genericArgs){
            String name = valueHolder.getName();
            Object value = valueHolder.getValue();
            if (paramName.equals(name)){
                return convertValue(value, paramType);
            }
        }
        //never happen
        throw new BeanCreationException(mbd.getResourceDescription(), beanName +" Can not find constructor param '"+paramName+"'.");
    }

    private Object convertValue(Object value, Class<?> paramType){
        //是ref
        if(value instanceof BeanReference){
            Object convertedValue = getBeanReference((BeanReference) value);
            return convertedValue;
            //是普通value
        }else {
            try {
                return BeanUtils.convertByType(value, paramType);
            } catch (Exception e) {
                throw new BeanCreationException(mbd.getResourceDescription(), mbd.getBeanName(),
                        " convert constructor-arg value '"+value+"' fail.",e);
            }
        }
    }

    /**
     * Get constructor parameter names and parameter types
     * @param constructor
     * @return
     */
    private Map<String, Class<?>> getParamNameTypeMap(Constructor<?> constructor){
        Class<?>[] paramTypes = constructor.getParameterTypes();
        List<String> paramNames = getParamNames(constructor);
        Map<String, Class<?>> paramNameTypeMap = new HashMap<String, Class<?>>(paramNames.size());
        for(int i=0; i< paramNames.size(); i++){
            paramNameTypeMap.put(paramNames.get(i), paramTypes[i]);
        }
        return paramNameTypeMap;

    }

    private List<String> getParamNames(Constructor<?> constructor){
        Class<?>[] paramTypes = constructor.getParameterTypes();
        List<String> paramNames = null;
        try {
            paramNames = ByteCodeUtils.getParameterNames(constructor);
            if(CollectionUtils.isEmpty(paramNames) || paramNames.size() != paramTypes.length){
                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "Can not get parameter names of constructor '"+constructor.getName()+"'.");
            }
            return paramNames;
        } catch (Exception e) {
            if(e instanceof BeanCreationException){
                throw (BeanCreationException)e;
            }
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Get parameter names of constructor '"+constructor.getName()+"' failed." , e );
        }
    }


    private Object getBeanReference(BeanReference beanReference){
        Object refBean = this.beanFactory.getBean(beanReference.getName());
        if(refBean == null){
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Autowire by constructor fail: Bean reference '"+beanReference.getName()+"' not found.");
        }
        return refBean;
    }

}
