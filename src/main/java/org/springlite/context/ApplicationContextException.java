package org.springlite.context;

import org.springlite.beans.exception.BeansException;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class ApplicationContextException  extends BeansException{

    /**
     * Create a new {@code ApplicationContextException}
     * with the specified detail message and no root cause.
     * @param msg the detail message
     */
    public ApplicationContextException(String msg) {
        super(msg);
    }

    /**
     * Create a new {@code ApplicationContextException}
     * with the specified detail message and the given root cause.
     * @param msg the detail message
     * @param cause the root cause
     */
    public ApplicationContextException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
