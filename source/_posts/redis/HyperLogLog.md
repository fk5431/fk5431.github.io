---
title: HyperLogLog
date: 2020-02-13 16:52
comments: false
tags: 
- redis
- HyperLogLog
categories: 
- redis
- HyperLogLog
keywords:
- HyperLogLog
---

### HyperLogLog

HyperLogLog提供不精确的去重技术方案，虽然不精确但是也不是非常不精确，标准误差是0.81%。可以满足一些统计UV的需求。

#### 使用方法

HyperLogLog提供了两个指令pfadd和pfcount，一个是增加计数，一个是获取计数。

```
127.0.0.1:6379> pfadd codehole user1
(integer) 1
127.0.0.1:6379> pfcount codehole
(integer) 1
127.0.0.1:6379> pfadd codehole user2
(integer) 1
127.0.0.1:6379> pfcount codehole
(integer) 2
127.0.0.1:6379> pfadd codehole user3
(integer) 1
127.0.0.1:6379> pfcount codehole
(integer) 3
127.0.0.1:6379> pfadd codehole user4
(integer) 1
127.0.0.1:6379> pfcount codehole
(integer) 4
127.0.0.1:6379> pfadd codehole user5
(integer) 1
127.0.0.1:6379> pfcount codehole
(integer) 5
127.0.0.1:6379> pfadd codehole user6
(integer) 1
127.0.0.1:6379> pfcount codehole
(integer) 6
127.0.0.1:6379> pfadd codehole user7 user8 user9 user10
(integer) 1
127.0.0.1:6379> pfcount codehole
(integer) 10
```

#### 注意事项

HyperLogLog这个数据结构需要占据一定12K的存储，不适合统计单个用户相关的数据。redis对HyperLogLog的存储进行了优化，在技术比较小时，它的存储空间采用稀疏矩阵存储，空间占用很小，仅仅在计数慢慢变大，稀疏矩阵的占用空间超过了阈值才会一次性转变为稠密矩阵，才会占用12K空间。

