package org.springlite.service;

import org.springlite.beans.InitializingBean;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/29
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface Printer extends InitializingBean{

    void println(String text);

    boolean isReady();
}
