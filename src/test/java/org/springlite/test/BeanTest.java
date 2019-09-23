package org.springlite.test;

import junit.framework.Assert;
import org.junit.Test;
import org.springlite.context.ApplicationContext;
import org.springlite.context.support.ClassPathXmlApplicationContext;
import org.springlite.service.*;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class BeanTest {

    //loan beanDefinition, createBean, resource cyclic loading, etc.
    @Test
    public void testBasic() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean-basic.xml");
        HelloWorldService helloWorldService = (HelloWorldService) applicationContext.getBean("helloWorldService");
        helloWorldService.say();
    }


    //depends-On, lazy-init
    @Test
    public void testDependsOn() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean-depends-on_lazy-init.xml");
        PrintService printService = (PrintService) applicationContext.getBean("printService");
        printService.print("DKKKKKKK");
    }

    //scope
    @Test
    public void testScope() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean-scope.xml");

        //prototype
        PrinterDriver pd1 = (PrinterDriver) applicationContext.getBean("printerDriver");
        PrinterDriver pd2 = (PrinterDriver) applicationContext.getBean("printerDriver");
        Assert.assertEquals(false, pd1 == pd2);

        //singleton
        PrintService ps1 = (PrintService) applicationContext.getBean("printService");
        PrintService ps2 = (PrintService) applicationContext.getBean("printService");
        Assert.assertEquals(true, ps1 == ps2);

    }


    @Test
    public void testDestroyMethod(){
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean-basic-import.xml");
        OutputService os = applicationContext.getBean(OutputService.class);
        os.output("yes....");
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        applicationContext.close();
    }

    //depends-On, circular reference
    @Test
    public void testDependsOnCircular() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean-depends-circular.xml");
    }

}
