---
title: redis
date: 2020-02-13 15:32
comments: false
tags: 
- redis
categories: 
- redis
keywords:
- redis
---

### redis

redis是一个key-value的存储系统。支持数据的持久化，可以将内存中的数据保存在磁盘中，重启的时候可以再次加载使用。redis不仅仅支持key-value的数据，还支持list、set、zset、hash等数据结构的存储。redis还支持数据的备份（master-salve模式）。

redis支持的数据结构：String、Hash、List、Set、SortedSet、HyperLogLog、Geo、Pub/Sub等。

redis一般有这些用途：

- 缓存：缓存在redis最为人熟知的场景，可以有效提升服务器的性能。
- 排行榜：使用redis的SortedSet可以很方便的实现排行榜。
- 限速器：利用redis中原子性的自增操作可以
- 好友关系：利用集合的一些命令（交集、并集、差集等）可以搞定共同好友之类的功能。
- 分布式锁：可以使用redis实现。
- 简单的消息队列：可以使用redis自身的发布订阅模式，也可以使用List来实现。

#### redis的基本数据结构

##### string

字符串是redis中最简单的数据结构，通过唯一的key来获取对应的value。redis的字符串是动态字符串，可以修改的字符串，内部结构实现类似于ArrayList，采用预分配冗余空间的方式来减少内存的频繁分配。当字符串长度小于1M时，扩容都是加倍先有的空间，如果超过1M后，扩容时一次只会多扩1M的空间。（字符串最大长度为512M）

键值对:

```
> set name codehole
OK
> get name
"codehole"
> exists name
(integer) 1
> del name
(integer) 1
```

批量键值对:

```
> set name1 codehole
OK
> set name2 holycoder
OK
> mget name1 name2 name3 # 返回一个列表
1) "codehole"
2) "holycoder"
3) (nil)
> mset name1 boy name2 girl name3 unknown
> mget name1 name2 name3
1) "boy"
2) "girl"
3) "unknown"
```

过期和set命令扩展：

```
> set name codehole
> get name
"codehole"
> expire name 5  # 5s 后过期
...  # wait for 5s
> get name
(nil)

> setex name 5 codehole  # 5s 后过期，等价于 set+expire
> get name
"codehole"
... # wait for 5s
> get name
(nil)

> setnx name codehole  # 如果 name 不存在就执行 set 创建
(integer) 1
> get name
"codehole"
> setnx name holycoder
(integer) 0  # 因为 name 已经存在，所以 set 创建不成功
> get name
"codehole"  # 没有改变
```

计数：
如果value是一个整数，还可以对它进行自增操作，自增的范围是signed long的最大最小值，超过这个值会报错。

```
> set age 30
OK
> incr age
(integer) 31
> incrby age 5
(integer) 36
> incrby age -5
(integer) 3
```

##### list

redis的列表相当于java中的LinkedList。这代表list的插入和删除操作很快，但是索引定位的时间复杂度会为O(N)。

list结构经常用来做异步队列来使用。将需要延后处理的任务结构体序列化为字符串塞进队列，另一个线程从这个列表中轮询数据进行处理。

右边进左边出：队列

```
> rpush books python java golang
(integer) 3
> llen books
(integer) 3
> lpop books
"python"
> lpop books
"java"
> lpop books
"golang"
> lpop books
(nil)
```

右边进右边出：栈

```
> rpush books python java golang
(integer) 3
> rpop books
"golang"
> rpop books
"java"
> rpop books
"python"
> rpop books
(nil)
```

lindex 相当于 get(int index) 方法，需要对链表进行遍历。

ltrim有两个参数start_index和end_index，定义了一个区间，在这个区间内的值保留，其他的舍弃。

##### hash

redis的hash相当于HashMap，是无序字典。内部实现也是数组+链表的二维结构。

在java中HashMap进行rehash的操作很耗时，需要一次性全部的rehash。但是在redis中为了高性能不能堵塞服务，采用了渐进式rehash策略。
渐进式rehash会在rehash的同时保留新旧两个hash就结构，查询时会同时查询两个hash结构，然后在后续的定时任务中以及hash操作中，循序渐进的将旧hash的内容一点点迁移到新的hash结构中。当迁移完成之后，就是使用新的hash结构代替。当hash移除了最后一个元素之后，该结构就会自动被删除，内存被回收。

```
> hset books java "think in java"  # 命令行的字符串如果包含空格，要用引号括起来
(integer) 1
> hset books golang "concurrency in go"
(integer) 1
> hset books python "python cookbook"
(integer) 1
> hgetall books  # entries()，key 和 value 间隔出现
1) "java"
2) "think in java"
3) "golang"
4) "concurrency in go"
5) "python"
6) "python cookbook"
> hlen books
(integer) 3
> hget books java
"think in java"
> hset books golang "learning go programming"  # 因为是更新操作，所以返回 0
(integer) 0
> hget books golang
"learning go programming"
> hmset books java "effective java" python "learning python" golang "modern golang programming"  # 批量 set
OK
```

hash结构中单个子key也可以进行计数，对应的指令是hincrby，和incr使用基本一样。

##### set

相当于HashSet，内部的键值对是无序的唯一的。它的内部实现相当于一个特殊的字典，字典中所有的 value 都是一个值NULL。
                         
```
> sadd books python
(integer) 1
> sadd books python  #  重复
(integer) 0
> sadd books java golang
(integer) 2
> smembers books  # 注意顺序，和插入的并不一致，因为 set 是无序的
1) "java"
2) "python"
3) "golang"
> sismember books java  # 查询某个 value 是否存在，相当于 contains(o)
(integer) 1
> sismember books rust
(integer) 0
> scard books  # 获取长度相当于 count()
(integer) 3
> spop books  # 弹出一个
"java"
```

##### zset

zset类似SortedSet和HashMap的结合，一方面是一个set保证了内部value的唯一性，另一方面给每个value赋予一个score，代表这个value排序的权重。

zset 可以用来存储学生的成绩，value 值是学生的 ID，score 是他的考试成绩。我们可以对成绩按分数进行排序就可以得到他的名次。

```
> zadd books 9.0 "think in java"
(integer) 1
> zadd books 8.9 "java concurrency"
(integer) 1
> zadd books 8.6 "java cookbook"
(integer) 1
> zrange books 0 -1  # 按 score 排序列出，参数区间为排名范围
1) "java cookbook"
2) "java concurrency"
3) "think in java"
> zrevrange books 0 -1  # 按 score 逆序列出，参数区间为排名范围
1) "think in java"
2) "java concurrenc
```

