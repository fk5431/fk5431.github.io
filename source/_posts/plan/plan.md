---
title: 复习计划
date: 2020-02-19 15:05
comments: false
tags: 
- 复习计划
categories: 
- 复习计划
keywords:
- 复习计划
---
01 JAVA基础
1.1 java知识点

//Hashmap 源码级掌握，扩容，红黑树，最小树化容量，hash冲突解决，有些面试官会提出发自灵魂的审问，比如为什么是红黑树，别的树不可以吗；为什么8的时候树化，4不可以吗，等等
//concureentHashMap，段锁，如何分段，和hashmap在hash上的区别，性能，等等
//HashTable ，同步锁，这块可能会问你synchronized关键字 1.6之后提升了什么，怎么提升的这些
//ArrayList 优势，扩容，什么时候用
//LinkedList 优势，什么时候用，和arraylist的区别 等等
//基本类型和包装类型的区别，涉及自动装箱和拆箱，怎么做的，原理
//String ，StringBuffer，StringBuilder哪个是安全的
//static能不能修饰threadLocal，为什么，这道题我当时一听到其实挺懵逼的
//Comparable和Comparator接口是干什么的，其区别
//多态的原理是什么，感觉这个很容易被问到
//接口和抽象类，面试官问我是怎么理解的，我说接口对应功能，抽象类对应属性，然后面试官给我说了他的看法，说抽象类更偏向于一种模板~ 然后又交流了一下各自的想
//快速失败(fail-fast)和安全失败(fail-safe)的区别是什么
//synchronized 的实现原理以及锁优化？
//volatile 的实现原理？
//Java 的信号量？
//synchronized 在静态方法和普通方法的区别？
//怎么实现所有线程在等待某个事件的发生才会去执行？
//CAS？CAS 有什么缺陷，如何解决？
//synchronized 和 lock 有什么区别？
//Hashtable 是怎么加锁的 ？
List，Map，Set接口在取元素师，各有什么特点
如何线程安全的实现一个计数器
生产者消费者模式，要求手写过代码，还是要知道的
//单例模式，饿汉式，懒汉式，线程安全的做法，两次判断instance是否为空，每次判断的作用是什么。
//线程池，这个还是很重要的，在生产中用的挺多，四个线程池类型，其参数，参数的理解很重要，corepoolSize怎么设置，maxpoolsize怎么设置，keep-alive各种的，和美团面试官探讨过阻塞队列在生产中的设置，他说他一般设置为0，防止用户阻塞
//cyclicbarrier 和countdownlatch的区别，个人理解 赛马和点火箭
//线程回调，这块 被问过让我设计一个RPC，怎么实现，其实用到了回调这块的东西
sleep 和yeild方法有什么区别
//volatile关键字，可见性。
//乐观锁和悲观锁的使用场景
//悲观锁的常见实现方式：lock synchronized retreentlock
//乐观锁：CAS MVCC
//读写锁的实现方式，16位int的前八位和后八位分别作为读锁和写锁的标志位
死锁的条件，怎么解除死锁，怎么观测死锁。
希望大家能够好好看一下反射的原理，怎么确定类，怎么调方法
RPC框架，同步异步，响应时间，这些都被问到过，还让设计过
同步，异步，阻塞，非阻塞 在深信服的面试中遇到过，最好再找一些应用场景加以理解
1.2 JVM

内存模型以及分区，需要详细到每个区放什么。
堆里面的分区：Eden，survival （from+ to），老年代，各自的特点。
对象创建方法，对象的内存分配，对象的访问定位。
GC 的两种判定方法
GC 的三种收集方法：标记清除、标记整理、复制算法的原理与特点，分别用在什么地方，如果让你优化收集方法，有什么思路？
GC 收集器有哪些？CMS 收集器与 G1 收集器的特点
Minor GC 与 Full GC 分别在什么时候发生？
JVM 内存分哪几个区，每个区的作用是什么?
如和判断一个对象是否存活?(或者 GC 对象的判定方法)
java 中垃圾收集的方法有哪些?
类加载器双亲委派模型机制？
java 内存模型，java 类加载过程?
什么是类加载器，类加载器有哪些?
简述 java 内存分配与回收策率以及 Minor GC 和Major GC
02 数据库
2.1 MySQL

