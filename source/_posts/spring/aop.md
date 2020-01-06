---
title: Spring——AOP
date: 2020-01-06 21:20
comments: false
tags: 
- java
- spring
- aop
categories: 
- spring
keywords: 
- java
- spring
- aop
---

### Spring——AOP

#### AOP

AOP(Aspect Oriented Programming) 面向切面编程，作为面向对象的一种补充，用于处理系统中分布于各个模块的横切关注点，如事务管理、日志、缓存等等。AOP实现的关键在于AOP框架自动创建的AOP代理，AOP代理主要分为静态代理和动态代理。

##### 静态代理

###### AspectJ静态代理

AspectJ 是一个java实现的面向切面的框架，它扩展了java语言，有定义的语法，因此它有一个专门的编译器来生成遵守java字节编码规范的Class文件。

使用AspectJ可以引入依赖：
```
<dependency>
  <groupId>org.aspectj</groupId>
  <artifactId>aspectjrt</artifactId>
  <version>1.8.9</version>
</dependency>
<dependency>
  <groupId>org.aspectj</groupId>
  <artifactId>aspectjtools</artifactId>
  <version>1.8.9</version>
</dependency>
```

编写一个普通类

```
public class Hello {
    public void sayHello() {
        System.out.println("hello");
    }

    public static void main(String[] args) {
        Hello h = new Hello();
        h.sayHello();
    }
}
```

使用AspectJ编写Aspect

```
public aspect TxAspect {
    void around():call(void Hello.sayHello()){
        System.out.println("start");
        proceed();
        System.out.println("");
    }
}
```

> AspectJ编译使用 ajc

上面代码会输出：

```
start
hello
end
```

可以看出AOP已经生效，在没有修改Hello类的情况下就为Hello类增加了新功能。

编译后的Hello类如下：

```
public class Hello {
    public Hello() {
    }

    public void sayHello() {
        System.out.println("hello");
    }

    public static void main(String[] args) {
        Hello h = new Hello();
        sayHello_aroundBody1$advice(h, TxAspect.aspectOf(), (AroundClosure)null);
    }
}
```

AspectJ的静态代理会在编译阶段将Aspect写入Java字节码中，运行的时候就是经过增强的AOP对象。

###### JDK静态代理

JDK的静态代理更多的是一种设计模式，JDK静态代理的代理类和委托类会实现同一接口或继承自相同的类。

具体例子可以参看：[代理模式](http://fk5431.com/20170803/Design_pattern/15_proxy/)

##### 动态代理

动态代理是在程序运行期间由JVM根据反射等机制动态生成的

###### JDK动态代理

JDK动态代理通过反射来接收被代理的类，并且要求被代理的类必须实现一个接口。JDK动态代理的核心是InvocationHandler接口和Proxy类。

具体例子可以参看：[代理模式](http://fk5431.com/20170803/Design_pattern/15_proxy/)

###### CGLIB动态代理

JDK动态代理要求某个类必须有实现的接口，而生成的代理类也智能代理某个类接口定义的方法。如果某个类没有实现接口，就不能使用JDK产生动态代理了。

cglib（Code Generation Library）是一个强大、高性能、高质量的Code生成类库，可以在运行期扩展Java类和实现Java接口。

cglib创建某个类的动态代理类的模式是：
1. 查找类上所有非final的public类型的方法定义。
2. 将这些方法的定义转换成字节码。
3. 将组成的字节码转换成相应的代理的class对象。
4. 实现MethodInterceptor接口，用来处理对代理类上所有方法的请求（和JDK动态代理的InvocationHandler的功能一样）。

定义一个简单的类：

```
public class RealImage  {
    private String fileName;

    public RealImage() {
    }

    public void dispalay() {
        System.out.println("Displaying ");
    }
}
```

实现MethodInterceptor：

```
public class MethodInterceptorImpl implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("proxy before");
        Object invoke = methodProxy.invokeSuper(o, objects);
        System.out.println("proxy after");
        return invoke;
    }
}
```
代理类测试：

```
public class CglibProxyDemo {

    public static void main(String[] args) {
        RealImage image = new RealImage();
        MethodInterceptor interceptor = new MethodInterceptorImpl();

        //cglib 中加强器，用来创建动态代理
        Enhancer enhancer = new Enhancer();
        //设置要创建动态代理的类
        enhancer.setSuperclass(image.getClass());
        // 设置回调，这里相当于是对于代理类上所有方法的调用，都会调用CallBack，而Callback则需要实行intercept()方法进行拦截
        enhancer.setCallback(interceptor);
        RealImage proxyImage = (RealImage) enhancer.create();
        proxyImage.dispalay();
    }
}
```

输出

```
proxy before
Displaying 
proxy after
```

通过以上实例可以看出，Cglib通过继承实现动态代理，具体类不需要实现特定的接口，而且代理类可以调用具体类的非接口方法，更加灵活。

#### Spring AOP

Spring AOP采用的是动态代理，在运行期间对业务方法进行增强，不会生成新类，Spring的动态代理提供了对JDK动态代理和cglib的支持：

1. 如果目标对象实现了接口，默认情况下会采用JDK的动态代理实现AOP。
2. 如果目标对象实现了接口，可以强制使用cglib实现AOP。
3. 如果目标对象没有实现了接口，必须采用cglib。

> Spring AOP 基于注解配置的情况下，需要依赖于AspectJ包的标准注解，但不需要额外的编译以及AspectJ的织入器，而基于XMl配置不需要，所以Spring AOP只是复用了AspectJ的注解，并没有其他依赖AspectJ的地方。

强制使用cglib 可以在配置文件中配置：
```
<aop:aspectj-autoproxy proxy-target-class="true"/>
```

