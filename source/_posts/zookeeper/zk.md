---
title: zk
date: 2020-01-07 21:51
comments: false
tags: 
- zookeeper
categories: 
- zookeeper
keywords: 
- zookeeper
---

### Zookeeper

Zookeeper是一个分布式协调服务，可用于服务发现、分布式锁、配置管理等。Zookeeper提供了一个类似于Linux文件系统的树形结构（轻量级的内存文件系统，适合存储少量信息，不适合存储大量文件或者大文件），同时提供了对于每个节点的监控和通知机制。

#### Zookeeper角色

Zookeeper集群是一个基于主从复制的高可用集群，每个服务器承担了三个角色中的一个：Leader、Follower、Observer

##### Leader
 
1. 一个zk集群同一时间只会有一个实际工作的Leader，它会发起并维护与各Follower及Observer间的心跳。
2. 所有的写操作必须要通过Leader完成在由Leader将写操作广播给其他服务器。只要有半数节点（不包括Observer）写入成功。该写请求就被会提交。

##### Follower

1. 一个zk集群可能同时存在多个Follower，它会响应Leader的心跳。
2. Follower可直接处理并返回客户端的读请求，同时会将写请求转发给Leader处理。
3. 并且负责在Leader处理写请求时对请求进行投票。

##### Observer

角色与Follower类似，但无投票权。zk需保证高可用和强一致性，为了支持更多的客户端，需要增加更多server；server增多，投票阶段延迟增加会影响性能；引入Observer，Observer不参与投票；Observer接收客户端的连接，并将写请求转发给Leader节点；加入更多的Observer，提高伸缩性，同时不影响吞吐率。

##### ZAB协议（Zookeeper Atomic Broadcast，ZooKeeper原子消息广播协议）

ZAB协议是zk作为保证数据一致性的核心算法，视为分布式协调服务zookeeper专门设计的一种支持崩溃恢复的原子协议。


###### 事务编号Zxid（事务请求计数器+epoch）

在ZAB协议的事务编号Zxid设计中，Zxid是一个64位的数字，其中低32位是一个简单的单调递增计数器，针对客户端每一个事务请求，计数器就会+1；高32位则代表Leader周期的epoch编号，每当选举出一个新的Leader服务器，就会从这个Leader服务器上取出其本地日志中最大的事务的Zxid，并从中读取epoch值然后+1作为新的epoch，低32为从0开始计数。
Zxid类似于RDBMS中的事务ID，用于标识一次更新操作的ID，为保证顺序性必须单调递增。

> epoch 可以理解为当前集群所处的年代或者周期，每次Leader变更之后，都会在前一个年代的基础上+1。

###### ZAB协议——恢复模式（选主）\广播模式（同步）

ZAB 协议有两种模式，分别是恢复模式（选主）和广播模式（同步）。当服务启动或者在Leader崩溃后，ZAB就进入了恢复模式，当领导者被选举出来，且大多数Server完成了和Leader的状态同步后，恢复模式就结束了，此时整个服务框架就可进入消息广播模式了。状态同步保证了Leader和Server具有相同的系统状态。

> 状态同步就是数据同步，用来保证集群中存在过半的机器能够和Leader服务器的数据状态保持一致。

当一台同样遵守ZAB协议的服务器启动后加入集群中时，如果此时集群中已经存在一个Leader服务器在负责消息广播，那么新加入的服务器就会进入数据恢复模式：找到数Leader所在的服务器，然后进行数据同步，然后一起参与到消息广播的流程中。

> ZAB 协议的消息广播过程使用的是一个原子广播协议，类似一个 二阶段提交过程。对于客户端发送的写请求，全部由 Leader 接收，Leader 将请求封装成一个事务 Proposal，将其发送给所有 Follwer ，然后，根据所有 Follwer 的反馈，如果超过半数成功响应，则执行 commit 操作（先提交自己，再发送 commit 给所有 Follwer）。


###### ZAB协议4阶段

1. Leader election（选举阶段）：节点在一开始就处于选举阶段，只要有一个节点得到超半数节点的票数，它就可以当选准Leader。只有到达广播阶段准Leader才会成为真正的Leader。这一阶段的目的是为了选出一个准Leader然后进入下一个阶段。
2. Discovery（发现阶段）：在这个节点，Follower和准Leader进行通信，同步Follower最近接收的事务提议。这一个阶段的主要目的是发现当前大多数节点接收的最新提议，并且准Leader生成新的epoch，让Follower接收并且更新它们的accepted Epoch。
    一个Follower只会链接一个Leader，如果有一个节点f认为另一个Follower p是Leader，f在尝试链接p时会被拒绝，f被拒绝之后，就会重新进入选举阶段。
3. Synchronization（同步阶段）：同步阶段主要是利用Leader前一阶段获得的最新提议历史，同步集群中所有的副本。只有当大多数节点同步完成，准Leader才会成为真正的Leader。Follower只会接受Zxid比自己lastZxid大的提议。
4. Broadcast（广播阶段）：这个阶段zk集群可以对外提供事务服务，并且Leader可以进行消息广播，如果有新的节点加入，还需要对新节点进行同步。

###### 投票机制

每个server首先给自己投票，然后用自己的选票和其他server选票的对比，权重大的胜出，使用权重较大的更新自身的选票箱：

> myid：是zk集群中服务器的唯一标识，例如3台zkserver，编号就是1、2、3，权重依次增大。

1. 每个Server启动后都询问其他的Server要投票给谁。对于其他server的询问，server每次根据自己的状态都回复自己推荐的leader的id和上一次处理事务的zxid（系统启动时每个server都会推荐自己）。
2. 收到所有server回复后，就计算出zxid最大的那个server，并将这个server相关信息设置成下一次要投票的server。(如果zxid相同，就比较myid，myid较大的作为下次投票)
3. 计算这过程中获得票数最多的server为获胜者，如果获胜者的票数超过半数，则该server被选为leader。否则继续这个过程直到leader被选举出来。
4. leader开始等待server连接。（由LOOKING状态变为LEADING状态）
5. Follower连接Leader，将最大的zxid发送给leader。（由LOOKING状态变为FOLLOWING状态）
6. Leader根据Follower的zxid确定同步点，到这里选举阶段完成。








