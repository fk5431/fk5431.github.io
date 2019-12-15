---
title: 代理模式
date: 2017-08-03 22:15:23
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式 代理模式
---

## 代理模式

### 介绍
 
代理模式为其他对象提供一种代理以控制对这个对象的访问。（结构型模式）
代理模式提供三种角色：
- 抽象角色：声明真实对象和代理对象的共同接口。
- 代理角色：代理对象角色内部含有对真实对象的引用，从而可以操作真实对象，同时代理对象提供与真实对象相同的接口以便在任何时刻都能代替真实对象。同时，代理对象可以在执行真实对象操作时，附加其他的操作，相当于对真实对象进行封装。
- 真实角色：代理角色所代表的真实对象，是我们最终要引用的对象。
代理模式对外部提供统一的接口方法，而代理类在接口中实现对真实类的附加操作行为，从而可以在不影响外部调用情况下，进行系统扩展。

### 分类

#### 静态代理

静态代理就是我们自己静态定义的代理类，例如我们要进行图片加载的时候，可以通过一个代理类减少图片对象加载的内存占用：

#### DEMO

图片接口
```
package factory.pattern.proxy.demo1;

/**
 * Created by FK on 2017/8/3.
 */
public interface Image {
    void dispalay();
}
```
接口实体类

```
package factory.pattern.proxy.demo1;

/**
 * Created by FK on 2017/8/3.
 */
public class RealImage implements Image {
    private String fileName;

    public RealImage(String fileName) {
        this.fileName = fileName;
        loadFormDisk(fileName);
    }

    private void loadFormDisk(String fileName) {
        System.out.println("Loading " + fileName);
    }

    @Override
    public void dispalay() {
        System.out.println("Displaying " + fileName);
    }
}
```

```
package factory.pattern.proxy.demo1;

/**
 * Created by FK on 2017/8/3.
 */
public class ProxyImage implements Image {
    private RealImage realImage;
    private String fileNmme;

    public ProxyImage(String fileNmme) {
        this.fileNmme = fileNmme;
    }

    @Override
    public void dispalay() {
        if( realImage == null){
            realImage = new RealImage(fileNmme);
        }
        realImage.dispalay();
    }
}
```

测试
```
package factory.pattern.proxy.demo1;

/**
 * Created by FK on 2017/8/3.
 */
public class Test {
    public static void main(String[] args) {
        Image image = new ProxyImage("test.jpg");
        image.dispalay();
        System.out.println();
        image.dispalay();
    }
}
```

#### 动态代理

动态代理不需要实现接口，是利用JDK的API来实现代理对象的生成，所以动态代理也是JDK代理。

> JDK中生成代理对象的API
>  代理类所在包:java.lang.reflect.Proxy
>  JDK实现代理只需要使用newProxyInstance方法,但是该方法需要接收三个参数,完整的写法是:
> ```
> static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces,InvocationHandler h )
> ```
> 注意该方法是在Proxy类中是静态方法,且接收的三个参数依次为:
> 
> - ClassLoader loader,:指定当前目标对象使用类加载器,获取加载器的方法是固定的
> - Class<?>[] interfaces,:目标对象实现的接口的类型,使用泛型方式确认类型
> - InvocationHandler h:事件处理,执行目标对象的方法时,会触发事件处理器的方法,会把当前执行目标对象的方法作为参数传入

#### DEMO 

```
package factory.pattern.proxy.demo2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by FK on 2017/8/4.
 */
public class ProxyFactory {
    private Object target;

    public ProxyFactory(Object target) {
        this.target = target;
    }

    public Object getProxyInstance(){
        return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object returnValue = method.invoke(target, args);
                        return returnValue;
                    }
                });
    }
}
```

测试

```
package factory.pattern.proxy.demo2;

import factory.pattern.proxy.demo1.Image;
import factory.pattern.proxy.demo1.RealImage;

/**
 * Created by FK on 2017/8/4.
 */
public class Test {
    public static void main(String[] args) {
        Image image = new RealImage("test1.jpg");
        System.out.println(image.getClass());
        Image proxy = (Image) new ProxyFactory(image).getProxyInstance();
        System.out.println(proxy.getClass());
        proxy.dispalay();
    }
}
```

### 应用场合

#### 远程代理

就是为一个对象在不同的地址空间提供局部代表，这样可以隐藏一个对象存在不同地址空间的事实。

#### 虚拟代理

根据需要创建开销很大的对象。通过它来存放实例化需要很长时间的真实对象。

> html网页加载的时候图片是下载之后才能看到，未打开的图片框只存放了真实图片的路径和尺寸。

#### 安全代理

安全代理用来控制真实对象访问时的权限。

#### 智能指引

当调用真实的对象时，代理处理另外一些事。

> 计算真是对象的引用次数，当该对象没有引用时，可以自动释放它；或当第一次引用一个持久化对象，将它装载如内存。