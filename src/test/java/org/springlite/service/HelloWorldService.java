package org.springlite.service;

import org.springlite.beans.InitializingBean;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface HelloWorldService extends InitializingBean {

    void init();

    void say();

}
