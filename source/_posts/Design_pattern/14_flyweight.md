---
title: 享元模式
date: 2017-08-01 21:45:44
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式 享元模式
---

## 享元模式

### 介绍

享元模式使用共享来避免大量拥有相同内容对象的开销，避免内存的损耗。享元对象能做到共享的关键是区分**内蕴状态**和**外蕴状态**。
内蕴状态是存储在享元对象内部的，不会随环境的改变而有所不同（内蕴状态可以共享）。
外蕴状态是随环境的改变而改变的，不可以共享，外蕴状态不可以影响享元对象的内蕴状态，它们是相互独立的。

#### 优点

- 大幅度地降低内存中对象的数量

#### 缺点

- 使得系统更加复杂
- 享元模式将享元对象的状态外部化，而读取外部状态使得运行时间稍微变长

### 结构(涉及角色)

- Flyweight：抽象享元角色，规定具体享元角色要实现的方法。
- ConcreteFlyweight：具体享元角色，实现抽象享元规定的接口。
- FlyweightFactory：享元工厂角色，负责创建和管理享元角色。

### DEMO

抽象享元角色，抽象享元中接受一个参数state，state是外蕴状态，由外部传入的不可被共享。

```
package factory.pattern.flyweight;

/**
 * Created by FK on 2017/8/1.
 */
public interface FlyWeight {
    public void operation(String state);
}
```

具体享元角色，内部intrinsticState是内蕴状态，内蕴状态在被对象创建后就不会在改变了。

```
package factory.pattern.flyweight;

/**
 * Created by FK on 2017/8/1.
 */
public class ConcreteFlyWeight implements FlyWeight {
    private Character intrinsicState = null;

    public ConcreteFlyWeight(Character intrinsicState) {
        this.intrinsicState = intrinsicState;
    }

    @Override
    public void operation(String state) {
        System.out.println("Intrinsic State = " + this.intrinsicState);
        System.out.println("Extrinsic State = " + state);
    }
}
```

享元工厂类（客户端不可以直接将具体享元类实例化， 必须通过一个工厂对象得到享元对象）

```
package factory.pattern.flyweight;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by FK on 2017/8/1.
 */
public class FlyWeightFactory {
    private Map<Character, FlyWeight> files = new HashMap<>();
    //享元工厂在系统中只有一个，可以使用单利模式来获取    
    private static FlyWeightFactory flyWeightFactory = new FlyWeightFactory();
    private FlyWeightFactory(){}
    public static FlyWeightFactory getInstance(){
        return flyWeightFactory;        
    }
    
    public FlyWeight factory(Character state){
        FlyWeight fly = files.get(state);
        if(fly == null){
            fly = new ConcreteFlyWeight(state);
            files.put(state, fly);
        }
        return fly;
    }

}
```

测试：
```
package factory.pattern.flyweight;

/**
 * Created by FK on 2017/8/1.
 */
public class Test {
    public static void main(String[] args) {
        FlyWeightFactory factory = FlyWeightFactory.getInstance();

        FlyWeight fly = factory.factory(new Character('a'));
        fly.operation("first call");

        fly = factory.factory(new Character('b'));
        fly.operation("Second Call");

        fly = factory.factory(new Character('a'));
        fly.operation("Third Call");
    }

}
```
