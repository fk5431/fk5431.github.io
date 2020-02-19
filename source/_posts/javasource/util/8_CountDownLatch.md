---
title: CountDownLatch阅读 
date: 2019-12-02 21:48:17
comments: false
tags: 
- java
- CountDownLatch
- 锁
- 同步
categories: 
- java
keywords:  
- java
- CountDownLatch
- 锁
- 同步
---

### CountDownLatch

countDownLatch这个类使一个线程等待其他线程各自执行完毕后再执行。是通过一个计数器来实现的，计数器的初始值是线程的数量。每当一个线程执行完毕后，计数器的值就-1，当计数器的值为0时，表示所有线程都执行完毕，然后在闭锁上等待的线程就可以恢复工作了。

countDownLatch的实现也很简单，也是通过内部Sync实现了AQS就完成了功能。

#### Sync

```
private static final class Sync extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = 4982264981922014374L;

    Sync(int count) {
        setState(count);
    }

    int getCount() {
        return getState();
    }

    protected int tryAcquireShared(int acquires) {
        return (getState() == 0) ? 1 : -1;
    }

    protected boolean tryReleaseShared(int releases) {
        // Decrement count; signal when transition to zero
        for (;;) {
            int c = getState();
            if (c == 0)
                return false;
            int nextc = c-1;
            if (compareAndSetState(c, nextc))
                return nextc == 0;
        }
    }
}
```

Sync的构造方法就会给AQS的state设置一个数，释放资源（tryReleaseShared）的时候通过CAS操作去将state-1。

#### CountDownLatch的实现

一般我们会定义CountDownLatch的时候，new的时候会设置一个count，也就是等待count个线程完成之后，在执行的当前的线程。

```
public CountDownLatch(int count) {
    if (count < 0) throw new IllegalArgumentException("count < 0");
    this.sync = new Sync(count);
}
```

> 如果count小于当然就是报错，然后就创建了一个内部的Sync。

调用await的时候，就是将当前线程进行阻塞：
```
public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
}
```
await方式直接调用的AQS的acquireSharedInterruptibly方法，AQS中acquireSharedInterruptibly调用了doAcquireSharedInterruptibly：
```
private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

doAcquireSharedInterruptibly和其他同步阻塞方法一样，先将node包装为共享模式然后加入Waiter队列，如果node的前一个节点是头结点就尝试去获取资源，如果获取到将本节点设置为头结点然后唤醒剩余共享节点返回，否则就将此线程阻塞。


如果我们等待其他线程每完成一个就会调用countDown方法：
```
public void countDown() {
    sync.releaseShared(1);
}
```
releaseShared就相当于先执行了tryReleaseShared（先以CAS的方式将state-1），然后执行AQS的doReleaseShared方法。
doReleaseShared方法可以参考[aqs](http://fk5431.com/20191118/javasource/util/3_AbstractQueuedSynchronizer/)，就是对共享模式的node进行唤醒，如果state为0的时候，就会唤醒，此时此线程已等待其他线程执行完毕，就可以开始执行了。
