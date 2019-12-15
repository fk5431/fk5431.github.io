---
title: 责任链模式
date: 2017-08-06 21:00:13
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式 责任链模式
---

## 责任链模式

### 介绍

在责任链模式里，很多对象由每一个对象对其下家的引用而连接起来形成一条链。请求会在这个链上传递，直到链上某一个对象可以处理这个请求。（行为型模式）

### 结构图

责任链模式的结构像下面这样：

![责任链模式](../../uploads/chainOfResponsibility/1.png)

- 抽象处理者角色（handler）：定义一个处理请求的接口，还可以定义一个后继连接（可选）。
- 具体处理者角色（ConcreteHandler）：具体处理者接到请求后，如果可以处理就处理请求，如果不能处理就把请求传给下一个继承。

### 优缺点

#### 优点

- 降低耦合度：client发送请求并不需要知道是哪个对象处理的请求。
- 责任链可简化对象的相互链接：它们仅需保持一个指向其后继者的引用，而不需保持它所有的候选接受者的引用。
- 增强给对象指派职责的灵活性： 通过改变链内的成员或者调动它们的次序，允许动态地新增或者删除责任。
- 加新的请求处理类很方便。

#### 缺点

- 不能保证请求一定被处理。
- 系统性能有一定影响，调试时候不方面，可能造成循环调用。

### DEMO

责任链模式经常用于过滤，如果要过滤一句话中的文字是，首先定义一个过滤器接口
```
package factory.pattern.chainOfResponsibility;

/**
 * Created by FK on 2017/8/6.
 */
public interface Filter {
    String doFilter(String str);
}
```
实现过滤HTML标记
```
package factory.pattern.chainOfResponsibility;

/**
 * Created by FK on 2017/8/6.
 */
public class HtmlFilter implements Filter {
    @Override
    public String doFilter(String str) {
        String r = str;
        //过滤msg中的HTML标记
        r = r.replace("<", "&lt;").replace(">", "&gt;");
        return r;
    }
}
```
实现敏感词过滤
```
package factory.pattern.chainOfResponsibility;

/**
 * Created by FK on 2017/8/6.
 */
public class SensitiveFilter implements  Filter {
    @Override
    public String doFilter(String str) {
        String r = str;
        //过滤敏感词
        r = r.replace("敏感", "").replace("被就业", "就业");
        return r;
    }
}
```
控制整个链
```
package factory.pattern.chainOfResponsibility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FK on 2017/8/6.
 */
public class FilterChain implements Filter {
    public List<Filter> filters = new ArrayList<>();

    public FilterChain addFilter(Filter f){
        filters.add(f);
        return this;
    }

    @Override
    public String doFilter(String str) {
        String r = str;
        for(Filter f : filters){
            r = f.doFilter(r);
        }
        return r;
    }
}
```
测试
```
package factory.pattern.chainOfResponsibility;

/**
 * Created by FK on 2017/8/6.
 */
public class Test {
    public static void main(String[] args) {
        //需要被过滤的语句
        String str = "被就业了：），敏感信息，<script>";

        //搞一个过过滤链
        FilterChain chain = new FilterChain();
        chain.addFilter(new HtmlFilter()).addFilter(new SensitiveFilter());
        String s = chain.doFilter(str);
        System.out.println(s);
    }
}
```