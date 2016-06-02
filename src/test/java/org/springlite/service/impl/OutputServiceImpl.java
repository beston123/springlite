package org.springlite.service.impl;

import org.springlite.service.OutputService;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/29
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class OutputServiceImpl implements OutputService {

    @Override
    public void output(String text) {
        System.out.println(text);
    }

    @Override
    public void destroy(){
        System.out.println("OutputService is destroyed.");
    }

}
