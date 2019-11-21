---
title: ConcurrentHashMap(1.7|1.8)阅读 
date: 2019-11-21 22:20:28
comments: false
tags: 
- java
categories: 
- java
keywords: ConcurrentHashMap
---

### ConcurrentHashMap 

#### jdk1.7

ConcurrentHashMap 和 HashMap非常类似，ConcurrentHashMap在实现上采取了分段锁的思想来实现的。

ConcurrentHashMap在内部采用了一个叫做 Segment 的结构，一个Segment就类似HashMap中的table，这样在定位一个元素的时候，需要进行两次hash操作，一次定位到Segment，第二次定位到Segment中的table的index。这样带来的好处的是写操作只需要针对对应的Segment的进行写，因此Segment的数量就是理论最大的并发数。

ConcurrentHashMap的结构类似如下： 

![结构图](../../../../uploads/javasource/util/ConcurrentHashMap.png)

##### Segment 
```
static final class Segment<K,V> extends ReentrantLock implements Serializable {
    private static final long serialVersionUID = 2249069246763182397L;
    //重试次数 单核 1 多核64
    static final int MAX_SCAN_RETRIES =
        Runtime.getRuntime().availableProcessors() > 1 ? 64 : 1;
    //Segment中的table
    transient volatile HashEntry<K,V>[] table;
    //元素的数量
    transient int count;
    //对table的大小造成影响的操作的数量
    transient int modCount;
    //阈值
    transient int threshold;
     //负载因子
    final float loadFactor;
    //构造函数
    Segment(float lf, int threshold, HashEntry<K,V>[] tab) {
        this.loadFactor = lf;
        this.threshold = threshold;
        this.table = tab;
    }

    final V put(K key, int hash, V value, boolean onlyIfAbsent) {
        //获取锁（针对Segment），获取到node为null，否则调用scanAndLockForPut方法
        HashEntry<K,V> node = tryLock() ? null :
            scanAndLockForPut(key, hash, value);
        V oldValue;
        try {
            //Segment中的table
            HashEntry<K,V>[] tab = table;
            int index = (tab.length - 1) & hash;
            HashEntry<K,V> first = entryAt(tab, index);
            for (HashEntry<K,V> e = first;;) {
                //对应index上的链表节点不为空，看看是不是有相等的key，有相等的key就替换； 
                if (e != null) {
                    K k;
                    if ((k = e.key) == key ||
                        (e.hash == hash && key.equals(k))) {
                        oldValue = e.value;
                        if (!onlyIfAbsent) {
                            e.value = value;
                            ++modCount;
                        }
                        break;
                    }
                    e = e.next;//遍历下一个
                }
                else { //链表节点为空
                    if (node != null)
                        node.setNext(first);//加到链表的头部
                    else //node为空就得新建
                        node = new HashEntry<K,V>(hash, key, value, first);
                    int c = count + 1;
                    if (c > threshold && tab.length < MAXIMUM_CAPACITY)
                        rehash(node); //需不需要rehash
                    else
                        setEntryAt(tab, index, node);//直接插入node到指定index位置
                    ++modCount;
                    count = c;
                    oldValue = null;
                    break;
                }
            }
        } finally {
            unlock();
        }
        return oldValue;
    }

    @SuppressWarnings("unchecked")
    private void rehash(HashEntry<K,V> node) {
        HashEntry<K,V>[] oldTable = table;//旧的table
        int oldCapacity = oldTable.length;
        int newCapacity = oldCapacity << 1;
        threshold = (int)(newCapacity * loadFactor);
        HashEntry<K,V>[] newTable =
            (HashEntry<K,V>[]) new HashEntry[newCapacity];//新的table
        int sizeMask = newCapacity - 1;
        for (int i = 0; i < oldCapacity ; i++) {//遍历旧的table
            HashEntry<K,V> e = oldTable[i]; //每个table的第一个entry
            if (e != null) {
                HashEntry<K,V> next = e.next;
                int idx = e.hash & sizeMask; //重新计算新的index
                if (next == null)   //  Single node on list 只有一个元素
                    newTable[idx] = e;//直接在新的table新index上赋值该节点
                else { // Reuse consecutive sequence at same slot
                    HashEntry<K,V> lastRun = e;
                    int lastIdx = idx;
                    for (HashEntry<K,V> last = next;
                         last != null;
                         last = last.next) {//遍历链表
                        int k = last.hash & sizeMask; //在新table的位置
                        if (k != lastIdx) {//头结点和头结点的next元素的节点发生了变化
                            lastIdx = k;
                            lastRun = last;
                        }
                    }
                    // 1. lastRun 和 lastIdx 没有发生变化,也就是整个链表的每个元素位置和一样,都没有发生变化
                    // 2.    lastRun 和 lastIdx 发生了变化,记录变化位置和变化节点,然后把变化的这个节点设置到新table
                    newTable[lastIdx] = lastRun;
                    // Clone remaining nodes
                    for (HashEntry<K,V> p = e; p != lastRun; p = p.next) {
                        V v = p.value;
                        int h = p.hash;
                        int k = h & sizeMask;
                        HashEntry<K,V> n = newTable[k];
                        newTable[k] = new HashEntry<K,V>(h, p.key, v, n);
                    }
                }
            }
        }
        // 处理扩容时那个添加的节点
        int nodeIndex = node.hash & sizeMask; // add the new node
        node.setNext(newTable[nodeIndex]);
        newTable[nodeIndex] = node;
        table = newTable;
    }
    //自选获取锁
    private HashEntry<K,V> scanAndLockForPut(K key, int hash, V value) {
        HashEntry<K,V> first = entryForHash(this, hash);//获取头节点 
        HashEntry<K,V> e = first;
        HashEntry<K,V> node = null;
        int retries = -1; // negative while locating node 是为了找到对应hash桶,遍历链表时找到就停止
        while (!tryLock()) { //自旋获取锁
            HashEntry<K,V> f; // to recheck first below
            if (retries < 0) {
                if (e == null) {//结束遍历节点
                    if (node == null) // speculatively create node
                        node = new HashEntry<K,V>(hash, key, value, null);
                    retries = 0;
                }
                else if (key.equals(e.key))// 找到节点 停止遍历
                    retries = 0;
                else
                    e = e.next;
            }
            else if (++retries > MAX_SCAN_RETRIES) {// 达到自旋的最大次数
                lock();//阻塞
                break;
            }
            else if ((retries & 1) == 0 &&
                     (f = entryForHash(this, hash)) != first) {//// 头结点变化,需要重新遍历,说明有新的节点加入或者移除
                e = first = f; // re-traverse if entry changed
                retries = -1;
            }
        }
        return node;
    }
    //自旋获取锁
    private void scanAndLock(Object key, int hash) {
        // similar to but simpler than scanAndLockForPut
        HashEntry<K,V> first = entryForHash(this, hash);
        HashEntry<K,V> e = first;
        int retries = -1;
        while (!tryLock()) {
            HashEntry<K,V> f;
            if (retries < 0) {
                if (e == null || key.equals(e.key))
                    retries = 0;
                else
                    e = e.next;
            }
            else if (++retries > MAX_SCAN_RETRIES) {
                lock();
                break;
            }
            else if ((retries & 1) == 0 &&
                     (f = entryForHash(this, hash)) != first) {
                e = first = f;
                retries = -1;
            }
        }
    }

    final V remove(Object key, int hash, Object value) {
        if (!tryLock())
            scanAndLock(key, hash);
        V oldValue = null;
        try {
            HashEntry<K,V>[] tab = table;
            int index = (tab.length - 1) & hash;
            HashEntry<K,V> e = entryAt(tab, index);
            HashEntry<K,V> pred = null;
            while (e != null) {
                K k;
                HashEntry<K,V> next = e.next;
                if ((k = e.key) == key ||
                    (e.hash == hash && key.equals(k))) {
                    V v = e.value;
                    if (value == null || value == v || value.equals(v)) {
                        if (pred == null)
                            setEntryAt(tab, index, next);
                        else
                            pred.setNext(next);
                        ++modCount;
                        --count;
                        oldValue = v;
                    }
                    break;
                }
                pred = e;
                e = next;
            }
        } finally {
            unlock();
        }
        return oldValue;
    }

    final boolean replace(K key, int hash, V oldValue, V newValue) {
        if (!tryLock())
            scanAndLock(key, hash);
        boolean replaced = false;
        try {
            HashEntry<K,V> e;
            for (e = entryForHash(this, hash); e != null; e = e.next) {
                K k;
                if ((k = e.key) == key ||
                    (e.hash == hash && key.equals(k))) {
                    if (oldValue.equals(e.value)) {
                        e.value = newValue;
                        ++modCount;
                        replaced = true;
                    }
                    break;
                }
            }
        } finally {
            unlock();
        }
        return replaced;
    }

    final V replace(K key, int hash, V value) {
        if (!tryLock())
            scanAndLock(key, hash);
        V oldValue = null;
        try {
            HashEntry<K,V> e;
            for (e = entryForHash(this, hash); e != null; e = e.next) {
                K k;
                if ((k = e.key) == key ||
                    (e.hash == hash && key.equals(k))) {
                    oldValue = e.value;
                    e.value = value;
                    ++modCount;
                    break;
                }
            }
        } finally {
            unlock();
        }
        return oldValue;
    }

    final void clear() {
        lock();
        try {
            HashEntry<K,V>[] tab = table;
            for (int i = 0; i < tab.length ; i++)
                setEntryAt(tab, i, null);
            ++modCount;
            count = 0;
        } finally {
            unlock();
        }
    }
}
```

