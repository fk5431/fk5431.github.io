---
title: JDK常用命令（虚拟机性能监控故障处理）
date: 2019-12-11 22:02
comments: false
tags: 
- java,JDK
categories: 
- java,JDK
keywords: java,JDK
---

### jps：虚拟机进程状况工具

jps的功能：可以列出正在运行的虚拟机进程，并显示虚拟机执行主类（Main Class，main()函数所在的类）名称以及这些进程的本地虚拟机唯一ID。
它是使用频率最高的JDK命令行工具，因为其他的JDK工具大多需要输入它查询到LVMID来确定要监控的是哪一个虚拟机进程。 对于本地虚拟机进程来讲，LVMID和操作系统的进程ID是一致的，使用PS（LINUX）也可以查询到虚拟机进程的LVMID，但是如果启动了多个虚拟机进程就无法根据进程名称定位。
jps命令格式：jps [options] [hostid]

| 选项 | 作用 |
| ---  | --- |
| -q  | 只输出LVMID，省略主类的名称 |
| -m  | 输出虚拟机进程启动时传递给主类main函数的参数 |
| -l  | 输出主类的全名，如果执行的是jar包，输出jar路径 |
| -v  | 输出虚拟机进程启动时jvm参数 |

### jstat：虚拟机统计信息监视工具

jstat (JVM Statistics Monitoring Tool)用于监视虚拟机各种运行状态信息的命令行工具。 它可以显示本地或者远程虚拟机进程中的类装载、内存、垃圾会搜、JIT编译等运行数据。

jstat的命令格式：jstat [ option vmid [interval[s|ms] [count]]]

> 对于命令格式中的VMID和LVMID，如果是本地虚拟机进程，两者一致。如果是远程虚拟机进程，则VMID的格式为[protocol:][//]lvmind[@hostname[:port]/servername]
> interval : 查询间隔  count：查询次数 如果省略interval和count，则只查询一次
> 选项option代表用户希望查询的虚拟机信息，主要分为3类：类装载、垃圾收集、运行期编译状况。

| 选项 | 作用 |
| ---  | --- |
| -class | 监视类装载、卸载数量，总空间以及类装载锁耗费的时间 |
| -gc | 监视Java堆状况，包括eden区、两个Survivor区、老年代、永久代等的容量、已使用空间、GC时间合计等信息 |
| -gccapacity | 监视内容与-gc基本相同，但输出主要关注Java堆各个区域使用的最大、最小空间 |
| -gcutil | 监视内容与-gc基本相同，但输出主要关注已使用空间占总空间的百分比 |
| -gccause | 与-gcutil功能一样，但是会额外输出导致上一次GC产生的原因 |
| -gcnew | 监视新生代GC状况 |
| -gcnewcapacity | 监视内容与-gcnew 基本相同，但输出主要关注Java堆各个区域使用的最大、最小空间 |
| -gcold | 监视老年代代GC状况 |
| -gcoldcapacity | 监视内容与-gcold 基本相同，但输出主要关注Java堆各个区域使用的最大、最小空间 |
| -gcpermcapacity | 输出永久代使用的最大、最小空间 |
| -compiler | 输出JIT编译器编译过的方法、耗时等信息 |
| -printcompilation | 输出已经被JIT编译的方法 |

输入:
```
jstat -gc 2780 1000 20
```

查看进程2780的GC情况，每隔1s查一次，查20次,部分返回如下：

```
S0C    S1C    S0U    S1U      EC       EU        OC         OU       PC     PU    YGC     YGCT    FGC    FGCT     GCT
4352.0 4352.0 1713.6  0.0   34944.0  13334.2   164708.0   149426.3    ?      ?      1042   13.752  14      0.445   14.196
4352.0 4352.0 1713.6  0.0   34944.0  13966.0   164708.0   149426.3    ?      ?      1042   13.752  14      0.445   14.196
4352.0 4352.0 1713.6  0.0   34944.0  14611.7   164708.0   149426.3    ?      ?      1042   13.752  14      0.445   14.196
4352.0 4352.0 1713.6  0.0   34944.0  15243.8   164708.0   149426.3    ?      ?      1042   13.752  14      0.445   14.196
4352.0 4352.0 1713.6  0.0   34944.0  15886.3   164708.0   149426.3    ?      ?      1042   13.752  14      0.445   14.196
4352.0 4352.0 1713.6  0.0   34944.0  16527.8   164708.0   149426.3    ?      ?      1042   13.752  14      0.445   14.196
```


- S0C：第一个幸存区的大小
- S1C：第二个幸存区的大小
- S0U：第一个幸存区的使用大小
- S1U：第二个幸存区的使用大小
- EC：年轻代的大小
- EU：年轻代使用大小
- OC：老年代大小
- OU：老年代使用大小
- PC：perm内存大小（永久区）
- PU：perm内存使用大小
- YGC：年轻代垃圾回收次数
- YGCT：年轻代垃圾回收消耗时间
- FGC：老年代垃圾回收次数
- FGCT：老年代垃圾回收消耗时间
- GCT： 垃圾回收消耗总时间

### jinfo：java配置信息工具

jinfo的作用是实时的查看和调整虚拟机各项参数。

语法：jinfo [option] pid

执行 jinfo -flags pid 命令后查询结果中，Non-default VM flags为虚拟机默认的设置参数，Command line为用户自行设置的参数

### jmap：java内存映像工具

jmap命令用于生成堆转储快照（一般称为heapdump或dump文件）。jmap不仅仅为了获取dump文件，它还可以查询finalize执行队列、Java堆、永久代的详细信息，如空间使用率、当前使用的是那种收集器等。

jmap 命令格式： jmap [option] vmid

| 选项 | 作用 |
| --- | ---  |
| -dump | 生成java堆转储快照。 格式为 -dump:[live,] format=b,file=<filename> 其中live子参数说明是否只dump出存活的对象 |
| -finalizeinfo | 显示在F-Queue中等待Finalizer线程执行finalize方法的对象。 只在linux/solaris平台有效 |
| -heap | 只显示Java堆详细信息。如使用脑胀回收器、参数配置、分代状况等。只在linux/solaris平台有效 |
| -histo | 显示堆中对象统计信息，包括类、实例数量、合计容量 |
| -permstat | 以ClassLoader为统计口径显示永久代内存状态。 只在linux/solaris平台有效 |
|  -F | 当虚拟机进程对-dump选项没有响应时，可以使用这个选项强制生成dump快照。只在linux/solaris平台有效 |

### jhat：虚拟机堆转储快照分析工具

jhat与jmap搭配使用，用于分析jmap生成的堆转储快照。jhat内置了一个微型的HTTP/HTML服务器，生成的dump文件的分析结果后，可以在浏览器中输入“http://localhost:7000”进行查看。

使用：
```
jmap -dump:format=b,file=test.bin 2780
jhat test.bin
```

### jstack：java堆栈跟踪工具

jstack作用：用于生成虚拟机当前时刻的线程快照。 
1. 线程快照是指当前虚拟机内的每一个线程正在执行的方法堆栈的集合。 
2. 生成线程快照的作用是可用于定位线程出现长时间停顿的原因，如线程间死锁 、死循环、请求外部资源导致的长时间等待等等问题，当线程出现停顿现象时，就可以用jstack查看各个线程调用的堆栈情况。

jstack命令格式：jstack [option] vmid

| 选项 | 作用 |
| --- | --- |
| -F | 当正常输出的请求不被响应时，强制输出线程堆栈 |
| -m | 如果调用到本地方法的话，可以显示C/C++的堆栈 |
| -l |  除了堆栈信息，显示关于锁的附件信息 |
