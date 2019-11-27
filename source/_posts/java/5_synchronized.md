---
title: synchronized 
date: 2019-11-27 22:22:13
comments: false
tags: 
- synchronized
categories: 
- synchronized
keywords: synchronized,锁
---

synchronized 可以使用任意一个非空的对象当做锁。synchronized属于独占式的悲观锁，也是可重入锁。它可以保证同一时刻只有一个线程执行某个方法或某个代码块，还可保证共享变量的可见性。

### synchronized的三种作用范围

- 作用于方法，锁住的是对象实例。
- 作用于代码块，指定加锁对象。
- 作用于静态方法，锁住的是class实例，相当于类的全局锁。

### java对象头和Monitor（来自深入理解java虚拟机）

在HotSpot虚拟机中，对象在内存中存储的布局可以分为3块区域：对象头（Header）、实例数据（Instance Data）和对齐填充（Padding）。

- 对象头：对象头主要包括两部分信息，第一部分用于存储对象自身的的运行时数据，如哈希码（HashCode）、GC分代年龄、锁状态表示、线程持有的锁、偏向线程ID、偏向时间戳等，这部分数据长度在32为和64为虚拟机中分别为32bit和64bit，简称Mark Word。对象头的另一部分是类型指针，即对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例。如果对象是一个数组，在对象头还必须有一块用于记录数组长度的数据（1字节），这样虚拟机可以通过普通java对象的元数据信息确定java对象的大小。
    > 由于对象运行时的数据很多，已经超过了32/64位bit能记录的限度，但是对象头信息是与对象自身定义的数据无关的额外存储，考虑到虚拟机的空间效率，Mark Word被设计成一个非固定的数据结构以便在极小的空间存储尽量多的数据。
- 实例数据：这是对象真正存储的有效信息，就是代码中定义的各种字段内容（包括从父类继承的）。
- 对齐填充：这部分不是必然存在的，仅仅是占位符的作用，JVM 要求对象起始地址必须是8字节的整数倍。（对象大小必须是8字节的整数倍）

Mark Word 默认存储

| 锁状态 | 25bit | 4bit | 1bit是否是偏向锁 | 2bit锁标志位 |
| ----- | ----- | ---- | --------------- | ----------- |
| 无锁状态 | 对象HashCode | 对象分代年龄 | 0 | 01         |

可能变化的结构：

<table>
    <tr>
        <td rowspan="2">锁状态</td>
        <td colspan="2">25bit</td>
        <td rowspan="2">4bit</td>
        <td>1bit</td>
        <td>2bit</td>
    </tr>
	<tr>
		<td>23bit</td>
		<td>2bit</td>
		<td>是否是偏向锁</td>
		<td>锁标志位</td>
	</tr>
	<tr>
	    <td>轻量级锁</td>
	    <td rowspan="4">指向栈中锁记录的指针</td>
	    <td>00</td>
	</tr>
	<tr>
	    <td>重量级锁</td>
	    <td rowspan="4">指向重量级锁的指针</td>
	    <td>10</td>
	</tr>
	<tr>
	    <td>GC标记</td>
	    <td rowspan="4">空，不需要记录信息</td>
	    <td>11</td>
	</tr>
	<tr>
	    <td>偏向锁</td>
	    <td>偏向线程ID</td>
	    <td>偏向时间戳</td>
	    <td>对象分代年龄</td>
	    <td>1</td>
	    <td>01</td>
	</tr>
</table>

在锁标志位为10时，也就是代表重量级锁（synchronized），其中指针指向的是Monitor对象的起始地址。每个对象都与一个Monitor关联，对象和其Monitor之间有多种实现的方式（Monitor可以和对象一起创建销毁或获取锁对象的时候生成），但是当一个Monitor被某个线程持有便处于锁定状态。

> Monitor是由C++实现的，核心组件如下 ：
> - WaitSet：调用wait的方法被阻塞的线程被放在这；
> - ContentionList：竞争队列，所有请求锁的线程被先放在这个队列；
> - EntryList：Contention中资格成功候选资源的线程被放入这；
> - OnDeck：任意时刻，只有一个线程正在竞争锁，该线程就是OnDeck；
> - Owner：当前获取到锁资源的线程被称为Owner；
> - !Owner：当前释放锁的线程；

https://blog.csdn.net/javazejian/article/details/72828483

