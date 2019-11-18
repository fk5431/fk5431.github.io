---
title: aqs源码 
date: 2019-11-18 21:33:08
comments: false
tags: 
- java
categories: 
- java
keywords: aqs, AbstractQueuedSynchronizer，锁，同步
---

### aqs

AQS（AbstractQueuedSynchronizer）是JAVA中众多锁以及并发工具的基础，其底层采用乐观锁，大量使用了CAS操作， 并且在冲突时，采用自旋方式重试，以实现轻量级和高效地获取锁。

AQS虽然被定义为抽象类，但事实上它并不包含任何抽象方法。AQS是被设计为支持多种用途，如果定义抽象方法，子类在继承的时候就需要实现所有抽象方法，所以AQS将需要子类覆盖的方法都设计为protect方法，默认抛出UnsupportedOperationException异常，。如果子类用到这些方法就必须重写，否则会抛出异常，如果没有用到则不需要做任何操作。

AbstractQueuedSynchronizer只继承了AbstractOwnableSynchronizer，实现了java.io.Serializable接口。

> AbstractOwnableSynchronizer类是一种同步器，这个类仅有set和get独占线程资源。
> ```
> public abstract class AbstractOwnableSynchronizer
>      implements java.io.Serializable {
>  
>      private static final long serialVersionUID = 3737899427754241961L;
>  
>      protected AbstractOwnableSynchronizer() { }
>
>      private transient Thread exclusiveOwnerThread;
> 
>      protected final void setExclusiveOwnerThread(Thread t) {
>          exclusiveOwnerThread = t;
>      }
>
>      protected final Thread getExclusiveOwnerThread() {
>          return exclusiveOwnerThread;
>      }
>  }
> ```
> exclusiveOwnerThread 即独占资源的线程。

#### AQS原理

AQS维护了一个state变量和node双向链表。

state是已获取资源占有许可的数量。例如线程调用acquire(1)获取资源的许可，acquire会调用一次tryAcquire(1)获取资源。如果获取成功，则state加1并且调用父类的设置独占线程将当前线程设置为独占线程。如果获取失败，则说明已经有线程占用了这个资源，需要等待占用释放。此时将该线程封装成node节点，加入双向链表，，之后Locksupport.pack()堵塞当前线程。如果这个线程被唤醒则继续循环调用tryAcquire获取许可，如果获取到了将自己的node节点设置为链表的头结点并把之前的头结点去掉。如果线程释放资源，调用release方法，release方法会调用tryRelease方法尝试释放资源,如果释放成功，则state减1，再调用AQS的父类AbstractOwnableSynchronizer的设置独占线程为null，再locksupport.unpack()双向node链表的头node节点的线程，恢复其执行。

#### 源码

##### 成员变量

```
private transient volatile Node head;

private transient volatile Node tail;

private volatile int state;

private static final Unsafe unsafe = Unsafe.getUnsafe();

private static final long stateOffset;
private static final long headOffset;
private static final long tailOffset;
private static final long waitStatusOffset;
private static final long nextOffset;

static {
    try {
        stateOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
        headOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
        tailOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
        waitStatusOffset = unsafe.objectFieldOffset
            (Node.class.getDeclaredField("waitStatus"));
        nextOffset = unsafe.objectFieldOffset
            (Node.class.getDeclaredField("next"));

    } catch (Exception ex) { throw new Error(ex); }
}
```

有一个头尾节点和state变量，实现CAS的Unsafe的工具类，还有一些偏移量，都用于Unsafe的CAS操作，通过静态代码块进行初始化，通过objectFieldOffset获取对应字段相对于该对象的起始地址的偏移量。

##### Node节点

