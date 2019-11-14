---
title: HashMap阅读 （1.8|1.7）
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

#### 1.7JDK
```
public V get(Object key) {
    if (key == null)
        return getForNullKey();
    Entry<K,V> entry = getEntry(key);

    return null == entry ? null : entry.getValue();
}
```

> 如果key是空值，单独获取

```
private V getForNullKey() {
    if (size == 0) {
        return null;
    }
    for (Entry<K,V> e = table[0]; e != null; e = e.next) {
        if (e.key == null)
            return e.value;
    }
    return null;
}
```

> null的key总是存储在table的0index中。

```
final Entry<K,V> getEntry(Object key) {
    if (size == 0) {
        return null;
    }

    int hash = (key == null) ? 0 : hash(key);
    for (Entry<K,V> e = table[indexFor(hash, table.length)];
         e != null;
         e = e.next) {
        Object k;
        if (e.hash == hash &&
            ((k = e.key) == key || (key != null && key.equals(k))))
            return e;
    }
    return null;
}
```

> 获取非空的key，先根据key的hash值和table的长度得到下标索引，在对应的index上的链表遍历查询。
> indexFor 即hash值与table.length 相与。


#### 1.8JDK

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

### HashMap put 方法

#### 1.7JDK

```
public V put(K key, V value) {
    if (table == EMPTY_TABLE) {
        inflateTable(threshold);
    }
    if (key == null)
        return putForNullKey(value);
    int hash = hash(key);
    int i = indexFor(hash, table.length);
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {
        Object k;
        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }

    modCount++;
    addEntry(hash, key, value, i);
    return null;
}
```

> 第一步，如果table为空的话进行初始化。第二步，如果key是空，将value存储table[0]中，先遍历如果有更新新值返回旧值，如果没有addEntry。
> 第三步，根据key计算hash值与table.length相与找到index，先遍历如果有更新新值返回旧值，如果没有addEntry。

```
void addEntry(int hash, K key, V value, int bucketIndex) {
    if ((size >= threshold) && (null != table[bucketIndex])) {
        resize(2 * table.length);
        hash = (null != key) ? hash(key) : 0;
        bucketIndex = indexFor(hash, table.length);
    }

    createEntry(hash, key, value, bucketIndex);
}
```

> addEntry方法中判断了这个bucketIndex的链表没有冲突且元素size超过负载因子*容量的大小就进行resize的操作，然后重新计算key 的hash值和在table的index，最后进行createEntry。
> createEntry 在链表中增加了一个节点。

```
void resize(int newCapacity) {
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
        threshold = Integer.MAX_VALUE;
        return;
    }

    Entry[] newTable = new Entry[newCapacity];
    transfer(newTable, initHashSeedAsNeeded(newCapacity));
    table = newTable;
    threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
}
void transfer(Entry[] newTable, boolean rehash) {
    int newCapacity = newTable.length;
    for (Entry<K,V> e : table) {
        while(null != e) {
            Entry<K,V> next = e.next;
            if (rehash) {
                e.hash = null == e.key ? 0 : hash(e.key);
            }
            int i = indexFor(e.hash, newCapacity);
            e.next = newTable[i];
            newTable[i] = e;
            e = next;
        }
    }
}
```

> resize 方法中调用了transfer，将原数组的中的元素转移到新数组中，这个过程中将链表反转到新数组中，此时多线程进行操作可能会导致链表出现环状，此时get这个key的index是这个环list会导致CPU100%。
