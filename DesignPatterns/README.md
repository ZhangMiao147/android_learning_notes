# 设计模式
* [UML类图](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F.md)

## 创建型模式
　　创建型模式（Creational Pattern）对类的实例化过程进行了抽象，能够将软件模块中对象的创建和对象的使用分离。为了使软件的结构更加清晰，外界对于这些对象只需要知道他们公共的结构，而不清楚其具体的实现细节，使整个系统的设计更加符合单一职责原则。
　　创建型模式在创建什么（What）、由谁创建（Who）、何时创建（When）等方面都为软件设计者提供了尽可能大的灵活性。创建型模式隐藏了类的实例的创建细节，通过隐藏对象如何被创建和组合在一起达到使整个系统独立的目的。

- [简单工厂模式（Simple Factory）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E5%B7%A5%E5%8E%82%E6%A8%A1%E5%BC%8F/%E7%AE%80%E5%8D%95%E5%B7%A5%E5%8E%82%E6%A8%A1%E5%BC%8F.md)
- [工厂方法模式（Factory Method）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E5%B7%A5%E5%8E%82%E6%A8%A1%E5%BC%8F/%E5%B7%A5%E5%8E%82%E6%96%B9%E6%B3%95%E6%A8%A1%E5%BC%8F.md)
- [抽象工厂模式（Abstract Method）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E5%B7%A5%E5%8E%82%E6%A8%A1%E5%BC%8F/%E6%8A%BD%E8%B1%A1%E5%B7%A5%E5%8E%82%E6%A8%A1%E5%BC%8F.md)
- 建造者模式（Builder）
- 原型模式（Prototype）
- [单例模式（Singleton）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E5%8D%95%E4%BE%8B%E6%A8%A1%E5%BC%8F/%E5%8D%95%E4%BE%8B%E6%A8%A1%E5%BC%8F.md)

## 结构型模式
　　结构型模式（Structural Pattern）描述如何将类或者对象结合在一起行程更大的结构，就像搭积木，可以通过简单积木的组合行程复杂的、功能更为强大的结构。
　　结构型模式可以分为类结构型模式和对象结构式模式：类结构型关心类的组合，由多个类可以组合成一个更大的系统，在类结构型模式中一般只存在继承关系和实现关系；对象结构型模式关心类与对象的组合，通过关联关系使得在一个类中定义另一个类的实例对象，然后通过该对象调用其方法。根据“合成复用原则”，在系统中尽量使用关联关系来替代继承关系，因此大部分结构型模型都是对象结构型模式。

- 适配器模式（Adapter）
- 桥接模式（Bridge）
- 组合模式（Composite）
- 装饰模式（Decorator）
- 外观模式（Facade）
- 享元模式（Flyweight）
- 代理模式（Proxy）

## 行为型模式
　　行为型模式（Behavioral Pattern）是对在不同的对象之间划分责任和算法的抽象化。
　　行为型模式不仅仅关注类和对象的结构，而且重点关注它们之间的相互作用。
　　通过行为型模式，可以更加清晰地划分类与对象的职责，并研究系统在运行时实例对象之间的交互。在系统运行时，对象并不是孤立的，它们可以通过相互通信与协作完成某些复杂操作，一个对象在运行时也将影响到其他对象的运行。
　　行为型模式分为类行为型模式和对象行为型模式两种：类行为型模式使用继承关系在几个类之间分配行为，类行为型模式主要通过多态等方式来分配父类与子类的职责；对象行为型模式则使用对象的聚合关联关系来分配行为，对象行为型模式主要是通过对象关联等方式来分配两个或多个类的职责。根据“合成复用原则”，系统中要尽量使用关联关系来取代继承关系，因此大部分行为型设计模式都属于对象行为型设计模式。

- 职责链模式（Chain of Responsibility）
- 命令模式（Command）
- 解释器模式（Interpreter）
- 迭代器模式（Iterator）
- 中介者模式（Mediator）
- 备忘录模式（Memento）
- [观察者模式（Observer）](https://github.com/ZhangMiao147/android_learning_notes/blob/master/DesignPatterns/%E8%A7%82%E5%AF%9F%E8%80%85%E6%A8%A1%E5%BC%8F/%E8%A7%82%E5%AF%9F%E8%80%85%E6%A8%A1%E5%BC%8F.md)
- 状态模式（State）
- 策略模式（Strategy）
- 模板方法模式（Template Method）
- 访问者模式（Visitor）



## 参考文章
* [图说设计模式](https://design-patterns.readthedocs.io/zh_CN/latest/index.html)

