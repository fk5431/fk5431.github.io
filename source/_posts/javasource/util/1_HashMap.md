---
title: HashMap阅读 （1.8|1.7）
date: 2019-11-14 15:24:25
comments: false
tags: 
- java
- HashMap
categories: 
- java
keywords: 
- java
- HashMap
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

### HashMap 内 Node(1.8)

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
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}
```

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

> 首先进行校验 table不为空 && table长度大于0 && table索引位置(使用table.length - 1和hash值进行位与运算)的节点不为空。
> 其次看first节点（索引位节点）是不是目标节点，如果是目标节点则返回。
> 如果是红黑树节点，则调用红黑树的查找节点方法进行查找，如果是链表节点遍历查询。

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
> createEntry 在链表头部中增加了一个节点。

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


#### 1.8jDK 

```
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

> 如果table为空或者长度为0的时候辉县进行初始化，其次索引位置上的节点为null的时候会创建新的节点。
> 其次判断p节点的key和hash值是否跟传入的相等，如果相等此节点即为要查询的节点；如果p节点是红黑树节点，调用红黑树的putTreeVal查找目标节点；如果p是链表节点，找不到目标节点则创建一个新的节点，如果节点数超过8则转换为红黑树。
> 最后如果e节点不为空，则代表目标节点存在，使用传入的value覆盖该节点的value，并返回oldValue;如果没找到目标节点，超过阈值会执行resize扩容，

```
final Node<K,V>[] resize() {
     Node<K,V>[] oldTab = table;
     int oldCap = (oldTab == null) ? 0 : oldTab.length;
     int oldThr = threshold;
     int newCap, newThr = 0;
     // 1.老表的容量不为0，即老表不为空
     if (oldCap > 0) {
         // 1.1 判断老表的容量是否超过最大容量值：如果超过则将阈值设置为Integer.MAX_VALUE，并直接返回老表,
         // 此时oldCap * 2比Integer.MAX_VALUE大，因此无法进行重新分布，只是单纯的将阈值扩容到最大
         if (oldCap >= MAXIMUM_CAPACITY) {
             threshold = Integer.MAX_VALUE;
             return oldTab;
         }
         // 1.2 将newCap赋值为oldCap的2倍，如果newCap<最大容量并且oldCap>=16, 则将新阈值设置为原来的两倍
         else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                  oldCap >= DEFAULT_INITIAL_CAPACITY)
             newThr = oldThr << 1; // double threshold
     }
     // 2.如果老表的容量为0, 老表的阈值大于0, 是因为初始容量被放入阈值，则将新表的容量设置为老表的阈值
     else if (oldThr > 0)
         newCap = oldThr;
     else {
         // 3.老表的容量为0, 老表的阈值为0，这种情况是没有传初始容量的new方法创建的空表，将阈值和容量设置为默认值
         newCap = DEFAULT_INITIAL_CAPACITY;
         newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
     }
     // 4.如果新表的阈值为空, 则通过新的容量*负载因子获得阈值
     if (newThr == 0) {
         float ft = (float)newCap * loadFactor;
         newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                   (int)ft : Integer.MAX_VALUE);
     }
     // 5.将当前阈值设置为刚计算出来的新的阈值，定义新表，容量为刚计算出来的新容量，将table设置为新定义的表。
     threshold = newThr;
     @SuppressWarnings({"rawtypes","unchecked"})
     Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
     table = newTab;
     // 6.如果老表不为空，则需遍历所有节点，将节点赋值给新表
     if (oldTab != null) {
         for (int j = 0; j < oldCap; ++j) {
             Node<K,V> e;
             if ((e = oldTab[j]) != null) {  // 将索引值为j的老表头节点赋值给e
                 oldTab[j] = null; // 将老表的节点设置为空, 以便垃圾收集器回收空间
                 // 7.如果e.next为空, 则代表老表的该位置只有1个节点，计算新表的索引位置, 直接将该节点放在该位置
                 if (e.next == null)
                     newTab[e.hash & (newCap - 1)] = e;
                 // 8.如果是红黑树节点，则进行红黑树的重hash分布(跟链表的hash分布基本相同)
                 else if (e instanceof TreeNode)
                     ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                 else { // preserve order
                     // 9.如果是普通的链表节点，则进行普通的重hash分布
                     Node<K,V> loHead = null, loTail = null; // 存储索引位置为:“原索引位置”的节点
                     Node<K,V> hiHead = null, hiTail = null; // 存储索引位置为:“原索引位置+oldCap”的节点
                     Node<K,V> next;
                     do {
                         next = e.next;
                         // 9.1 如果e的hash值与老表的容量进行与运算为0,则扩容后的索引位置跟老表的索引位置一样
                         if ((e.hash & oldCap) == 0) {
                             if (loTail == null) // 如果loTail为空, 代表该节点为第一个节点
                                 loHead = e; // 则将loHead赋值为第一个节点
                             else
                                 loTail.next = e;    // 否则将节点添加在loTail后面
                             loTail = e; // 并将loTail赋值为新增的节点
                         }
                         // 9.2 如果e的hash值与老表的容量进行与运算为1,则扩容后的索引位置为:老表的索引位置＋oldCap
                         else {
                             if (hiTail == null) // 如果hiTail为空, 代表该节点为第一个节点
                                 hiHead = e; // 则将hiHead赋值为第一个节点
                             else
                                 hiTail.next = e;    // 否则将节点添加在hiTail后面
                             hiTail = e; // 并将hiTail赋值为新增的节点
                         }
                     } while ((e = next) != null);
                     // 10.如果loTail不为空（说明老表的数据有分布到新表上“原索引位置”的节点），则将最后一个节点
                     // 的next设为空，并将新表上索引位置为“原索引位置”的节点设置为对应的头节点
                     if (loTail != null) {
                         loTail.next = null;
                         newTab[j] = loHead;
                     }
                     // 11.如果hiTail不为空（说明老表的数据有分布到新表上“原索引+oldCap位置”的节点），则将最后
                     // 一个节点的next设为空，并将新表上索引位置为“原索引+oldCap”的节点设置为对应的头节点
                     if (hiTail != null) {
                         hiTail.next = null;
                         newTab[j + oldCap] = hiHead;
                     }
                 }
             }
         }
     }
     // 12.返回新表
     return newTab;
}
```

> 在JDK1.8中，resize操作不需要重新计算索引，且迁移新表后数据不会倒置。不需要重新计算hash，只需要用原来的hash值，加上高一位做为索引。