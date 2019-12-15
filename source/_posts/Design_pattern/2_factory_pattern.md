---
title: 设计模式——工厂模式
date: 2017-06-17 21:21:50
comments: false
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式 工厂模式
---

## 工厂模式

### 介绍

定义一个创建对象的接口，让其子类自己决定实例化哪一个工厂类，工厂模式使其创建过程延迟到子类进行。（创建型模式）

### 使用场景

例如orm框架，在选择数据库的时候只需要替换方言和驱动，不需要对已经实现的具体细节进行改动。

- 优点：使用工厂模式可以屏蔽具体实现，只需要关心接口的调用；在增加一个产品的时候，只需要在工厂类里扩展一个产品就可以。
- 缺点：每增加产品就必须增加具体实现类和实现工厂，增加了系统复杂性。（如果产品非常非常多，233333）

### DEMO

工厂模式比较简单，就写个最常用的例子：图形。

```
package factory.pattern;

/**
 * Created by FK on 2017/6/17.
 */
public interface Shape {
    void draw();
}

```
创建 shape抽象类

```
package factory.pattern;

/**
 * Created by FK on 2017/6/17.
 */
public class Rectangle implements Shape {
    @Override
    public void draw() {
        System.out.println("Rectangle : draw()");
    }
}
```
rectangle实现shape方法

```
package factory.pattern;

/**
 * Created by FK on 2017/6/17.
 */
public class Circle implements Shape {

    @Override
    public void draw() {
        System.out.println("Circle : draw()");
    }
}
```
circle实现shape方法

```
package factory.pattern;

/**
 * Created by FK on 2017/6/17.
 */
public class Square implements Shape {

    @Override
    public void draw() {
        System.out.println("Square : draw()");
    }
}

```
square实现shape方法

```
package factory.pattern;

/**
 * Created by FK on 2017/6/17.
 */
public class ShapeFactory {

    public Shape getShape(String shapeType){
        if(shapeType == null){
            return null;
        }
        if(shapeType.equalsIgnoreCase("CIRCLE")){
            return new Circle();
        } else if(shapeType.equalsIgnoreCase("RECTANGLE")){
            return new Rectangle();
        } else if(shapeType.equalsIgnoreCase("SQUARE")){
            return new Square();
        }
        return null;
    }

}
```
创建一个工厂，生成基于给传入值的实体类的对象。

```
package factory.pattern;

/**
 * Created by FK on 2017/6/17.
 */
public class FactoryPattern {
    public static void main(String[] args) {
        ShapeFactory shapeFactory = new ShapeFactory();
        Shape circle = shapeFactory.getShape("CIRCLE");
        //调用 Circle 的 draw 方法
        circle.draw();
        Shape rectangle = shapeFactory.getShape("RECTANGLE");
        //调用 Rectangle 的 draw 方法
        rectangle.draw();
        Shape square = shapeFactory.getShape("SQUARE");
        //调用 Square 的 draw 方法
        square.draw();
    }
}
```
写个demo来测试下输出是否实现了工厂模式，输出如下：
```
Circle : draw()
Rectangle : draw()
Square : draw()
```