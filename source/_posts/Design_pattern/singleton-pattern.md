---
title: 单例模式
date: 2017-06-19 12:18:25
comments: false
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式, 单例模式
---

## 单例模式

### 定义
一个类有且仅有一个实例，并且自行实例化向整个系统提供。(创建型模式)
设计模式中比较简单的几种之一,单例模式就是让一个类在系统运行过程中只会产生唯一的一个实例,单例模式主要:

1. 单例类只能有一个实例。
2. 单例类必须自己创建自己的唯一实例。
3. 单例类必须给所有其他对象提供这一实例。

### 使用场景
- 需要频繁实例化然后销毁的对象。
- 创建对象时耗时过多或者耗资源过多，但又经常用到的对象。
- 有状态的工具类对象。
- 频繁访问数据库或文件的对象。

### 单例模式的实现

单例模式的实现一般根据实例化对象时的不同分为懒汉式和饿汉式.

#### 懒汉式典型实现

```
package factory.pattern.singleton;
/**
 * Created by fk5431 on 6/19/17.
 */
public class SingletonLazy {
    private static SingletonLazy singletonLazy;
    private SingletonLazy (){}
    public static SingletonLazy getInstance(){
        if( singletonLazy == null){
            singletonLazy = new SingletonLazy();
        }
        return singletonLazy;
    }
}
```
懒汉式的模式就是在需要进行实例化的时候在进行实例化,这种方式在多线程的时候是有问题的,会有线程安全问题.

#### 懒汉式线程安全形式

```
package factory.pattern.singleton;
/**
 * Created by fk5431 on 6/19/17.
 */
public class SingletonLazeSafe {
    private static SingletonLazeSafe singletonLazeSafe;
    private SingletonLazeSafe(){}
    public static synchronized SingletonLazeSafe getInstance(){
        if(singletonLazeSafe == null){
            singletonLazeSafe = new SingletonLazeSafe();
        }
        return singletonLazeSafe;
    }
}
```
懒汉式的线程安全模式在进入获取实例方法时候就会加synchronize,可以保证线程安全获取唯一的实例,但是如果该方法调用过多会有性能方面的影响.

#### 饿汉式

```
package factory.pattern.singleton;
/**
 * Created by fk5431 on 6/19/17.
 */
public class SingletonHungary {
    private static SingletonHungary singletonHungary = new SingletonHungary();
    private SingletonHungary(){}
    public static SingletonHungary getInstance(){
        return singletonHungary;
    }
}
```
饿汉式会在类装载时候就进行实例的初始化,虽然没有了synchronize的效率影响,但是在类加载的时候就进行了初始化一方面浪费了内存,也没有实现lazy loading加载的效果.

#### 双重校验锁
```
package factory.pattern.singleton;
/**
 * Created by fk5431 on 6/19/17.
 */
public class DoubleCheckedLocking {
    private volatile static DoubleCheckedLocking doubleCheckedLocking;
    private DoubleCheckedLocking(){}
    public DoubleCheckedLocking getInstance(){
        if(doubleCheckedLocking == null){
            synchronized (DoubleCheckedLocking.class){
                if(doubleCheckedLocking == null){
                    doubleCheckedLocking = new DoubleCheckedLocking();
                }
            }
        }
        return doubleCheckedLocking;
    }
}
```
这种方式采用了双锁机制,一方面保证了多线程的安全,另一方面还提高了效率.
(双重检查锁定的问题下次专门内个文章)

#### 静态内部类
```
package factory.pattern.singleton;
/**
 * Created by fk5431 on 6/19/17.
 */
public class SingletonStaticClass {
    //静态内部类
    private static class SingletonHodler{
        private static final SingletonStaticClass INSTANCE = new SingletonStaticClass();
    }
    private SingletonStaticClass(){}
    public static final SingletonStaticClass getInstance(){
        return SingletonHodler.INSTANCE;
    }
}
```
这种方式利用了 classloder 机制来保证初始化 instance 时只有一个线程,但是这个是类装载的时候不一定会进行初始化,只有在调用 getInstance 方法时候才会显示的装载SingletonHodler,然后实例化instance.

