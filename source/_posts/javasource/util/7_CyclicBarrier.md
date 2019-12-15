---
title: CyclicBarrier阅读 
date: 2019-12-04 22:17:53
comments: false
tags: 
- java
categories: 
- java
keywords: CyclicBarrier 锁 同步
---

### CyclicBarrier

CyclicBarrier我们叫做栅栏，其实与CountDownLatch的功能类似（CyclicBarrier也可实现CountDownLatch的功能），都是等待多个线程执行完成之后在进行最后的动作，只不过CountDownLatch是一次性的，而CyclicBarrier是可循环的

> CyclicBarrier可以使一定数量的线程反复地在栅栏位置处汇集。当线程到达栅栏位置时将调用await方法，这个方法将阻塞直到所有线程都到达栅栏位置。如果所有线程都到达栅栏位置，那么栅栏将打开，此时所有的线程都将被释放，而栅栏将被重置以便下次使用。

### 源码

CyclicBarrier内部主要使用了ReentrantLock和Condition两个类。

#### 构造函数

```
public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException();
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
}

public CyclicBarrier(int parties) {
    this(parties, null);
}
```

CyclicBarrier默认的构造函数设置了栅栏拦截的线程数量，每个线程使用await方法告诉CyclicBarrier到达了栅栏，然后当前线程被阻塞。
另一个构造函数的第二个参数代表所有线程都到达栅栏时，优先执行的动作（barrierAction）。

#### await

```
public int await() throws InterruptedException, BrokenBarrierException {
    try {
        return dowait(false, 0L);
    } catch (TimeoutException toe) {
        throw new Error(toe); // cannot happen
    }
}
public int await(long timeout, TimeUnit unit)
    throws InterruptedException,
           BrokenBarrierException,
           TimeoutException {
    return dowait(true, unit.toNanos(timeout));
}
```

await方法有两个，默认的没有设置超时等待，第二个设置了超市等待的时间，两个方法最终都是调用的dowait来完成的。

```
private int dowait(boolean timed, long nanos)
    throws InterruptedException, BrokenBarrierException,
           TimeoutException {
    final ReentrantLock lock = this.lock;
    lock.lock();//独占式加锁
    try {
        final Generation g = generation;
        if (g.broken)//broken为true抛异常，默认为false
            throw new BrokenBarrierException();
        if (Thread.interrupted()) {//线程中断了
            breakBarrier();//broken为true 通知阻塞在栅栏上的其他线程
            throw new InterruptedException();
        }
        int index = --count;//获取下标
        if (index == 0) {  // tripped  如果是0，则说明最后一个线程调用了此方法
            boolean ranAction = false;
            try {
                final Runnable command = barrierCommand;
                if (command != null)
                    command.run(); //如果command不为空 就先执行 最后的动作
                ranAction = true;//执行完
                nextGeneration();//唤醒所有阻塞的线程并重新初始化
                return 0;
            } finally {
                if (!ranAction)//如果执行栅栏任务的时候失败了，就将broken设置为true
                    breakBarrier();
            }
        }
        for (;;) {//最后一个线程还没执行到
            try {
                if (!timed)//没有时间限制 则直接阻塞等待，直到被唤醒
                    trip.await();
                else if (nanos > 0L)
                    nanos = trip.awaitNanos(nanos);//阻塞指定时间
            } catch (InterruptedException ie) {
                if (g == generation && ! g.broken) {//当前代broken是false，设置broken为false，抛异常，代表此待结束
                    breakBarrier();
                    throw ie;
                } else {
                    Thread.currentThread().interrupt();//不是当前代，中断标记
                }
            }
            if (g.broken)// 当有任何一个线程中断了，会唤醒其他线程，剩余线程也要跑异常
                throw new BrokenBarrierException();

            if (g != generation)//换代 （因为一个线程可以使用多个栅栏，当别的栅栏唤醒了这个线程，就会走到这里，所以需要判断是否是当前代。）
                return index;
            // 如果有时间限制，且时间小于等于0，销毁栅栏并抛出异常
            if (timed && nanos <= 0L) {
                breakBarrier();
                throw new TimeoutException();
            }
        }
    } finally {
        lock.unlock();
    }
}
```

如果该线程不是最后一个调用await方法的线程，则它会一直处于等待状态，除非发生以下情况：

- 最后一个线程到达，即index == 0
- 某个参与线程等待超时
- 某个参与线程被中断
- 调用了CyclicBarrier的reset()方法。重新初始化栅栏

### 总结

- CyclicBarrier使用ReentrantLock独占锁来执行await，并发性会受影响。
- 如果等待过程，线程被中断了就会抛异常。
- 如果线程被其他的CyclicBarrier线程唤醒，g肯定是当前代，循环阻塞。否则是被当前CyclicBarrier唤醒的，返回下标完成一次全抵达栅栏的过程。
