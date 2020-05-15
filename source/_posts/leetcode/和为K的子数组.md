---
title: 和为K的子数组
date: 2020-05-15 21:56
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
### 和为K的子数组

给定一个整数数组和一个整数 k，你需要找到该数组中和为 k 的连续的子数组的个数。

示例 1 :

输入:nums = [1,1,1], k = 2
输出: 2 , [1,1] 与 [1,1] 为两种不同的情况。
说明 :

数组的长度为 [1, 20,000]。
数组中元素的范围是 [-1000, 1000] ，且整数 k 的范围是 [-1e7, 1e7]。


前缀和加hash表优化

pre[i] 为[0,i]的和，所以pre[i] = pre[i-1] + num[i]
子连续数组 [j,i] 的和为K的话: pre[i] - pre[j-1] = k,因此下标j 满足 pre[j-1] = pre[i] - k



```
public int subarraySum(int[] nums, int k) {
    if(nums == null || nums.length == 0) return 0;
    int count = 0, pre = 0;
    Map<Integer, Integer> map = new HashMap<>();
    map.put(0, 1);
    for (int num : nums) {
        pre += num;
        if(map.containsKey(pre - k)) {
            count += map.get(pre - k);
        }
        map.put(pre, map.getOrDefault(pre-k, 0) + 1);
    }
    return count;
}
```