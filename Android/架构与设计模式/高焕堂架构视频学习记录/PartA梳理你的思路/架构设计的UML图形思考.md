# 架构设计的 UML 图形思考

## 1. 建模与图形思考

架构师（Architect）的职责就是创意设计与人际沟通。

身为架构师，其图形绘制和思考能力愈好，其创意设计与任意沟通能力就愈好。因此，培养 Android 架构师的图形思考能力是极为重要的。

## 2. UML 软件图形语言和工具

### 2.1. 图形思考的表达：图形语言

* 图形建模：模型（Model）内含一组基本概念，及其间之关系；如果以图形表示出来，就能发挥图形思考的效益了。
* UML 图形语言，协助架构师发挥其图形思考，表达出系统架构（Architecture）的模型。
* 由于人们对这些图形元素有了共同的认知，所以系统架构之描述（即模型）也就成为人与人之间可以认知和理解的东西。因之，人与人之间采用共同的（图形）模型时，就易于沟通，易于互相合作了。

### 2.2. 软件：UML 是一种图形语言

UML 是个很好的图形语言，也有很多 UML 建模工具，对于 Android 架构师或开发者来说，都是非常重要的。无论在培养图形思考或团队沟通上，对于 Android 软件创意或管理上，是无可取代的。

### 2.3. UML 的建模工具

Astah Professional（原名 JUDE）是 UML 建模工具中，最具有简洁设计、轻便简单、易学好用的。Astah 功能枪法，支持 UML2.x 中的图表（Diagram），包括：

* Class Diagram(类别图) 
* Use Case Diagram(用例图) 
* Statemachine Diagram(状态机图) 
* Sequence Diagram(顺序图) 
* Activity Diagram(活动图) 
* Communication Diagram(通信图) 
* Component Diagram(模块图) 
* Deployment Diagram(布署图) 
* Composite Structure Diagram(组合结构图)

## 3. 绘制 UML 类别图：表达 < 基类/子类 >

图文对照，能有效培养架构师的图形思考和创意，提升架构师与项目经理、业主的沟通能力。在图形上，也能增加美感，培养架构师对软硬件的感觉（Feeling），而不是只能逻辑的理解（Understanding），有助于与设计师进行创意交流。

## 4. 绘制 UML 类别图：表达接口（Interface）

对于架构师而言，【接口】（Interface）的角色比【类别】（Class）来得重要多了。

## 5. 演练：UML 的类别与接口

 ### 5.1. 接口的表示

在 OOP 里，将接口定义为一种特殊的类别（Class）。

如果一个类别的某些函数是抽象函数的话，就称为【抽象函数】（Abstract Class）。如果一个抽象类别，它的所有的函数全部都是抽象函数的话，就称为【纯粹抽象类别】（Pure Abstract Class）；这种类别又称为【接口】（Interface）。

