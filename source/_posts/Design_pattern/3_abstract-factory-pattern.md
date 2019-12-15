---
title: 抽象工厂模式
date: 2017-06-18 14:39:16
comments: false
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式 抽象工厂模式
---
## 抽象工厂模式

### 介绍

为创建一组相关或相互依赖的对象提供一个接口，而且无需指定他们的具体类。（创建型模式）

### 和工厂模式区别

工厂模式提供一个产品的结构，而抽象工厂模式提供多个产品的结构，可以组成一个产品族。

> QQ换皮肤可以使用抽象工厂模式

**优点**：当一个产品族中的多个对象被设计成一起工作时，它能保证客户端始终只使用同一个产品族中的对象。
**缺点**：扩展非常麻烦，需要修改很多代码。

### DEMO

可以在前一个例子上给图形增加颜色，就相当于一个产品族了：

先创建一个图形和颜色的接口类：


```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public interface Color {
    void fill();
}
```

```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public interface Shape {
    void draw();
}
```

然后分别实现各自两个类进行测试：

```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public class Black implements Color {
    @Override
    public void fill() {
        System.out.println("Black : fill()");
    }
}
```

```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public class Red implements Color {
    @Override
    public void fill() {
        System.out.println("Red : fill()");
    }
}
```

```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public class Circle implements Shape {
    @Override
    public void draw() {
        System.out.println("Circle : draw()");
    }
}
```

```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public class Rectangle implements Shape {
    @Override
    public void draw() {
        System.out.println("Rectangle : draw()");
    }
}
```

然后实现工厂抽象类：

```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public abstract class AbstractFactory {
    abstract Color getColorFactory(String color);
    abstract Shape getShapeFactory(String shape);
}
```
然后扩展工厂抽象类：

```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public class ShapeFactory extends AbstractFactory {
    @Override
    Color getColorFactory(String color) {
        return null;
    }

    @Override
    Shape getShapeFactory(String shape) {
        if(shape == null) {
            return null;
        }
        if ("rectangle".equals(shape)){
            return new Rectangle();
        }else if ("circle".equals(shape)){
            return new Circle();
        }else {
            return null;
        }
    }
}
```

```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public class ColorFactory extends AbstractFactory {
    @Override
    Color getColorFactory(String color) {
        if(color == null) {
            return null;
        }
        if("red".equals(color)){
            return new Red();
        }else if("black".equals(color)){
            return new Black();
        }else {
            return null;
        }
    }

    @Override
    Shape getShapeFactory(String shape) {
        return null;
    }
}
```
然后写一个工厂创造器：
```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public class FactoryProducer {
    public static AbstractFactory getFactory(String choice){
        if(choice.equalsIgnoreCase("shape")){
            return new ShapeFactory();
        } else if(choice.equalsIgnoreCase("color")){
            return new ColorFactory();
        }
        return null;
    }
}
```
最后写一个测试类来试试输出：
```
package factory.pattern.abs;

/**
 * Created by FK on 2017/6/18.
 */
public class AbstractFactoryPattern {

    public static void main(String[] args) {
        AbstractFactory shapeFactory = FactoryProducer.getFactory("shape");
        Shape shape1 = shapeFactory.getShapeFactory("circle");
        shape1.draw();
        Shape shape2 = shapeFactory.getShapeFactory("rectangle");
        shape2.draw();
        AbstractFactory colorFactory = FactoryProducer.getFactory("color");
        Color color1 = colorFactory.getColorFactory("red");
        color1.fill();
        Color color2 = colorFactory.getColorFactory("black");
        color2.fill();
    }

}
```

输出如果如下:
```
Circle : draw()
Rectangle : draw()
Red : fill()
Black : fill()
```