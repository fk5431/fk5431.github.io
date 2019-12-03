---
title: ReentrantReadWriteLock阅读 
date: 2019-12-02 21:48:17
comments: false
tags: 
- java
categories: 
- java
keywords: ReentrantReadWriteLock,锁,同步
---

### ReentrantReadWriteLock

ReentrantReadWriteLock实现的是ReadWriteLock接口，ReadWriteLock接口中只定义了 readLock 和 writeLock 方法。

ReadLock和WriteLock方法都是通过Sync的方法实现的，所以Sync是读写锁的核心：

#### Sync