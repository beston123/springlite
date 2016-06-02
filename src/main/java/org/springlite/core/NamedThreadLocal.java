package org.springlite.core;

import org.apache.commons.lang.Validate;

/**
 * <p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/26
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class NamedThreadLocal<T> extends ThreadLocal<T> {

    private final String name;


    /**
     * Create a new NamedThreadLocal with the given name.
     * @param name a descriptive name for this ThreadLocal
     */
    public NamedThreadLocal(String name) {
        Validate.notEmpty(name, "Name must not be empty");
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}