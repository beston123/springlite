package org.springlite.bean;

import java.math.BigDecimal;
import java.net.URL;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/6/1
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class Person {

    private String name;

    private People mother;

    private int age;

    private BigDecimal weight;

    private URL url;

    public Person(People mother, String name, int age, BigDecimal weight, URL url) {
        this.mother = mother;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public People getMother() {
        return mother;
    }

    public void setMother(People mother) {
        this.mother = mother;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", mother=" + mother +
                ", age=" + age +
                ", weight=" + weight +
                ", url=" + url +
                '}';
    }
}
