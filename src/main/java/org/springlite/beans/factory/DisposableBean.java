package org.springlite.beans.factory;

/**
 * Created by swy on 2016/5/26.
 */
public interface DisposableBean {

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     * @throws Exception in case of shutdown errors.
     * Exceptions will get logged but not rethrown to allow
     * other beans to release their resources too.
     */
    void destroy() throws Exception;

}