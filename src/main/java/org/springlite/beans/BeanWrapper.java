package org.springlite.beans;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface BeanWrapper {

    /**
     * Return the bean instance wrapped by this object, if any.
     * @return the bean instance, or {@code null} if none set
     */
    Object getWrappedInstance();

    /**
     * Return the type of the wrapped JavaBean object.
     * @return the type of the wrapped bean instance,
     * or {@code null} if no wrapped object has been set
     */
    Class<?> getWrappedClass();

}
