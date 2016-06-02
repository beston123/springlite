package org.springlite.beans.factory.config;

import org.springlite.beans.exception.BeansException;
import org.springlite.beans.factory.config.BeanPostProcessor;

/**
 * <p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/27
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;

    boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;

//    PropertyValues postProcessPropertyValues(
//            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName)
//            throws BeansException;
}
