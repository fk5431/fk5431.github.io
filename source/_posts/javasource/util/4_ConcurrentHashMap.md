---
title: ConcurrentHashMap(1.7|1.8)阅读 
date: 2019-11-21 22:20:28
comments: false
tags: 
- java
- ConcurrentHashMap
categories: 
- java
keywords: 
- java
- ConcurrentHashMap 
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

ConcurrentHashMap 主要的默认参数和HashMap差不多。

> - DEFAULT_CONCURRENCY_LEVEL 是默认的并发数（Segment数组的数量）
> - MIN_SEGMENT_TABLE_CAPACITY 每个Segment最小的容量 
> - MAX_SEGMENTS 每个Segment最大的容量   
> - RETRIES_BEFORE_LOCK默认的自旋次数

##### put
```
public V put(K key, V value) {
    Segment<K,V> s;
    if (value == null)
        throw new NullPointerException();
    int hash = hash(key);//对应key的hash值
    int j = (hash >>> segmentShift) & segmentMask;//获取对应hash值在segments数组的index
    if ((s = (Segment<K,V>)UNSAFE.getObject          // nonvolatile; recheck
         (segments, (j << SSHIFT) + SBASE)) == null) //  in ensureSegment
        s = ensureSegment(j); //如果对应内存偏移量中没有，则创建ensureSegment
    return s.put(key, hash, value, false);
}
private Segment<K,V> ensureSegment(int k) {
    final Segment<K,V>[] ss = this.segments;
    long u = (k << SSHIFT) + SBASE; // raw offset
    Segment<K,V> seg;
    if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u)) == null) {
        Segment<K,V> proto = ss[0]; // use segment 0 as prototype  以初始化创建的第一个index下标的值为模板
        int cap = proto.table.length;
        float lf = proto.loadFactor;
        int threshold = (int)(cap * lf);
        HashEntry<K,V>[] tab = (HashEntry<K,V>[])new HashEntry[cap];
        if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u))
            == null) { // recheck 再次检查有没有其他线程已经创建了
            Segment<K,V> s = new Segment<K,V>(lf, threshold, tab);
            while ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u))
                   == null) {
                if (UNSAFE.compareAndSwapObject(ss, u, null, seg = s)) // 通过cas自旋的方式来创建
                    break;
            }
        }
    }
    return seg;
}
```

ConcurrentHashMap中value不能为null，否则会抛出空指针异常，然后获取到对应key的hash值对应的Segment。
如果对应的Segment不存在，则通过CAS的方式去创建Segment，这是不加锁方式下又可能保证线程安全的方法。

##### get
```
public V get(Object key) {
    Segment<K,V> s; // manually integrate access methods to reduce overhead
    HashEntry<K,V>[] tab;
    int h = hash(key);//对应key的hash值
    long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;//获取对应hash值存储所在segments数组中内存偏移量
    if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null &&
        (tab = s.table) != null) { //获取到对应Segment中的table
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

ConcurrentHashMap 的get方法就是先获取到对应的Segment，在获取到Segment中的table对应的HashEntry的头节点，但是通过UNSAFE.getObjectVolatile 获取到的是内存中最新的数据，在遍历的过程中，有可能数据被其他线程修改，导致最终返回的数据可能是过时的数据，因此此方法是弱一致性的。

##### size

```
public int size() {
    final Segment<K,V>[] segments = this.segments;
    int size;
    boolean overflow; // 是否溢出
    long sum;         // 存储本次循环过程中计算得到的modCount的值
    long last = 0L;   // 存储上一次遍历过程中计算得到的modCount的和
    int retries = -1; // first iteration isn't retry
    try {
        //无限for循环，结束条件就是任意前后两次遍历过程中modcount值的和是一样的，说明第二次遍历没有做任何变化
        for (;;) {//
            //
            //为了防止由于有线程不断在更新map而导致每次遍历过程一直发现modCount和上一次不一样导致线程一直进行遍历验证前后两次modCoun,加了一个最多重复的次数限制,超过这个次数则直接强制对所有的segment进行加锁(会导致可以延迟创建的Segment在这个过程中被创建）
            if (retries++ == RETRIES_BEFORE_LOCK) {
                for (int j = 0; j < segments.length; ++j)
                    ensureSegment(j).lock(); // force creation
            }
            sum = 0L;
            size = 0;
            overflow = false;
            for (int j = 0; j < segments.length; ++j) {
                Segment<K,V> seg = segmentAt(segments, j);
                if (seg != null) {
                    sum += seg.modCount;
                    int c = seg.count;
                    if (c < 0 || (size += c) < 0)
                        overflow = true;
                }
            }
            if (sum == last)
                break;
            last = sum;
        }
    } finally {
        //由于只有在retries等于RETRIES_BEFORE_LOCK时才会执行强制加锁，并且由于是用的retries++，所以强制加锁完毕后，retries的值是一定会大于RETRIES_BEFORE_LOCK的，
        if (retries > RETRIES_BEFORE_LOCK) {
            for (int j = 0; j < segments.length; ++j)
                segmentAt(segments, j).unlock();
        }
    }
    return overflow ? Integer.MAX_VALUE : size;
}
```

#### jdk1.8

在jdk1.8中没有采用分段锁的方式保证在多线程下的安全，采用的是CAS+synchronized来保证在多线程的安全。
底层的数据存储也变成了数组+链表+红黑树的方式。

##### Node

Node 和HashMap中的Node结构大致类似，只不过value和next通过volatile进行了修饰，保证了内存的可见性。还增加了一个find方法，通过这个node遍历之后的全部node找对对应key的节点。

```
final int hash;
final K key;
volatile V val;
volatile Node<K,V> next;

