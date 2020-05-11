---
title: 返回倒数第 k 个节点
date: 2020-05-05 23:28
comments: false
tags: 
- java
- lc
categories: 
- java
- lc
keywords:
- lc
---

### 返回倒数第 k 个节点

实现一种算法，找出单向链表中倒数第 k 个节点。返回该节点的值。

注意：本题相对原题稍作改动

示例：

输入： 1->2->3->4->5 和 k = 2
输出： 4
说明：

给定的 k 保证是有效的。

- 最简单的，要知道整个链表的长度，那么倒数第K个节点即第 len - k + 1 个节点

>    public int kthToLast(ListNode head, int k) {
>        int len = 0;
>        ListNode node = head;
>        while (node != null) {
>            len++;
>            node = node.next;
>        }
>        for (int i = 1; i < len - k + 1; i++) {
>            head = head.next;
>        }
>        return head.val;
>    }

    最差时间复杂度就是 2N
    
- 快慢指针，让第一个指针先走K步，然后此时 第一个指针在第K个节点，第二个指针在head，两个指针一起往后走，第一个指针到末尾 返回第二个指针的节点值

>    public int kthToLast1(ListNode head, int k) {
>        ListNode first = head;
>        ListNode sec = head;
>        for (int i = 0; i < k; i++) {
>            first = first.next;
>        }
>        while (first != null) {
>            first = first.next;
>            sec = sec.next;
>        }
>        return sec.val;
>    }

    时间复杂度 N