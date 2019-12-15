---
title: 建造者模式
date: 2017-07-16 09:35:56
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式 建造者模式
---

## 建造者模式

### 定义

将一个复杂对象的构造与它的表示分离，使同样的构建过程可以创建不同的表示，这样的设计模式被称为建造者模式。(创建型模式)

### 使用场景

1 创建一些复杂的对象时,这些对象的内部组成间的构建顺序是稳定的,但是对象的内部组成构件面临着复杂的变化。
2 要创建的复杂对象的算法,独立于该对象的组成部分,也独立与组成部分的装配方法时。 

**优点**:

1. 使用建造者模式可以让客户端不知道产品内部的组成细节.
2. 具体的建造类之间是相互独立的,对系统的扩展是非常有利的.
3. 由于具体的建造者是独立的,因此可以对建造过程逐步细化,而对其他的模块没有任何影响.

### 建造者模式一般包含的角色

1. builder: 给出一个抽象接口,以规范产品对象的各个组成成分的建造.这个接口规定要实现负责对象的哪些部分的创建,不涉及具体对象部件的创建,
2. ConcreteBuilder: 实现Builder接口,针对不同的商业逻辑,具体化复杂对象的各部分的创建.在建造过程完成后,提供产品实例.
3. Director: 调用具体建造者来创建复杂对象的各个部分,在指导者中不涉及具体产品的信息,只负责保证对象各部分完整创建或按某顺序创建.
4. Product: 要创建的负责对象.

### Demo

如果要创建一个小人,肯定要创建人的头,身体,手,脚,现在系统将人分为胖人和瘦人,那么设计如下:

首先是Persion
```
package factory.pattern.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fk5431 on 7/24/17.
 */
public class Persion {
    private List<String> parts = new ArrayList<String>();

    public void Add(String part){
        parts.add(part);
    }

    public void Show(){
        for(String part : parts){
            System.out.println(part);
        }
    }

}
```
然后是Builder
```
package factory.pattern.Builder;
   
   /**
    * Created by fk5431 on 7/24/17.
    */
   public interface Builder {
       void BuildHead();
       void BuildBody();
       void BuildHand();
       void BuildFeet();
       Persion getResult();
   }
```
然后瘦人胖人类分别实现接口
```
package factory.pattern.Builder;

/**
 * Created by fk5431 on 7/24/17.
 */
public class FatPersonBuilder implements Builder {
    private Persion product;
    public FatPersonBuilder(){
        product = new Persion();
    }
    public void BuildHead() {
        product.Add("胖人头");
    }

    public void BuildBody() {
        product.Add("胖人身体");
    }

    public void BuildHand() {
        product.Add("胖人手");
    }

    public void BuildFeet() {
        product.Add("胖人脚");
    }

    public Persion getResult() {
        return product;
    }
}
```
```
package factory.pattern.Builder;

/**
 * Created by fk5431 on 7/24/17.
 */
public class ThinPersonBuilder implements Builder {
    Persion product;
    public ThinPersonBuilder(){
        product = new Persion();
    }
    public void BuildHead() {
        product.Add("瘦人头");
    }

    public void BuildBody() {
        product.Add("瘦人身体");
    }

    public void BuildHand() {
        product.Add("瘦人手");
    }

    public void BuildFeet() {
        product.Add("瘦人脚");
    }

    public Persion getResult() {
        return product;
    }
}
```
然后是指导者
```
package factory.pattern.Builder;

/**
 * Created by fk5431 on 7/24/17.
 */
public class Director {
    public Persion Persion(Builder builder){
        builder.BuildBody();
        builder.BuildFeet();
        builder.BuildHand();
        builder.BuildHead();
        return builder.getResult();
    }
}
```
最后测试一下
```
public class Test {

    public static void main(String[] args) {
        Director d = new Director();
        Persion p = d.Persion(new ThinPersonBuilder());
        p.Show();
    }
}
```