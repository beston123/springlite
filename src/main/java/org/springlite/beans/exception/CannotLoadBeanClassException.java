package org.springlite.beans.exception;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class CannotLoadBeanClassException extends BeansException {

    private String beanClassName;

    public CannotLoadBeanClassException(String beanName, String beanClassName, ClassNotFoundException cause) {
        super("Cannot find class [" + beanClassName + "] for bean with name \'" + beanName , cause);
        this.beanName = beanName;
        this.beanClassName = beanClassName;
    }

    public CannotLoadBeanClassException(String beanName, String beanClassName, LinkageError cause) {
        super("Error loading class [" + beanClassName + "] for bean with name \'" + beanName +  ": problem with class file or dependent class", cause);
        this.beanName = beanName;
        this.beanClassName = beanClassName;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public String getBeanClassName() {
        return this.beanClassName;
    }
}
