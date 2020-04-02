---
title: 快速失败(fail-fast)和安全失败(fail-safe)
date: 2020-02-24 15:59
comments: false
tags: 
- java
categories: 
- java
---

### 快速失败(fail-fast)和安全失败(fail-safe)

在java.util包中的集合类，像HashMap、ArrayList等都是快速失败的；而java.util.concurrent 包下的集合类都是安全失败的，例如ConcurrentHashMap。

#### 快速失败（fail-fast)

在使用迭代器对集合对象进行遍历的时候，如果A线程正在遍历集合，而B线程对集合进行了修改（增加、删除、修改），或者A线程在遍历的过程中修改了集合都会抛出ConcurrentModificationException异常。

因为迭代器在遍历时直接访问集合中的内容，并且在遍历过程中使用一个modCount的值。如果遍历期间集合内容发生变化就会改变modCount值。每次在迭代器使用hasNext()/next()，都会检测这个值是否有改变，如果发生改变就抛出异常终止遍历。

#### 安全失败（fail-safe)

采用安全失败机制的集合容器，在遍历时不是直接在集合内容上访问的，而是先复制原有集合内容，在拷贝的集合上进行遍历。

由于迭代时是对原集合的拷贝进行遍历，所以在遍历过程中对原集合所作的修改并不能被迭代器检测到，故不会抛 ConcurrentModificationException 异常