Segment中主要的方法就是put添加元素，rehash 对table进行扩容，scanAndLockForPut，scanAndLock自旋获取锁。

##### 成员变量
```
static final int DEFAULT_INITIAL_CAPACITY = 16;

static final float DEFAULT_LOAD_FACTOR = 0.75f;

static final int DEFAULT_CONCURRENCY_LEVEL = 16;

static final int MAXIMUM_CAPACITY = 1 << 30;

static final int MIN_SEGMENT_TABLE_CAPACITY = 2;

static final int MAX_SEGMENTS = 1 << 16; // slightly conservative

static final int RETRIES_BEFORE_LOCK = 2;
```
##### put
```
public V put(K key, V value) {
    Segment<K,V> s;
    if (value == null)
        throw new NullPointerException();
    int hash = hash(key);
    int j = (hash >>> segmentShift) & segmentMask;
    if ((s = (Segment<K,V>)UNSAFE.getObject          // nonvolatile; recheck
         (segments, (j << SSHIFT) + SBASE)) == null) //  in ensureSegment
        s = ensureSegment(j);
    return s.put(key, hash, value, false);
}
private Segment<K,V> ensureSegment(int k) {
    final Segment<K,V>[] ss = this.segments;
    long u = (k << SSHIFT) + SBASE; // raw offset
    Segment<K,V> seg;
    if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u)) == null) {
        Segment<K,V> proto = ss[0]; // use segment 0 as prototype
        int cap = proto.table.length;
        float lf = proto.loadFactor;
        int threshold = (int)(cap * lf);
        HashEntry<K,V>[] tab = (HashEntry<K,V>[])new HashEntry[cap];
        if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u))
            == null) { // recheck
            Segment<K,V> s = new Segment<K,V>(lf, threshold, tab);
            while ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u))
                   == null) {
                if (UNSAFE.compareAndSwapObject(ss, u, null, seg = s))
                    break;
            }
        }
    }
    return seg;
}
```

##### get
```
public V get(Object key) {
    Segment<K,V> s; // manually integrate access methods to reduce overhead
    HashEntry<K,V>[] tab;
    int h = hash(key);
    long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
    if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null &&
        (tab = s.table) != null) {
        for (HashEntry<K,V> e = (HashEntry<K,V>) UNSAFE.getObjectVolatile
                 (tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE);
             e != null; e = e.next) {
            K k;
            if ((k = e.key) == key || (e.hash == h && key.equals(k)))
                return e.value;
        }
    }
    return null;
}
```

