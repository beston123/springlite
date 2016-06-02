package org.springlite.bean;

import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/29
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class Student extends People{

    private String school;

    private List<String> friends;

    private Map score;

    private Properties basicInfo;

    private Set interest;

    private People mother;

    public Student (String name, int age, People mother){
        super(name, age);
        this.mother = mother;
    }

    public Student (String name, int age, People mother, String school){
        super(name, age);
        this.mother = mother;
        this.school = school;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public List getFriends() {
        return friends;
    }

    public void setFriends(List friends) {
        this.friends = friends;
    }

    public Map getScore() {
        return score;
    }

    public void setScore(Map score) {
        this.score = score;
    }

    public Properties getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(Properties basicInfo) {
        this.basicInfo = basicInfo;
    }

    public Set getInterest() {
        return interest;
    }

    public void setInterest(Set interest) {
        this.interest = interest;
    }

    public People getMother() {
        return mother;
    }

    public void setMother(People mother) {
        this.mother = mother;
    }


    @Override
    public String toString() {
        return "Student{" +
                "name='" + super.getName() +
                ", age=" + super.getAge() +
                (CollectionUtils.isNotEmpty(friends) ? " has "+friends.size()+ " friends" : "")+
                ", mother is " + mother +
                '}';
    }
}
