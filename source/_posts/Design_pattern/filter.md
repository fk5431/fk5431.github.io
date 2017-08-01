---
title: 过滤器模式
date: 2017-07-27 09:28:21
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式, 过滤器模式
---

## 过滤器模式

### 介绍

过滤器模式允许开发者用不同的标准过滤一组对象。（结构型模式）
使用过滤器模式可以很方便的进行扩展，单独一个过滤器的执行是独立的，不依赖其他过滤器。

## DEMO

例如，一个Person类有姓名、性别、婚姻状况属性 ：
```
package factory.pattern.filter;

/**
 * Created by fk5431 on 7/27/17.
 */
public class Person {
    private String name;
    private String sex;
    private String marital;

    public Person(String name, String sex, String marital){
        this.name = name;
        this.sex = sex;
        this.marital = marital;
    }
    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public String getMarital() {
        return marital;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setMarital(String marital) {
        this.marital = marital;
    }

    @Override
    public String toString() {
        return "Persion  name : " + this.name + "  sex  " + this.sex + "  marital  "  + this.marital;
    }
}
```
建立一个过滤器标准
```
package factory.pattern.filter;

import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public interface Filter {
    List<Person> filter(List<Person> persions);
}
```
实现男性过滤和未婚过滤
```
package factory.pattern.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public class MaleFilter implements Filter {
    @Override
    public List<Person> filter(List<Person> persions) {
        List<Person> result = new ArrayList<Person>();
        for(Person p : persions){
            if ("MALE".equalsIgnoreCase(p.getSex())){
                result.add(p);
            }
        }
        return result;
    }
}

package factory.pattern.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public class SingleFilter implements Filter {
    @Override
    public List<Person> filter(List<Person> persions) {
        List<Person> result = new ArrayList<Person>();
        for(Person p : persions){
            if ("SINGLE".equalsIgnoreCase(p.getMarital())){
                result.add(p);
            }
        }
        return result;
    }
}
```
过滤器可以进行叠加和其他操作
```
package factory.pattern.filter;

import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public class FilterAnd implements Filter {
    private Filter filter;
    private Filter otherfilter;

    public FilterAnd(Filter filter, Filter otherfilter){
        this.filter = filter;
        this.otherfilter = otherfilter;
    }

    @Override
    public List<Person> filter(List<Person> persions) {
        List<Person> tmpList = filter.filter(persions);
        return otherfilter.filter(tmpList);
    }
}
package factory.pattern.filter;

import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public class FilterOr implements Filter {
    private Filter filter;
    private Filter otherfilter;

    public FilterOr(Filter filter, Filter otherfilter){
        this.filter = filter;
        this.otherfilter = otherfilter;
    }

    @Override
    public List<Person> filter(List<Person> persions) {
        List<Person> tmpList = filter.filter(persions);
        List<Person> tmpList2 = otherfilter.filter(persions);
        for(Person p : tmpList2){
            if(!tmpList.contains(p)){
                tmpList.add(p);
            }
        }
        return tmpList;
    }
}
```
最后进行测试
```
package factory.pattern.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public class Test {
    public static void main(String[] args) {
        List<Person> persons = new ArrayList<>();
        persons.add(new Person("霍一", "FEMALE", "MARRIED"));
        persons.add(new Person("邓二", "MALE", "MARRIED"));
        persons.add(new Person("张三", "MALE", "SINGLE"));
        persons.add(new Person("李四", "FEMALE", "MARRIED"));
        persons.add(new Person("王五", "MALE", "SINGLE"));
        persons.add(new Person("赵六", "FEMALE", "SINGLE"));
        persons.add(new Person("孙七", "MALE", "SINGLE"));
        persons.add(new Person("罗八", "MALE", "MARRIED"));
        persons.add(new Person("刘九", "FEMALE", "SINGLE"));
        persons.add(new Person("史十", "FEMALE", "SINGLE"));

        List<Person> malePerson = new MaleFilter().filter(persons);
        for(Person p : malePerson){
            System.out.println(p.toString());
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        List<Person> singlePerson = new MaleFilter().filter(persons);
        for(Person p : singlePerson){
            System.out.println(p.toString());
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        List<Person> singleAndMalePerson = new FilterAnd(new MaleFilter(), new SingleFilter()).filter(persons);
        for(Person p : singleAndMalePerson){
            System.out.println(p.toString());
        }
    }
}
```
输出结果如下：
```
Persion  name : 邓二  sex  MALE  marital  MARRIED
Persion  name : 张三  sex  MALE  marital  SINGLE
Persion  name : 王五  sex  MALE  marital  SINGLE
Persion  name : 孙七  sex  MALE  marital  SINGLE
Persion  name : 罗八  sex  MALE  marital  MARRIED

Persion  name : 邓二  sex  MALE  marital  MARRIED
Persion  name : 张三  sex  MALE  marital  SINGLE
Persion  name : 王五  sex  MALE  marital  SINGLE
Persion  name : 孙七  sex  MALE  marital  SINGLE
Persion  name : 罗八  sex  MALE  marital  MARRIED

Persion  name : 张三  sex  MALE  marital  SINGLE
Persion  name : 王五  sex  MALE  marital  SINGLE
Persion  name : 孙七  sex  MALE  marital  SINGLE
``` 