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

readLock 和 writeLock方法分别返回了内部的读锁和写锁。
```
public ReentrantReadWriteLock.WriteLock writeLock() { return writerLock; }
public ReentrantReadWriteLock.ReadLock  readLock()  { return readerLock; }
```
ReadLock和WriteLock方法都是通过Sync的方法实现的，所以Sync是读写锁的核心：

> 对同一线程，读，写读，写写是共享的，读写是互斥的；对于不同线程而言，读是共享的，读写、写写、写读都是互斥的。

#### Sync

##### Sync中的常量和结构

```
//低16位为写锁状态，高16位为读锁状态
static final int SHARED_SHIFT   = 16;
//读锁每次增加的单位
static final int SHARED_UNIT    = (1 << SHARED_SHIFT);
//读锁的最大数
static final int MAX_COUNT      = (1 << SHARED_SHIFT) - 1;
//写锁的掩码
static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;
//返回读锁的数量
static int sharedCount(int c)    { return c >>> SHARED_SHIFT; }
//返回写锁的数量 EXCLUSIVE_MASK 高位都是0
static int exclusiveCount(int c) { return c & EXCLUSIVE_MASK; }
//当前读线程的计数器
static final class HoldCounter {
    int count = 0;//当前读线程的重入数
    final long tid = getThreadId(Thread.currentThread());
}
static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {//本地程序计数器
    public HoldCounter initialValue() {
        return new HoldCounter();
    }
}
private transient ThreadLocalHoldCounter readHolds;
private transient HoldCounter cachedHoldCounter;//当前线程缓存的HoldCounter
private transient Thread firstReader = null;//第一个读线程
private transient int firstReaderHoldCount;
```

##### 构造方法

Sync的构造方法设置了本地线程计数器和AQS的状态

```
Sync() {
readHolds = new ThreadLocalHoldCounter();
setState(getState()); // ensures visibility of readHolds
}
```

##### tryAcquire 写锁的获取

```
protected final boolean tryAcquire(int acquires) {
    Thread current = Thread.currentThread();
    int c = getState();
    int w = exclusiveCount(c);//写锁的重入数
    if (c != 0) {//已经有其他线程获取了读锁或者写锁
        //1.写锁状态为0，说明读锁已被获取 2.写锁不为0，而且获取写锁的不是当前线程
        if (w == 0 || current != getExclusiveOwnerThread())
            return false;
        //判断统一线程获取写锁是否超过最大数
        if (w + exclusiveCount(acquires) > MAX_COUNT)
            throw new Error("Maximum lock count exceeded");
        setState(c + acquires);//当前线程已有写锁，设置写锁的数量（重入）
        return true;
    }
    if (writerShouldBlock() || //是否被阻塞
        !compareAndSetState(c, c + acquires))//获取锁
        return false;
    setExclusiveOwnerThread(current);
    return true;
}
```
写锁的获取：
1. 获取state的状态，如果不为0说明已经被获取过锁了；
    - 如果读锁被获取（写锁为0）或者 写锁被获取 但不是当前线程，返回false。
    - 如果写锁是当前线程获取的，判断是不是超过写锁的最大数，没有就设置现在写锁的个数，返回true。
2. 如果state为0，就代表还没有线程获取到读锁和写锁，此时判断writerShouldBlock是否该被阻塞，如果不被阻塞，设置state也成功，就把当前线程设置为owner。

##### tryRelease 写锁的释放
```
protected final boolean tryRelease(int releases) {
    if (!isHeldExclusively())//检查是不是当前线程
        throw new IllegalMonitorStateException();
    int nextc = getState() - releases;//新占用数
    boolean free = exclusiveCount(nextc) == 0;//独占模式重入数为0，代表独占模式释放了
    if (free)
        setExclusiveOwnerThread(null);//把owner设置为空
    setState(nextc);
    return free;
}
```
- 先检查此线程是不是持有读锁的线程，不是就抛异常。
- 然后检查释放后写锁的占用数是不是0，如果为0则表示写锁释放，释放锁资源将锁的持有线程设置为null，否则仅仅修改state。

##### tryAcquireShared 读锁的获取

```
protected final int tryAcquireShared(int unused) {
    Thread current = Thread.currentThread();
    int c = getState();
    //有写锁占用，并且不是当前线程
    if (exclusiveCount(c) != 0 &&
        getExclusiveOwnerThread() != current)
        return -1;
    int r = sharedCount(c);//读锁数量
    if (!readerShouldBlock() &&//读锁是否需要阻塞
        r < MAX_COUNT &&//小于最大数
        compareAndSetState(c, c + SHARED_UNIT)) {//设置读锁成功
        if (r == 0) {//没有读锁，初始化
            firstReader = current;
            firstReaderHoldCount = 1;
        } else if (firstReader == current) {
            firstReaderHoldCount++;//第一个读锁线程重入
        } else {//不是第一个读锁的线程
            HoldCounter rh = cachedHoldCounter;
            // 计数器为空或者计数器的tid不为当前正在运行的线程的tid
            if (rh == null || rh.tid != getThreadId(current))
                // 获取当前线程对应的计数器
                cachedHoldCounter = rh = readHolds.get();
            else if (rh.count == 0) // 计数为0
                readHolds.set(rh);//加入到readHolds中
            rh.count++;
        }
        return 1;
    }
    return fullTryAcquireShared(current);
}
```
1. 先判断写锁不为0，并且不是当前线程，直接返回-1；
2. 然后判断如果读锁不需要阻塞，小于最大读锁的数，并且比较设置state成功：
    - 如果没有读锁，则初始化第一个读锁
    - 如果第一个读锁线程已持有读锁，进行重入
    - 如果不是第一个读锁的线程：计数器为空或者tid不是当前线程，获取当前线程的计数器；获取计数器为0，加入到readHolds中。最后计数+1

