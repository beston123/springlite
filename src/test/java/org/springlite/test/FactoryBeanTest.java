package org.springlite.test;

import org.junit.Test;
import org.springlite.bean.DbConnection;
import org.springlite.context.ApplicationContext;
import org.springlite.context.support.ClassPathXmlApplicationContext;
import org.springlite.service.HelloWorldService;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/29
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class FactoryBeanTest {

    //factoryBean
    @Test
    public void testFactoryBean() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("factorybean.xml");
        DbConnection dbConnection = (DbConnection) applicationContext.getBean("dbConnection");
        dbConnection.connect();
    }
}