Node<K,V> find(int h, Object k) {
    Node<K,V> e = this;
    if (k != null) {
        do {
            K ek;
            if (e.hash == h &&
                ((ek = e.key) == k || (ek != null && k.equals(ek))))
                return e;
        } while ((e = e.next) != null);
    }
    return null;
}
```

##### ConcurrentHashMap的初始化

```
private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    while ((tab = table) == null || tab.length == 0) {
        //如果sizeCtl < 0 说明已经有其他线程进行了初始化，此时只需要将CPU的时间片让出即可
        if ((sc = sizeCtl) < 0)
            Thread.yield(); // lost initialization race; just spin
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            //进行初始化
            try {
                if ((tab = table) == null || tab.length == 0) {
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    @SuppressWarnings("unchecked")
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    table = tab = nt;
                    sc = n - (n >>> 2);//0.75*capacity
                }
            } finally {
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}
```

初始化的操作会在第一次put操作的时候进行，初始化的时候会调整table的大小。

##### put方法

```
public V put(K key, V value) {
    return putVal(key, value, false);
}

final V putVal(K key, V value, boolean onlyIfAbsent) {
    //value不能为空
    if (key == null || value == null) throw new NullPointerException();
    int hash = spread(key.hashCode());//获取key的hash值 （(h ^ (h >>> 16)) & HASH_BITS;）
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        if (tab == null || (n = tab.length) == 0)//第一次put的时候对table进行初始化
            tab = initTable();
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) { //如果table对应的index上的Node为空 进行初始化
            if (casTabAt(tab, i, null,
                         new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        else if ((fh = f.hash) == MOVED)// 当前Map在扩容，先协助扩容，在更新值。
            tab = helpTransfer(tab, f);
        else { //hash冲突
            V oldVal = null;
            synchronized (f) {//通过synchronized 进行加锁
                if (tabAt(tab, i) == f) {//链表头结点
                    if (fh >= 0) {
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {//如果key存在的情况
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {//节点不存在的情况，直接加在链表的尾部
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    else if (f instanceof TreeBin) { //如果节点是红黑树节点，插入到红黑树节点中
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)//如果链表长度大于等于8
                    treeifyBin(tab, i); //转变为红黑树
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    addCount(1L, binCount);// 统计节点个数，检查是否需要resize
    return null;
}
```

1.8的ConcurrentHashMap进行put操作的时候，如果第一次进行put会先初始化table数组，如果对应的bucket为空，通过CAS将node放入对应的bucket，如果当前Map正在进行扩容，则先协助扩容；最后对这个节点加锁，如果节点存在进行更新值，如果不存在加在链表的尾部，如果是红黑树节点插入红黑树，最后会判断如果链表长度超过8会将当前链表转换为红黑树。

###### helpTransfer

```
final Node<K,V>[] helpTransfer(Node<K,V>[] tab, Node<K,V> f) {
    Node<K,V>[] nextTab; int sc;
    //tab不为空 && f 属于ForwardingNode类型（Node的hash值为-1）
    //如果nextTable为null,则表示迁移完成了
    if (tab != null && (f instanceof ForwardingNode) &&
        (nextTab = ((ForwardingNode<K,V>)f).nextTable) != null) {
        int rs = resizeStamp(tab.length);//要调整的大小
        while (nextTab == nextTable && table == tab &&
               (sc = sizeCtl) < 0) {
            if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                sc == rs + MAX_RESIZERS || transferIndex <= 0)
                break;
            //每有一个线程来帮助迁移，sizeCtl就+1
            if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                transfer(tab, nextTab);
                break;
            }
        }
        return nextTab;
    }
    return table;
}
```

此方法是帮助Map进行扩容

###### addCount 

```
private final void addCount(long x, int check) {
    CounterCell[] as; long b, s;
    //更新baseCount
    if ((as = counterCells) != null ||
        !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
        CounterCell a; long v; int m;
        boolean uncontended = true;
        if (as == null || (m = as.length - 1) < 0 ||
            (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
            !(uncontended =
              U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
            fullAddCount(x, uncontended);
            return;
        }
        if (check <= 1)
            return;
        s = sumCount();
    }
    if (check >= 0) {//是不是需要检查
        Node<K,V>[] tab, nt; int n, sc;
        //符合扩容条件，map.size 大于阈值 table不为空，链表长度超过8
        while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
               (n = tab.length) < MAXIMUM_CAPACITY) {
            int rs = resizeStamp(n);//根据长度得到一标识
            if (sc < 0) {//正在扩容
                // 如果 sc 的低 16 位不等于 标识符（校验异常 sizeCtl 变化了）
                // 如果 sc == 标识符 + 1 （扩容结束了，不再有线程进行扩容）（默认第一个线程设置 sc ==rs 左移 16 位 + 2，当第一个线程结束扩容了，就会将 sc 减一。这个时候，sc 就等于 rs + 1）
                // 如果 sc == 标识符 + 65535（帮助线程数已经达到最大）
                // 如果 nextTable == null（结束扩容了）
                // 如果 transferIndex <= 0 (转移状态变化了)
                // 结束循环 
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                    transferIndex <= 0)
                    break;
                // 如果可以帮助扩容，那么将 sc 加 1. 表示多了一个线程在帮助扩容
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }
            // 如果不在扩容，将 sc 更新：标识符左移 16 位 然后 + 2. 也就是变成一个负数。高 16 位是标识符，低 16 位初始是 2.
            else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                         (rs << RESIZE_STAMP_SHIFT) + 2))
                // 更新 sc 为负数后，开始扩容。
                transfer(tab, null);
            s = sumCount();
        }
    }
}
```

addCount 方法主要进行了两个操作 1 更新baseCount 2 检查是不是需要扩容
主要说下 检查扩容的部分，首先如果满足扩容条件的话，如果sc是负数则代表正在扩容，此时如果 sizeCtl变化|扩容结束|帮助线程达到最大|等结束循环。如果可以帮助扩容的话就在增加一个线程帮助扩容；如果还没开始扩容就把sc设为负数开始扩容。

###### transfer 扩容

```
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
    int n = tab.length, stride;
    //stride最小为16
    if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
        stride = MIN_TRANSFER_STRIDE; // subdivide range
    if (nextTab == null) {            // initiating
        try {
            @SuppressWarnings("unchecked")
            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];//扩容两倍
            nextTab = nt;
        } catch (Throwable ex) {      // try to cope with OOME
            sizeCtl = Integer.MAX_VALUE;
            return;
        }
        nextTable = nextTab;
        transferIndex = n;
    }
    int nextn = nextTab.length;
    //正在被迁移的node，ForwardingNode的hash 是MOVED，key value next 都是null，其中的nextTable指向新的tab
    ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
    boolean advance = true;//advance为true，可以继续迁移下一个节点，false则停止迁移
    boolean finishing = false; // to ensure sweep before committing nextTab
    for (int i = 0, bound = 0;;) { //i迁移位置的索引，bound迁移的边界
        Node<K,V> f; int fh;
        while (advance) {
            int nextIndex, nextBound;
            if (--i >= bound || finishing) //finishing 为true停止while循环
                advance = false;
            else if ((nextIndex = transferIndex) <= 0) {//transferIndex（上一次迁移的边界）赋值给nextInde，transferIndex小于等于0，说明原数组的所有位置的迁移都有相应的线程去处理了，该线程可以不用迁移了
                i = -1;
                advance = false;
            }
            //将nextBound赋值给bound，nextBound = nextIndex - stride（上一个边界减去步长）
            else if (U.compareAndSwapInt
                     (this, TRANSFERINDEX, nextIndex,
                      nextBound = (nextIndex > stride ?
                                   nextIndex - stride : 0))) {
                bound = nextBound;
                i = nextIndex - 1;
                advance = false;
            }
        }
        //i < 0 所有迁移任务完成
        if (i < 0 || i >= n || i + n >= nextn) {
            int sc;
             //所有迁移完成，将nextTable设为空，sizeCtl为新tab.length * 0.75
            if (finishing) {
                nextTable = null;
                table = nextTab;
                sizeCtl = (n << 1) - (n >>> 1);
                return;
            }
            //该线程完成迁移，sizeCtl - 1，对应之前helpTransfer()中+1
            if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                //不相等说明还有其他线程没完成迁移，该线程结束任务
                if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                    return;
                finishing = advance = true;//如果相等，则说明说有线程都完成任务了，设置finish为true
                i = n; // recheck before commit
            }
        }
        else if ((f = tabAt(tab, i)) == null)//如果旧tab[i]为null，则放入ForwardingNode
            advance = casTabAt(tab, i, null, fwd);
        else if ((fh = f.hash) == MOVED)//该节点为ForwardingNode，则说明已经被迁移过了，就可以开始迁移下一个节点了
            advance = true; // already processed
        else {
            synchronized (f) {//迁移开始加锁
                if (tabAt(tab, i) == f) {
                    Node<K,V> ln, hn;
                    if (fh >= 0) {//fh >= 0,说明是链表结构
                        int runBit = fh & n;
                        Node<K,V> lastRun = f;
                        for (Node<K,V> p = f.next; p != null; p = p.next) {
                            int b = p.hash & n;
                            if (b != runBit) {
                                runBit = b;
                                lastRun = p;
                            }
                        }
                        if (runBit == 0) {
                            ln = lastRun;
                            hn = null;
                        }
                        else {
                            hn = lastRun;
                            ln = null;
                        }
                        for (Node<K,V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash; K pk = p.key; V pv = p.val;
                            if ((ph & n) == 0)
                                ln = new Node<K,V>(ph, pk, pv, ln);
                            else
                                hn = new Node<K,V>(ph, pk, pv, hn);
                        }
                        setTabAt(nextTab, i, ln);
                        setTabAt(nextTab, i + n, hn);
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                    else if (f instanceof TreeBin) { //红黑树结构
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> lo = null, loTail = null;
                        TreeNode<K,V> hi = null, hiTail = null;
                        int lc = 0, hc = 0;
                        for (Node<K,V> e = t.first; e != null; e = e.next) {
                            int h = e.hash;
                            TreeNode<K,V> p = new TreeNode<K,V>
                                (h, e.key, e.val, null, null);
                            if ((h & n) == 0) {
                                if ((p.prev = loTail) == null)
                                    lo = p;
                                else
                                    loTail.next = p;
                                loTail = p;
                                ++lc;
                            }
                            else {
                                if ((p.prev = hiTail) == null)
                                    hi = p;
                                else
                                    hiTail.next = p;
                                hiTail = p;
                                ++hc;
                            }
                        }
                        ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                            (hc != 0) ? new TreeBin<K,V>(lo) : t;
                        hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                            (lc != 0) ? new TreeBin<K,V>(hi) : t;
                        setTabAt(nextTab, i, ln);
                        setTabAt(nextTab, i + n, hn);
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                }
            }
        }
    }
}
```

1. 遍历整个table，当前节点为空，则采用CAS的方式在当前位置放入fwd
2. 当前节点已经为fwd(with hash field “MOVED”)，则已经有有线程处理完了了，直接跳过 ，这里是控制并发扩容的核心
3. 当前节点为链表节点或红黑树，重新计算链表节点的hash值，移动到nextTable相应的位置（构建了一个反序链表和顺序链表，分别放置在i和i+n的位置上）。移动完成后，用Unsafe.putObjectVolatile在tab的原位置赋为为fwd, 表示当前节点已经完成扩容。


##### get

```
public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
    int h = spread(key.hashCode());
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (e = tabAt(tab, (n - 1) & h)) != null) {
        if ((eh = e.hash) == h) {
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                return e.val;
        }
        else if (eh < 0)
            return (p = e.find(h, key)) != null ? p.val : null;
        while ((e = e.next) != null) {
            if (e.hash == h &&
                ((ek = e.key) == key || (ek != null && key.equals(ek))))
                return e.val;
        }
    }
    return null;
}
```

读取元素的操作就很简单，获取到对应的hash值，如果 table为空或对应的头节点为空直接返回null，不然就找到对应的bucket找到对应的node返回。