读锁不需要阻塞、小于最大读锁的数、比较设置state成功这三个条件有一个为false会执行 fullTryAcquireShared 方法

```
final int fullTryAcquireShared(Thread current) {
    HoldCounter rh = null;
    for (;;) {
        int c = getState();
        if (exclusiveCount(c) != 0) {//有写锁
            if (getExclusiveOwnerThread() != current)//不是当前线程
                return -1;
        } else if (readerShouldBlock()) {//读锁要被阻塞（公平）
            if (firstReader == current) {//当前线程是第一个读线程
            } else {
                if (rh == null) {
                    rh = cachedHoldCounter;
                    if (rh == null || rh.tid != getThreadId(current)) {
                        rh = readHolds.get();//获取到当前线程的计数器
                        if (rh.count == 0)
                            readHolds.remove();
                    }
                }
                if (rh.count == 0)
                    return -1;
            }
        }
        if (sharedCount(c) == MAX_COUNT)//读锁是最大值了
            throw new Error("Maximum lock count exceeded");
        if (compareAndSetState(c, c + SHARED_UNIT)) {//设置读锁成功
            if (sharedCount(c) == 0) {//没有读锁初始化
                firstReader = current;
                firstReaderHoldCount = 1;
            } else if (firstReader == current) {//重入
                firstReaderHoldCount++;
            } else {
                if (rh == null)
                    rh = cachedHoldCounter;
                if (rh == null || rh.tid != getThreadId(current))
                    rh = readHolds.get();
                else if (rh.count == 0)
                    readHolds.set(rh);
                rh.count++;
                cachedHoldCounter = rh; // cache for release
            }
            return 1;
        }
    }
}
```
此方法与tryAcquireShared方法类似，保证了相关操作可以执行

##### tryReleaseShared 读锁的释放

```
protected final boolean tryReleaseShared(int unused) {
    Thread current = Thread.currentThread();
    if (firstReader == current) {//第一个读线程
        if (firstReaderHoldCount == 1)//第一个读线程重入数为1
            firstReader = null;//
        else
            firstReaderHoldCount--;//第一个读线程重入数-1
    } else {
        HoldCounter rh = cachedHoldCounter;
        if (rh == null || rh.tid != getThreadId(current))
            rh = readHolds.get();//获取到当前线程的计数器
        int count = rh.count;
        if (count <= 1) {
            readHolds.remove();
            if (count <= 0)
                throw unmatchedUnlockException();
        }
        //减少计数
        --rh.count;
    }
    for (;;) {//CAS设置state
        int c = getState();
        int nextc = c - SHARED_UNIT;
        if (compareAndSetState(c, nextc))
            return nextc == 0;
    }
}
```

1. 首先判断当前线程是否是第一个读线程，是的话，而且计数器为1，将firstReader设置为null，否则将firstReaderHoldCount-1；
2. 如不是第一个读线程，先获取当前线程的计数器，若计数器的count小于等于1，则移除当前线程的计数器，如果计数器的计数count小于等于0，则抛出异常，之后再减少计数即可。
3. CAS设置state

#### NonfairSync（非公平模式）
```
static final class NonfairSync extends Sync {
    private static final long serialVersionUID = -8159625535654395037L;
    final boolean writerShouldBlock() {
        return false; // writers can always barge
    }
    final boolean readerShouldBlock() {
        return apparentlyFirstQueuedIsExclusive();
    }
}
```

非公平模式的NonfairSync也是继承了Sync，实现了Sync的writerShouldBlock 和 readerShouldBlock 方法：

- writerShouldBlock：非公平模式下，写锁不需要被阻塞。
- readerShouldBlock：调用apparentlyFirstQueuedIsExclusive，如果队列中第一个节点是独占式，则返回true，堵塞读锁。

> 如果同步队列中的第一个线程是以独占模式获取锁（写锁），那么当前获取读锁的线程需要阻塞，让队列中的第一个线程先执行。

#### FairSync（公平模式）

```
static final class FairSync extends Sync {
    private static final long serialVersionUID = -2274990926593161451L;
    final boolean writerShouldBlock() {
        return hasQueuedPredecessors();
    }
    final boolean readerShouldBlock() {
        return hasQueuedPredecessors();
    }
}
```
公平模式下的实现了Sync的writerShouldBlock 和 readerShouldBlock 方法实现都一样：判断是否是等待队列中是否有前置节点，有则返回true。

#### ReadLock 和 WriteLock

ReentrantReadWriteLock中的ReadLock和WriteLock内部的Sync都是使用的ReentrantReadWriteLock的Sync，读锁和写锁的实现也都是通过调用Sync的方法来实现的，具体不就看了。

