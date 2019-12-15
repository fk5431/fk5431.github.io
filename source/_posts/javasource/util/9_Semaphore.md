---
title: Semaphore阅读 
date: 
comments: false
tags: 
- java
categories: 
- java
keywords: Semaphore 锁 同步
---

### Semaphore

Semaphore是一个计数信号量，必须由获取它的线程释放。常用于限制可以访问某些资源的线程数量，利用Semaphore限流。

Semaphore有两种模式，公平模式和非公平模式。公平模式就是调用acquire的顺序就是获取许可证的顺序，遵循FIFO;而非公平锁是抢占式的，有可能一个新的线程获取许可证的时候刚刚有释放的许可，但是还有正在等待许可的线程还在等待。

Semaphore内部主要是通过Sync继承了AQS来实现信号量的功能，内部工作流程还是基于AQS。

### 构造函数

```
public Semaphore(int permits) {
    sync = new NonfairSync(permits);
}

public Semaphore(int permits, boolean fair) {
    sync = fair ? new FairSync(permits) : new NonfairSync(permits);
}
```

Semaphore的构造函数有两个：
- Semaphore(int permits) ：提供了一个许可数量的入参，默认给内部Sync的是非公平方式。
- Semaphore(int permits, boolean fair) ：也需要提供提个许可数量的入参，fair来指定是公平模式还是非公平模式。

### Sync

```
abstract static class Sync extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = 1192457210091910933L;
    //设置状态数
    Sync(int permits) {
        setState(permits);
    }
    //获取许可数
    final int getPermits() {
        return getState();
    }
    //非公平模式下的获取
    final int nonfairTryAcquireShared(int acquires) {
        for (;;) {
            int available = getState();
            int remaining = available - acquires;
            if (remaining < 0 ||
                compareAndSetState(available, remaining))//如果许可小于0 或者 设置成功
                return remaining;
        }
    }
    //共享模式下释放
    protected final boolean tryReleaseShared(int releases) {
        for (;;) {
            int current = getState();
            int next = current + releases;//可用的许可
            if (next < current) // overflow
                throw new Error("Maximum permit count exceeded");
            if (compareAndSetState(current, next))//设置成功返回true
                return true;
        }
    }
    //减少许可的数据
    final void reducePermits(int reductions) {
        for (;;) {
            int current = getState();
            int next = current - reductions;
            if (next > current) // underflow  不能大于当前许可的数量
                throw new Error("Permit count underflow");
            if (compareAndSetState(current, next))
                return;
        }
    }
    //获取返回所有可用的许可
    final int drainPermits() {
        for (;;) {
            int current = getState();
            if (current == 0 || compareAndSetState(current, 0))
                return current;
        }
    }
}
```

Sync定义了非公平模式的获取 和 释放许可的方法，还定义了减少许可】获取所有许可的方法。内部NonfairSync和FairSync都继承于Sync。

### NonfairSync和FairSync

```
static final class NonfairSync extends Sync {
    private static final long serialVersionUID = -2694183684443567898L;

    NonfairSync(int permits) {
        super(permits);
    }

    protected int tryAcquireShared(int acquires) {
        return nonfairTryAcquireShared(acquires);//直接调用Sync的nonfairTryAcquireShared
    }
}
```

> NonfairSync类继承了Sync类，表示采用非公平策略获取资源，其只有一个tryAcquireShared方法，重写了AQS的该方法，直接调用了父类的nonfairTryAcquireShared。　

```
static final class FairSync extends Sync {
    private static final long serialVersionUID = 2014338818796000944L;

    FairSync(int permits) {
        super(permits);
    }

    protected int tryAcquireShared(int acquires) {
        for (;;) {
            if (hasQueuedPredecessors())//如果前面有其他节点在等待，返回-1
                return -1;
            int available = getState();
            int remaining = available - acquires;//剩余的许可
            if (remaining < 0 ||
                compareAndSetState(available, remaining))//剩余的许可小于0或者比较设置成功
                return remaining;
        }
    }
}
```

> FairSync类继承了Sync类，表示采用公平策略获取资源，使用公平策略来获取资源，会判断同步队列中是否存在其他的等待节点。

### Semaphore的核心方法

#### acquire

此方法从信号量获取一个（多个）许可，在提供一个许可前一直将线程阻塞，或者线程被中断。

```
public void acquire() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
}
```

该方法调用了AQS的acquireSharedInterruptibly，这个方法会先去执行tryAcquireShared去获取资源，如果返回的小于0，就是执行AQS的doAcquireSharedInterruptibly，直接加入阻塞队列，如果前面没有节点（pre是head），会尝试去获取，否则就进入阻塞。

#### release

release方法释放指定数量的许可。

```
public void release() {
    sync.releaseShared(1);
}
```

release方法也是直接调用了AQS的releaseShared，在释放许可成功后，会唤醒其他节点。


