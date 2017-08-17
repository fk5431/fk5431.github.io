---
title: 迭代器模式
date: 2017-08-17 09:19:52
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式, 迭代器模式
---

## 迭代器模式

### 介绍


### DEMO

看了JDK源码简单写了一个

首先是抽象迭代器

```
package factory.pattern.iterator;

/**
 * Created by FK on 2017/8/17.
 * 抽象迭代器类
 */
public interface Iterator<E> {
    public boolean hasNext();
    public E next();
}
```

抽象集合
```
package factory.pattern.iterator;

/**
 * Created by FK on 2017/8/17.
 */
public interface List<E> extends Iterable<E> {
    void add(E e);
    E get(int index);
    Iterator<E> iterator();
    int getSize();
}
```
抽象集合继承了iterable,iterable可以使得多个迭代器互不干扰
```
package factory.pattern.iterator;

/**
 * Created by FK on 2017/8/17.
 */
public interface Iterable<T> {
    Iterator<T> iterator();
}
```
具体的集合实现
```
package factory.pattern.iterator;

/**
 * Created by FK on 2017/8/17.
 */
public class ArrayList<E> implements List<E> {

    private Object[] elementData;
    private int size = 0;
    private int index = 0;

    public ArrayList() {
        this.elementData = new Object[10];
        this.size = 0;
        this.index = 0;
    }

    @Override
    public void add(E e) {
        elementData[index++] = e;
        size++;
    }

    @Override
    public E get(int index) {
        return (E) elementData[index];
    }

    @Override
    public Iterator<E> iterator() {
        return new ConcreteIterator();
    }

    @Override
    public int getSize() {
        return size;
    }

    private class ConcreteIterator implements Iterator<E>{
        int cursor;
        @Override
        public boolean hasNext() {
            return cursor != size ;
        }

        @Override
        public E next() {
            return (E) elementData[cursor++];
        }
    }
}
```

测试类
```
package factory.pattern.iterator;

/**
 * Created by FK on 2017/8/17.
 */
public class Test {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()){
            String str = iterator.next();
            System.out.println(str);
        }
    }
}
```