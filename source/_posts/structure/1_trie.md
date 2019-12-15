---
title: 字典树
date: 2019-12-06 15:45:38
comments: false
tags: 
- structure
categories: 
- structure
keywords: structure trie 字典数
---

### 字典树

字典树又称单词查找树，是一种树形的结构，是一种哈希数的变种。典型应用是用于统计、排序和保存大量的字符串，经常被用于文本词频的统计。它的优点是：利用字符串的公共前缀来减少查询时间，最大限度地减少无谓的字符串比较，查询效率比哈希树高。

![字典树](../../../uploads/structure/trie.png)

#### 基本性质

1. 根节点不包含字符，除根节点意外每个节点只包含一个字符。

2. 从根节点到某一个节点，路径上经过的字符连接起来，为该节点对应的字符串。

3. 每个节点的所有子节点包含的字符串不相同。

#### 实现

```
public class Trie {

    private static class Node {
        public Map<Character, Node> next;
        public boolean isWord;
        public int count;

        public Node() {
            next = new TreeMap<>();
            isWord = true;
            count = 0;
        }

    }

    private Node root;

    public Trie() {
        root = new Node();
    }

    public void addWord(String word) {
        char[] chars = word.toCharArray();
        Node current = root;
        for (char c : chars) {
            Node next = current.next.get(c);
            if (next == null) {
                current.next.put(c, new Node());
            }
            current = current.next.get(c);
        }
        if (!current.isWord) {
            current.isWord = true;
        }
        current.count++;
    }

    public int query(String str) {
        Node current = root;
        if (root == null) {
            return 0;
        }
        char[] chars = str.toCharArray();
        for (char c : chars) {
            Node next = current.next.get(c);
            if (next == null) {
                return 0;
            }
            current = next;
        }
        return current.count;
    }

    public boolean search(String str) {
        Node current = root;
        if (root == null) {
            return false;
        }
        char[] chars = str.toCharArray();
        for (char c : chars) {
            Node next = current.next.get(c);
            if (next == null) {
                return false;
            }
            current = next;
        }
        return current.isWord;
    }


    public static void main(String[] args) {
        Trie trie = new Trie();
        trie.addWord("abc");
        trie.addWord("abc");
        System.out.println(trie.query("abc"));
    }
}
```

字典树一般仅进行插入操作，然后去判断某个字段串是否存在，或者这个字符串存在的次数。