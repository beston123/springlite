package org.springlite.beans.factory.config;

import org.springlite.beans.exception.BeansException;

/**
 * Created by swy on 2016/5/25.
 */
public interface BeanPostProcessor {


    Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;


    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;

}