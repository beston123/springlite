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
@SuppressWarnings("serial")
public class BeanDefinitionStoreException extends RuntimeException {

    private String resourceDescription;

    private String beanName;


    /**
     * Create a new BeanDefinitionStoreException.
     * @param msg the detail message (used as exception message as-is)
     */
    public BeanDefinitionStoreException(String msg) {
        super(msg);
    }

    /**
     * Create a new BeanDefinitionStoreException.
     * @param msg the detail message (used as exception message as-is)
     * @param cause the root cause (may be {@code null})
     */
    public BeanDefinitionStoreException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new BeanDefinitionStoreException.
     * @param resourceDescription description of the resource that the bean definition came from
     * @param msg the detail message (used as exception message as-is)
     */
    public BeanDefinitionStoreException(String resourceDescription, String msg) {
        super(msg);
        this.resourceDescription = resourceDescription;
    }

    /**
     * Create a new BeanDefinitionStoreException.
     * @param resourceDescription description of the resource that the bean definition came from
     * @param msg the detail message (used as exception message as-is)
     * @param cause the root cause (may be {@code null})
     */
    public BeanDefinitionStoreException(String resourceDescription, String msg, Throwable cause) {
        super(msg, cause);
        this.resourceDescription = resourceDescription;
    }

    /**
     * Create a new BeanDefinitionStoreException.
     * @param resourceDescription description of the resource that the bean definition came from
     * @param beanName the name of the bean requested
     * @param msg the detail message (appended to an introductory message that indicates
     * the resource and the name of the bean)
     */
    public BeanDefinitionStoreException(String resourceDescription, String beanName, String msg) {
        this(resourceDescription, beanName, msg, null);
    }

    /**
     * Create a new BeanDefinitionStoreException.
     * @param resourceDescription description of the resource that the bean definition came from
     * @param beanName the name of the bean requested
     * @param msg the detail message (appended to an introductory message that indicates
     * the resource and the name of the bean)
     * @param cause the root cause (may be {@code null})
     */
    public BeanDefinitionStoreException(String resourceDescription, String beanName, String msg, Throwable cause) {
        super("Invalid bean definition with name '" + beanName + "' defined in " + resourceDescription + ": " + msg, cause);
        this.resourceDescription = resourceDescription;
        this.beanName = beanName;
    }


    /**
     * Return the description of the resource that the bean
     * definition came from, if any.
     */
    public String getResourceDescription() {
        return this.resourceDescription;
    }

    /**
     * Return the name of the bean requested, if any.
     */
    public String getBeanName() {
        return this.beanName;
    }

}

