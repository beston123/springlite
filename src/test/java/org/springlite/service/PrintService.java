package org.springlite.service;


import org.springlite.beans.InitializingBean;

public interface PrintService extends InitializingBean {

    void print(String text);
}
