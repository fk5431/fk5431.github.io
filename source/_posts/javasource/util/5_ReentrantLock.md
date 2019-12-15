---
title: ReentrantLock阅读 
date: 2019-12-01 20:15:48
comments: false
tags: 
- java
categories: 
- java
keywords: ReentrantLock 锁 同步 公平锁 非公平锁
---

### ReentrantLock

ReentrantLock是一个互斥锁，也是一个可重入锁。ReentrantLock锁在同一时刻只能被一个线程持有，但是它可被单个线程多次获取，每获取一次AQS的state就加1。
ReentrantLock内部的实现（公平锁和非公平锁）都是基于内部Sync的实现。

### 内部结构

ReentrantLock内部定义了三个重要的内部类，Sync、FairSync、NonfairSync。

Sync继承自抽象类AbstractQueuedSynchronizer，FairSync（公平锁）和 NonfairSync（非公平锁）继承于Sync。

#### Sync

```
abstract static class Sync extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = -5179523762034025860L;

    abstract void lock();
    //非公平锁的TryAcquire
    final boolean nonfairTryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {//没有被任何线程持有
            if (compareAndSetState(0, acquires)) {//CAS 获取
                setExclusiveOwnerThread(current);//获取成功，将当前线程设置为Owner
                return true;
            }
        }
        else if (current == getExclusiveOwnerThread()) {//如果当前线程已获取锁，将state+1返回
            int nextc = c + acquires;
            if (nextc < 0) // overflow
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        return false;
    }

    protected final boolean tryRelease(int releases) {
        int c = getState() - releases;
        if (Thread.currentThread() != getExclusiveOwnerThread())//如果当前线程不是持有锁的线程抛异常
            throw new IllegalMonitorStateException();
        boolean free = false;//标明释放释放锁
        if (c == 0) {
            free = true;
            setExclusiveOwnerThread(null);
        }
        setState(c);
        return free;
    }
    //持有锁的线程是否是当前线程
    protected final boolean isHeldExclusively() {
        return getExclusiveOwnerThread() == Thread.currentThread();
    }
    //newCondition
    final ConditionObject newCondition() {
        return new ConditionObject();
    }
    //持有锁的线程
    final Thread getOwner() {
        return getState() == 0 ? null : getExclusiveOwnerThread();
    }

    final int getHoldCount() {
        return isHeldExclusively() ? getState() : 0;
    }

    final boolean isLocked() {
        return getState() != 0;
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        setState(0); // reset to unlocked state
    }
}
```

Sync是ReentrantLock实现同步控制的基础。Sync内部定义了一些方法：lock（获取锁的方法，在子类实现）、nonfairTryAcquire（非公平锁的尝试获取资源）、tryRelease（释放资源）。

#### NonfairSync

```
static final class NonfairSync extends Sync {
    private static final long serialVersionUID = 7316153563782823691L;
    
    final void lock() {
        if (compareAndSetState(0, 1))
            setExclusiveOwnerThread(Thread.currentThread());
        else
            acquire(1);
    }
    
    protected final boolean tryAcquire(int acquires) {
        return nonfairTryAcquire(acquires);
    }
}
```

NonfairSync是内部非公平锁的定义，非公平锁获取锁的流程：

1. 调用lock方法,lock方法首先会进行CAS操作,将state属性尝试设置为1,如果成功,则代表获取到锁,将exclusiveOwnerThread属性设置为当前获取锁的线程。
2. 如果线程CAS失败,则调用AQS的acquire方法,去获取锁,tryAcquire(1)是子类自己的实现,调用了nonfairTryAcquire方法。如果调用了nonfairTryAcquire方法获取锁失败，那就会吧当前线程包装为Node加入同步队列。
   
#### FairSync 

```
static final class FairSync extends Sync {
    private static final long serialVersionUID = -3000897897090466540L;

    final void lock() {
        acquire(1);
    }
    //tryAcquire方法的公平锁版本,虽然是公平的,但是不保证一定会获取锁,除非是递归调用或者是第一个节点或者是前面无等待线程
    protected final boolean tryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {
            //hasQueuedPredecessors方法判断当先线程前面是否还有等待线程，如果有等待线程,则不去竞争获取锁
            if (!hasQueuedPredecessors() &&
                compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current);
                return true;
            }
        }
        else if (current == getExclusiveOwnerThread()) {
            int nextc = c + acquires;
            if (nextc < 0)
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        return false;
    }
}
```
公平锁在加锁的时候不会先尝试去加锁，直接去调用AQS的acquire方法去获取锁，在自己定义的尝试获取资源中，如果state为0，也会先去判断当前线程前面是否还有线程等待，如果没有等待的线程才会自己获取锁，否则加入等待队列；如果是当前线程获取到了锁则state+1.

### 公平锁和非公平锁的不同

1. 公平锁能保证：老的线程排队使用锁，新线程仍然排队使用锁。 
2. 非公平锁保证：老的线程排队使用锁；但是无法保证新线程抢占已经在排队的线程的锁。