```
static final class Node {
    //共享模式
    static final Node SHARED = new Node();
    //独占模式
    static final Node EXCLUSIVE = null;
    //当线程等待超时或者被中断，则取消等待
    static final int CANCELLED =  1;
    //后继节点处于等待状态，当前节点（为-1）被取消或者中断时会通知后继节点，使后继节点的线程得以运行
    static final int SIGNAL    = -1;
    //当前节点处于等待队列，节点线程等待在Condition上，当其他线程对condition执行signall方法时，等待队列转移到同步队列，加入到对同步状态的获取。
    static final int CONDITION = -2;
    //与共享模式相关，在共享模式中，该状态标识结点的线程处于可运行状态。
    static final int PROPAGATE = -3;
    //状态
    volatile int waitStatus;
    //上一个节点
    volatile Node prev;
    //下一个节点
    volatile Node next;
    //节点所代表的线程
    volatile Thread thread;
    //Node既可以作为同步队列节点使用，也可以作为Condition的等待队列节点使用
    Node nextWaiter;

    final boolean isShared() {
        return nextWaiter == SHARED;
    }

    final Node predecessor() throws NullPointerException {
        Node p = prev;
        if (p == null)
            throw new NullPointerException();
        else
            return p;
    }

    Node() {    // Used to establish initial head or SHARED marker
    }

    Node(Thread thread, Node mode) {     // Used by addWaiter
        this.nextWaiter = mode;
        this.thread = thread;
    }

    Node(Thread thread, int waitStatus) { // Used by Condition
        this.waitStatus = waitStatus;
        this.thread = thread;
    }
}
```

##### enq方法

```
private Node enq(final Node node) {
   for (;;) {
       Node t = tail;
       if (t == null) { // Must initialize
           if (compareAndSetHead(new Node()))
               tail = head;
       } else {
           node.prev = t;
           if (compareAndSetTail(t, node)) {
               t.next = node;
               return t;
           }
       }
   }
}
```

enq方法是将node加入链表，如果tail尾节点为空则必须进行初始化，如果tail不为空，则将node的前指针指向tail，通过CAS将tail的指向改为node，然后设置t.next为node，完成node插入链表尾部。

##### addWaiter方法

```
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    enq(node);
    return node;
}
```
addWaiter方法包装node节点，放入node双向链表。如果tail不为空则说明初始化过了直接将node加入链表尾部，如果为空则进行初始化再将node加入链表尾部。

##### acquireShared

```
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        doAcquireShared(arg);
}
```

###### doAcquireShared
TODO
```
private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

尝试去获取资源，如果没有获取资源返回负数，tryAcquireShared方法需要子类自己去实现，如果不实现会直接抛异常（在读写锁的Sync实现）；如果没有获取到资源加入等待队列等待获取资源。

##### unparkSuccessor方法

```
 private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next;
    //如果为空或已取消
    if (s == null || s.waitStatus > 0) {
        s = null;
        //从后向前找
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);
}
```

唤醒等待队列中下一个线程，node的waitStatus为signal或condition，则可以唤醒，先重置node的waitStatus为0（允许失败），找到下一个需要唤醒的节点唤醒。

##### doReleaseShared
```
private void doReleaseShared() {
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            //表示后继节点需要被唤醒
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;            // loop to recheck cases
                unparkSuccessor(h);
            }
            //如果后继节点暂时不需要唤醒，则把当前节点状态设置为PROPAGATE确保以后可以传递下去
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;                // loop on failed CAS
        }
        //如果头结点没有发生变化，表示设置完成，退出循环
        //如果头结点发生变化，比如说其他线程获取到了锁，为了使自己的唤醒动作可以传递，必须进行重试
        if (h == head)                   // loop if head changed
            break;
    }
}
```

##### setHeadAndPropagate

```
////两个入参，一个是当前成功获取共享锁的节点，一个就是tryAcquireShared方法的返回值，它可能大于0也可能等于0
private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head; // Record old head for check below
    //设置新的头节点
    setHead(node);
    //propagate > 0 表示调用方指明了后继节点需要被唤醒 头节点后面的节点需要被唤醒（waitStatus<0）
    if (propagate > 0 || h == null || h.waitStatus < 0) {
        Node s = node.next;
        //如果当前节点的后继节点是共享类型获取没有后继节点，则进行唤醒
        if (s == null || s.isShared())
            doReleaseShared();
    }
}
```





