事务四大特性（ACID）原子性、一致性、隔离性、持久性？
事务的并发？事务隔离级别，每个级别会引发什么问题，MySQL默认是哪个级别？
MySQL常见的三种存储引擎（InnoDB、MyISAM、MEMORY）的区别？
MySQL的MyISAM与InnoDB两种存储引擎在，事务、锁级别，各自的适用场景？
查询语句不同元素（where、jion、limit、group by、having等等）执行先后顺序
索引为什么要用B+树，B+树和B-树的区别是什么
mysql的默认事务级别，一共有哪些事务级别
mysql的一些语句，这些肯定需要掌握的
mysql锁，行锁，表锁 ，什么时候发生锁，怎么锁，原理
数据库优化，最左原则啊，水平分表，垂直分表
什么是临时表，临时表什么时候删除?
MySQL B+Tree索引和Hash索引的区别？
sql查询语句确定创建哪种类型的索引？如何优化查询？
聚集索引和非聚集索引区别？
有哪些锁（乐观锁悲观锁），select 时怎么加排它锁？
非关系型数据库和关系型数据库区别，优势比较？
数据库三范式，根据某个场景设计数据表？
数据库的读写分离、主从复制，主从复制分析的 7 个问题？
使用explain优化sql和索引？
MySQL慢查询怎么解决？
什么是 内连接、外连接、交叉连接、笛卡尔积等？
mysql都有什么锁，死锁判定原理和具体场景，死锁怎么解决？
varchar和char的使用场景？
mysql 高并发环境解决方案？
数据库崩溃时事务的恢复机制（REDO日志和UNDO日志）？
03 Spring相关
spring的两大特性- ioc aop，实现原理
如果存在A依赖B，B依赖A，那么是怎么加到IOC中去的
beanFactory的理解，怎么加载bean
FactoryBean的理解
基于注解的形式，是怎么实现的， 你知道其原理吗，说一下
依赖冲突，有碰到过吗，你是怎么解决的~
bean的生命周期
spring中的自动装配方式
BeanFactory 和 FactoryBean
Spring IOC 的理解，其初始化过程？
BeanFactory 和 ApplicationContext？
Spring Bean 的生命周期，如何被管理的？Spring Bean 的加载过程是怎样的？
如果要你实现Spring AOP，请问怎么实现？
如果要你实现Spring IOC，你会注意哪些问题？
Spring 是如何管理事务的，事务管理机制？
Spring 的不同事务传播行为有哪些，干什么用的？
Spring 中用到了那些设计模式？
Spring MVC 的工作原理？
Spring 循环注入的原理？
Spring 如何保证 Controller 并发的安全？
你一般是怎么对mvc项目进行分层的
dispatch-servlet的工作原理
为什么有了springmvc还要在项目中使用spring？
springmvc的运行机制，dispatch -》 hanldermapping-—》handler -》handlerAdapter-》执行handler-》modelandview -》 返回mv -》 视图解析器-》返回view -》 渲染响应
怎么防止依赖注入
怎么让mapper 和xml对应
如何自动包装对象
和spring相比，做了什么改变
starter你知道哪些
如何部署springmvc项目 以及如何部署springboot项目
springboot的插件，你使用过哪些
04 中间件
4.1 redis
Redis用过哪些数据数据，以及Redis底层怎么实现
Redis缓存穿透，缓存雪崩
如何使用Redis来实现分布式锁
Redis的并发竞争问题如何解决
Redis持久化的几种方式，优缺点是什么，怎么实现的
Redis的缓存失效策略
Redis集群，高可用，原理
Redis缓存分片，Redis的数据淘汰策略
为什么选择redis，有什么好处，基于内存，抗压
redis集群怎么进行数据分配，hash槽
redis的主从复制是怎么实现的
redis的数据结构 最常问 hash是什么， sorted set怎么实现的
因为项目的原因，问我redis是怎么保证高可用的，主从和集群怎么加在一起
redis 和memcache的区别
redis 分布式锁的实现原理 setNX 啥的
redis模拟session，除了redis你还考虑过别的吗
redis的缓存击穿，怎么处理这个问题
redis是基于内存的，那么它有持久化吗，aof rdb
aof和rdb的优缺点，你在项目中使用的哪一个
4.2 MQ
为什么选择rabbitMQ， 社区活跃，高并发
别的MQ也要了解，比如RocketMQ(阿里的，java开发，再次开发，并发高，分布式，出错少)
ActiveMQ， kafka
topic 和 block
MQ的作用，同步转异步，消除峰值
如何保证数据一致性，即原子性，ack
消息队列在项目中的应用
4.3 nginx
怎么配置负载均衡
怎么限流
怎么使用nginx缓存
为什么使用nginx，有别的替代品吗
请解释 x Nginx 如何处理 P HTTP 请求
在 x Nginx 中，如何使用未定义的服务器名称来阻止处理请求? ?
使用“ 反向代理服务器 ” 的优点是什么?
x Nginx 服务器上的 r Master 和 和 r Worker 进程分别是什么?
nginx的压力测试，你测试过吗，能抗住多少压力
你如何通过不同于 0 80 的端口开启 Nginx?
是否有可能将 x Nginx 的错误替换为 2 502 错误、 503
s stub_status 和 和 r sub_filter 指令的作用是什么? ?
4.5 dubbo
原理，怎么用
和erueka有什么区别
为什么要用dubbo，不用行不行？
跨域请求的一些知识点
Dubbo 支持哪些协议，每种协议的应用场景，优缺点？
Dubbo 超时时间怎样设置？
Dubbo 集群的负载均衡有哪些策略
Dubbo 的主要应用场景？
Dubbo 服务注册与发现的流程？
Dubbo 中 中 zookeeper 做注册中心，如果注册中心集群都挂掉，发布者和订阅者之间还能通信么？
dubbo 服务负载均衡策略？
05 其他插件
5.1 shiro
怎么做权限控制
为什么使用shiro，你直接使用aop不也是一样的吗，shiro还有标签~各种扯
shiro的两个最重要的函数
认证和授权是怎么做的
5.2 docker
和vmware的区别
你一般是怎么部署的 IDEA，直接把项目部署到docker并打包到云服务器
docker的好处，小，快
06 Linux
常见的命令
sed 和 awk 感觉linux必考。。
linux的使用场景，你什么时候会用linux -- 》 布置服务器
怎么查看进程和杀死进程
打印一个文件夹中的所有文件
float在计算机中是怎么存储的，当时被问到的时候，我也在问自己，怎么存的~~~ 佛了
线程和进程的区别
线程的通信方式，进程的通信方式
系统线程的数量上限是多少
页式存储的概念
内存碎片，你有了解过吗，有想过解决方案吗~
07 算法
7.1 排序算法
八大排序算法真的是面试宠儿
最常考 快速排序 和归并排序
哪些排序算法是稳定的 哪些是不稳定的
堆排 也应该掌握
7.2 树
根据遍历结果恢复树，递归
二叉搜索树第k大
树的和为k的路径
层次遍历
根据层次遍历和后序遍历恢复树
镜像树
树的深度
是不是平衡二叉树
7.3 链表
反转链表
链表环的入口
交叉链表的交点
复杂链表的复制
二叉搜索树变成双向链表
7.4 回溯算法
走迷宫
游戏通关
7.5 递推算法
走台阶
断钢筋
7.6 背包问题
装最多的东西
7.7 贪心算法
覆盖问题
时间问题
08 设计模式
面试中设计模式其实也是挺重要的

Java 中什么叫单例设计模式？请用 Java 写出线程安全的单例模式
在 Java 中，什么叫观察者设计模式（observer design pattern）
使用工厂模式最主要的好处是什么？在哪里使用
举一个用 Java 实现的装饰模式(decorator design pattern) ？它是作用于对象层次还是类层次？
在 Java 中，什么时候用重载，什么时候用重写？
举例说明什么情况下会更倾向于使用抽象类而不是接口
观察者模式
适配模式
工厂模式