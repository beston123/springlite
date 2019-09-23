package org.springlite.service.impl;

import org.springlite.service.HelloWorldService;
import org.springlite.service.OutputService;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    private String text;

    private OutputService outputService;

//    private HelloWorldServiceImpl(){}

    @Override
    public void init() {
        System.out.println("[HelloWorldService call 'init' method]");
    }

    @Override
    public void say(){
        outputService.output(text);
    }


    @Override
    public void afterPropertiesSet() {
        System.out.println("[HelloWorldService call 'afterPropertiesSet' method]");
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public OutputService getOutputService() {
        return outputService;
    }

    public void setOutputService(OutputService outputService) {
        this.outputService = outputService;
    }

}
