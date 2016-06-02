package org.springlite.test;

import junit.framework.Assert;
import org.junit.Test;
import org.springlite.bean.*;
import org.springlite.context.ApplicationContext;
import org.springlite.context.support.AbstractApplicationContext;
import org.springlite.context.support.ClassPathXmlApplicationContext;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/29
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class BeanPropertyConstructTest {

    //set property, construct args
    @Test
    public void testProperty() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean-property-construct.xml");
        Student student = (Student) applicationContext.getBean("student");
        Assert.assertNotNull(student.getMother());
        System.out.println(student);
    }

    @Test
    public void testConstructor() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("bean-construct-args.xml");
        Person p = applicationContext.getBean(Person.class);
        Assert.assertNotNull(p.getMother());
        System.out.println(p);
        People p1 = applicationContext.getBean(People.class);
        System.out.println(p1);

    }

    //property cyclic reference
    @Test
    public void testCyclicRefProperty() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("bean-cyclic-ref-property.xml");
        CyclicRefA a = ac.getBean(CyclicRefA.class);
        CyclicRefB b = ac.getBean(CyclicRefB.class);

        Assert.assertNotNull(a);
        Assert.assertNotNull(b);
        Assert.assertEquals(b, a.getCyclicRefB());
        Assert.assertEquals(a, b.getCyclicRefA());

    }

    //constructor cyclic reference
    @Test
    public void testCyclicRef(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("bean-cyclic-ref-constructor.xml");
        CyclicRefA a = ac.getBean(CyclicRefA.class);
        CyclicRefB b = ac.getBean(CyclicRefB.class);
    }


}
