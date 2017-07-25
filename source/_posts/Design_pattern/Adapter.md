---
title: 适配器模式
date: 2017-07-25 14:32:38
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式, 适配器模式
---

## 适配器模式

### 介绍

适配器模式讲一个类的接口转换为被期望用到的另一个接口,使得原本由于接口不兼容而不能一起工作的那些类可以在一起工作.(结构型模式)

### 适配器模式中的角色

一般适配器模式中有以下三个角色:

- 目标接口(Target) : 客户期待的接口.
- 需要适配的类(Adaptee) : 需要适配的类.
- 适配器(Adapter) : 包装一个需要适配的对象,吧原接口转换为目标接口.

### 优点 

- 通过适配器,客户端调用同一接口.
- 复用的现存的类
- 将目标类和适配者类解耦

### 缺点
过多的使用适配器，会让系统非常零乱，不易整体进行把握.

### DEMO
目标接口
```
package factory.pattern.Adapter;

/**
 * Created by fk5431 on 7/25/17.
 */
public class Target {
    public void Request(){
        System.out.println("普通请求");
    }
}
```
需要适配的类
```
package factory.pattern.Adapter;

/**
 * Created by fk5431 on 7/25/17.
 */
public class Adaptee {
    public void SpecificRequest(){
        System.out.println("特殊请求");
    }
}
```
适配器
```
package factory.pattern.Adapter;

/**
 * Created by fk5431 on 7/25/17.
 */
public class Adapter extends Target {
    private Adaptee adaptee = new Adaptee();

    @Override
    public void Request() {
        adaptee.SpecificRequest();
    }
}
```
测试
```
package factory.pattern.Adapter;
   
   /**
    * Created by fk5431 on 7/25/17.
    */
   public class Test {
       public static void main(String[] args) {
           Target target = new Adapter();
           target.Request();
       }
   }
```