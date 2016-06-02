package org.springlite.beans.factory.support;

import org.springlite.beans.BeanReference;
import org.springlite.beans.BeanUtils;
import org.springlite.beans.PropertyValue;
import org.springlite.beans.exception.BeanCreationException;
import org.springlite.beans.factory.BeanFactory;
import org.springlite.beans.factory.config.BeanDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/31
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class PropertyResolver {

    private BeanFactory beanFactory;

    private String beanName;

    private RootBeanDefinition mbd;

    public PropertyResolver(BeanFactory beanFactory, String beanName, RootBeanDefinition mbd){
        this.beanFactory = beanFactory;
        this.beanName = beanName;
        this.mbd = mbd;
    }


    public void autowireProperty(Object bean, PropertyValue property){
        //解决bean reference
        Object value = property.getValue();
        Object convertedValue;
        if (value instanceof BeanReference) {
            convertedValue = getBeanReference((BeanReference) value, property);
        }else if(BeanUtils.isSimpleProperty(value.getClass())){
            convertedValue = value;
        }else{
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Apply bean property '" + property.getName()+"', value '"+property.getValue()
                            + "' fail, only support a simple class type or a bean reference.");
        }

        //set by setter method
        Method setterMethod = findSetter(bean, property);
        if(setterMethod != null){

        //set by field
        }else{
            setByField(bean, property, convertedValue);
        }
    }

    /**
     * 查找setter方法
     * @param bean
     * @param property
     * @return
     */
    private Method findSetter(Object bean, PropertyValue property){
        try {
            Method declaredMethod  = bean.getClass().getDeclaredMethod(
                    "set" + property.getName().substring(0, 1).toUpperCase() + property.getName().substring(1),
                    property.getValue().getClass());
            return declaredMethod;
        } catch (NoSuchMethodException e) {
            //ignore
            //throw e;
        }
        return null;
    }

    /**
     * 使用setter设值
     * @param bean
     * @param property
     * @param value
     * @param setterMethod
     */
    private void setBySetter(Object bean, PropertyValue property, Object value, Method setterMethod){
        try {
            Class<?> paramClazz = setterMethod.getParameterTypes()[0].getClass();
            if(paramClazz.isAssignableFrom(value.getClass())){
                setterMethod.setAccessible(true);
                setterMethod.invoke(bean, value);
            }else if(BeanUtils.isSimpleProperty(paramClazz)){
                Object convertedValue = BeanUtils.convertByType(value, paramClazz);
                setterMethod.setAccessible(true);
                setterMethod.invoke(bean, convertedValue);
            }else{
                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "Apply bean property '" + property.getName()+"', value '"+property.getValue()
                                + "' fail, only support a simple class type or a bean reference.");
            }
        } catch (IllegalAccessException e) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Cannot apply property '" + property.getName()+"', value '"+property.getValue()+"'", e);
        } catch (InvocationTargetException e) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Cannot apply property '" + property.getName()+"', value '"+property.getValue()+"'", e);
        } catch (Exception e) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "UnknownException, Cannot apply property '" + property.getName()+"', value '"+property.getValue() +"'", e);
        }
    }

    /**
     * 字段设值
     * @param bean
     * @param property
     * @param value
     */
    private void setByField(Object bean, PropertyValue property, Object value){
        try {
            Field declaredField = bean.getClass().getDeclaredField(property.getName());
            Class<?> paramClazz = declaredField.getType();
            if(paramClazz.isAssignableFrom(value.getClass())){
                declaredField.setAccessible(true);
                declaredField.set(bean, value);
            }else if(BeanUtils.isSimpleProperty(paramClazz)){
                Object convertedValue = BeanUtils.convertByType(value, paramClazz);
                declaredField.setAccessible(true);
                declaredField.set(bean, convertedValue);
            }else{
                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "Apply bean property '" + property.getName()+"', value '"+property.getValue()
                                + "' fail, only support a simple class type or a bean reference.");
            }
        } catch (NoSuchFieldException e) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Cannot apply property '" + property.getName()+"', value '"+property.getValue()+
                            "', because bean has no field '" + property.getName()+"'", e);
        } catch (Exception e) {
            if(e instanceof BeanCreationException){
                throw (BeanCreationException)e;
            }
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    " Cannot apply property '" + property.getName()+"', value '"+property.getValue() + "'", e);
        }
    }

    private Object getBeanReference(BeanReference beanReference, PropertyValue property){
        Object refBean = this.beanFactory.getBean(beanReference.getName());
        if(refBean == null){
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Apply bean property '" + property.getName()+"', value '"+property.getValue()
                            + "' fail, bean reference '"+beanReference.getName()+"' not found.");
        }
        return refBean;
    }

}
