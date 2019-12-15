---
title: 组合模式
date: 2017-07-29 18:25:22
tags: 
- 设计模式
categories: 
- 设计模式
keywords: 设计模式 组合模式
---
## 组合模式

### 介绍

组合模式将对象组合成树形结构表示“部分-整体”的层次结构，组合模式使用户对单个对象和组合对象的使用具有一致性。（结构型）
就像文件系统一样，文件由目录和文件组成，每个目录都可以装载目录，目录的内容既可以是文件，也可以是目录。计算机的文件系统就是以递归结构来组织的，这种情景就适用于组合模式。

### 使用场景和涉及的角色

#### 适用性

1. 你想表示对象的部分-整体层次结构
2. 你希望用户忽略组合对象与单个对象的不同，用户将统一地使用组合结构中的所有对象。

#### 涉及角色

1. Component 是组合中的对象声明接口，在适当的情况下，实现所有类共有接口的默认行为。声明一个接口用于访问和管理Component子部件。
2. Leaf 在组合中表示叶子结点对象，叶子结点没有子结点。
3. Composite 定义有枝节点行为，用来存储子部件，在Component接口中实现与子部件有关操作，如增加(add)和删除(remove)等。


#### DEMO

```
package factory.pattern.composite.test;

/**
 * Created by FK on 2017/7/30.
 */
public abstract class Component {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Component(){}

    public Component(String name) {
        this.name = name;
    }

    protected abstract void add(Component company);

    protected abstract void remove(Component company);

    protected abstract void display(int depth);


}
```

枝节点行为，具体目录类
```
package factory.pattern.composite.test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FK on 2017/7/30.
 */
public class CatalogComponent extends Component {

    List<Component> list;

    public List<Component> getList() {
        return list;
    }

    public void setList(List<Component> list) {
        this.list = list;
    }

    public CatalogComponent() {
        list = new ArrayList<>();
    }

    public CatalogComponent(String name) {
        super(name);
        list = new ArrayList<>();
    }

    @Override
    protected void add(Component company) {
        list.add(company);
    }

    @Override
    protected void remove(Component company) {
        list.remove(company);
    }

    @Override
    protected void display(int depth) {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<depth;i++){
            sb.append("-");
        }
        System.out.println(new String(sb) + this.getName());
        for(Component c : list){
            c.display(depth + 2);
        }
    }
}
```

叶子结点对象,文件
```
package factory.pattern.composite.test;

/**
 * Created by FK on 2017/7/30.
 */
public class Leaf extends Component {
    public Leaf() {
        super();
    }

    public Leaf(String name) {
        super(name);
    }

    @Override
    protected void add(Component company) {

    }

    @Override
    protected void remove(Component company) {

    }

    @Override
    protected void display(int depth) {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<depth;i++){
            sb.append("-");
        }
        System.out.println(new String(sb) + this.getName());
    }
}
```
测试类
```
package factory.pattern.composite.test;

/**
 * Created by FK on 2017/7/30.
 */
public class Test {
    public static void main(String[] args) {
        Component c = new CatalogComponent();
        Component c1 = new CatalogComponent("一级目录1");
        Component c2 = new CatalogComponent("二级目录1");
        c2.add(new Leaf("文件1"));
        c1.add(c2);
        c.add(c1);
        c.add(new CatalogComponent("一级目录2"));
        c.display(0);
    }
}
```


