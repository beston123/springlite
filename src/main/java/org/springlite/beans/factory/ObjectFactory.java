package org.springlite.beans.factory;

import org.springlite.beans.exception.BeanCreationException;
import org.springlite.beans.exception.BeansException;

/**
 * Created by swy on 2016/5/26.
 */
public interface ObjectFactory<T> {

    T getObject() throws BeansException;
}
