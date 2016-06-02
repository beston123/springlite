package org.springlite.beans.exception;

@SuppressWarnings("serial")
public class BeansException extends RuntimeException {

    protected String beanName;

    protected Class beanClass;

    public BeansException(String msg) {
        super(msg);
    }

    public BeansException(String beanName, String msg) {
        this("Bean named '" + beanName + "' error: " + msg);
        this.beanName = beanName;
    }

    public BeansException(String beanName, String msg, Throwable cause) {
        this("Bean named '" + beanName + "' error: " + msg, cause);
        this.beanName = beanName;
    }

    public BeansException(Class beanClass, String msg) {
        this("Bean type '" + beanClass.getName() + "' error: " + msg);
        this.beanClass = beanClass;
    }

    public BeansException(Class beanClass, String msg, Throwable cause) {
        this("Bean type '" + beanClass.getName() + "' error: " + msg, cause);
        this.beanClass = beanClass;
    }

    public BeansException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public String getBeanName() {
        return this.beanName;
    }

    /**
     * Retrieve the innermost cause of this exception, if any.
     * @return the innermost exception, or {@code null} if none
     * @since 2.0
     */
    public Throwable getRootCause() {
        Throwable rootCause = null;
        Throwable cause = getCause();
        while (cause != null && cause != rootCause) {
            rootCause = cause;
            cause = cause.getCause();
        }
        return rootCause;
    }

    /**
     * Check whether this exception contains an exception of the given type:
     * either it is of the given class itself or it contains a nested cause
     * of the given type.
     * @param exType the exception type to look for
     * @return whether there is a nested exception of the specified type
     */
    public boolean contains(Class exType) {
        if (exType == null) {
            return false;
        }
        if (exType.isInstance(this)) {
            return true;
        }
        Throwable cause = getCause();
        if (cause == this) {
            return false;
        }
        if (cause instanceof BeansException) {
            return ((BeansException) cause).contains(exType);
        }
        else {
            while (cause != null) {
                if (exType.isInstance(cause)) {
                    return true;
                }
                if (cause.getCause() == cause) {
                    break;
                }
                cause = cause.getCause();
            }
            return false;
        }
    }


}
