---
title: HashMap阅读 （1.8）
date: 2019-11-14 15:24:25
comments: false
tags: 
- java
categories: 
- java
keywords: HashMap
---

### HashMap 内部默认的参数

> | 变量 | 值 |  含义 |
> | ---- | ---- | ---- |
> |DEFAULT_INITIAL_CAPACITY| 16 | 默认容量|
> | MAXIMUM_CAPACITY | 1 << 30 | 最大容量 | 
> | DEFAULT_LOAD_FACTOR | 0.75 | 负载因子 |
> | TREEIFY_THRESHOLD | 8 | 链表超过8转为红黑树 |
> | UNTREEIFY_THRESHOLD | 6 | 红黑树转为链表的阈值 |
> | MIN_TREEIFY_CAPACITY | 6 | 当table的长度小于64时，只是进行扩容 |

### HashMap 内 Node

```
static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }
```

内部存储key value的结构，本质是一个链表，其中包括key，value，key value的hash值异或的hash和下一个节点。

### HashMap的扰动函数 

1.8JDK
```
static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
```

1.7JDK
```
final int hash(Object k) {
        int h = hashSeed;
        if (0 != h && k instanceof String) {
            return sun.misc.Hashing.stringHash32((String) k);
        }
 
        h ^= k.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
```

自己的高半区和低半区做异或，为了混合原始哈希码的高位和低位，以此来加大低位的随机性。JDK 1.7做了四次右位移异或混合，目的都一致。

### HashMap get 方法
```
final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```