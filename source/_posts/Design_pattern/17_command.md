---
title: 命令模式
date: 2017-08-08 14:54:23
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式 命令模式
---

## 命令模式

### 介绍

> 在软件系统中，行为请求者与行为实现者通常是一种紧耦合关系，但是有时候，比如需要对行为进行记录、撤销或重做、事务等处理时，这种无法抵御变化的紧耦合关系就不大合适。

将一个请求封装成一个对象，从而是你可用不同的请求对客户进行参数化，对请求排队和记录请求日志，以及支持可撤销的操作。
命令模式就是对命令进行封装，把发出命令和执行命令分开，请求方并不知道命令是怎么被接受，也不知道命令是否执行，什么时候执行，怎么被执行的；使用命令模式可以使请求成为一个对象，这个对象可以和其他对象一样被存储和传递。

## 涉及角色

- 抽象命令（command）：定义命令的接口，申明执行的方法。
- 具体命令（concreteCommand）：具体命令，实现要执行的方法，它通常是“虚”的表现；通常会有接受者，并调用接受者的功能来完成命令要执行的操作。
- 接受者（receiver）：真正执行命令的对象。任何类都可能成为一个接受者，只要实现命令要求实现的相应功能。
- 调用者（invoker）：要求命令对象执行请求，通常会持有命令对象，可以持有很多的命令对象。

## 优缺点

### 优点

- 解除了请求者与实现者之间的耦合，降低了系统的耦合度
- 对请求排队或记录请求日志，支持撤销操作
- 可以容易地设计一个组合命令
- 新命令可以容易地加入到系统中

### 缺点

- 因为针对每一个命令都需要设计一个具体命令类，使用命令模式可能会导致系统有过多的具体命令类

### 适用场景

- 当需要对行为进行“记录、撤销/重做”等处理时。
- 系统需要将请求者和接收者解耦，使得调用者和接收者不直接交互。
- 系统需要在不同时间指定请求、请求排队和执行请求。
- 系统需要将一组操作组合在一起，即支持宏命令。

## DEMO

command
```
package factory.pattern.command.demo1;

/**
 * Created by FK on 2017/8/8.
 */
public interface Order {
    void execute();
}
```
concreteCommand

```
package factory.pattern.command.demo1;

/**
 * Created by FK on 2017/8/8.
 */
public class BuyStock implements  Order{

    private Stock abcStock;

    public BuyStock(Stock abcStock) {
        this.abcStock = abcStock;
    }

    @Override
    public void execute() {
        abcStock.buy();
    }
}
```
```
package factory.pattern.command.demo1;

/**
 * Created by FK on 2017/8/8.
 */
public class SellStock implements Order {
    private Stock abcStock;

    public SellStock(Stock abcStock){
        this.abcStock = abcStock;
    }

    @Override
    public void execute() {
        abcStock.sell();
    }
}
```
receive
```
package factory.pattern.command.demo1;

/**
 * Created by FK on 2017/8/8.
 */
public class Stock {
    private String name = "ABC";
    private int quantity = 10;

    public void buy(){
        System.out.println("Stock [ Name: "+name+",Quantity: " + quantity +" ] bought");
    }

    public void sell(){
        System.out.println("Stock [ Name: "+name+",Quantity: " + quantity +" ] sold");
    }
}
```
invoker
```
package factory.pattern.command.demo1;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FK on 2017/8/8.
 */
public class Broker {
    private List<Order> orderList = new ArrayList<Order>();

    public void takeOrder(Order order){
        orderList.add(order);
    }

    public void placeOrders(){
        for (Order order : orderList) {
            order.execute();
        }
        orderList.clear();
    }
}
```
TEST
```
package factory.pattern.command.demo1;

/**
 * Created by FK on 2017/8/8.
 */
public class Test {
    public static void main(String[] args) {
        Stock abcStock = new Stock();

        BuyStock buyStock = new BuyStock(abcStock);
        SellStock sellStock = new SellStock(abcStock);

        Broker broker = new Broker();
        broker.takeOrder(buyStock);
        broker.takeOrder(sellStock);
        broker.placeOrders();
    }
}
```