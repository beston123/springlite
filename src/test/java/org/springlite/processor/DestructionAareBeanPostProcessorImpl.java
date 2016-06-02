package org.springlite.processor;

import org.springlite.beans.exception.BeansException;
import org.springlite.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/31
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class DestructionAareBeanPostProcessorImpl implements DestructionAwareBeanPostProcessor {
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        System.out.println("等待500ms再销毁我。。。");
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
