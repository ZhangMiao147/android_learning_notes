

## Collider-Unity3D 角色互动

Collider 碰撞框：用来检测物体是否发生了接触。

* Box Collder：方形碰撞框

  * Is Trigger 勾选，触发 OnTriggerEnter（开始接触），OnTriggerStay（持续接触），OntriggerExit（结束接触）三个方法。

  * 添加 Rigidbody 属性（带来物理运算），就不会触发 OnTrigger 系列方法，而是触发 还会触发 OnCollisionEnter，OnCollisionStay，OnCollisionExit 三个方法。

* Sphere Collder：圆形碰撞框

* Mesh Collder：根据物体的形状形成碰撞框

## Rigidbood简介（上）-Unity3D角色互动

* 如果不想人物穿墙，添加 Rigidbody 属性即可。而且限制绕 X 和 Y 轴旋转。

![](img/rigidbody.png)

* 加了 Rigidbody 物体会受到物理系统的控制。
  * Mass：质量
  * Dray：阻力
  * Angular Dray：转圈时受到的阻力
  * Use Gravity：受重力的影响
  * Is Kinematic：是否受运动学
  * Interpolate：差值算法，抖动才会需要用到
  * Collision Detection：碰撞检测用哪种方法，默认时 Discrete
  * Constraints：
    * Freeze Postion：冻结方向，在该方向上不受物理控制
    * Freeze Rotation：冻结角度
* Rigidbody.AddForce()：给物体施加一个力，物体自己运动起来。

## 应用 Rigidbody 实现物体旋转-Unity3D 角色互动

* Rigidbody.angularVelocity：设置一个角速度。

## NPC 的 AI - Unity3D 角色互动

* 多个 Collder，靠近就会有反应，玩家进入最近的 Collder，表示进入火力圈，就可以发火了。
* 用 tag 区分是不是玩家
* NavMesgAgent：Unity 提供的路径导航。

## 导航网格的设置-Unity 3D 角色互动

* 寻路算法，unity 提供了导航。
* 实现导航
  1. 设置导航网格，Window->NavMesh
  2. 设置 Nav Mesh Agent
  3. 调用 Nav Mesh Agent 的方法设置路径

* NavMesgAgent.SetDestination()：设置终点，会自动寻路前往。
* NavMesgAgent.Stop()：停止前往。
* NavMesgAgent.Resume()：之前停止了，现在恢复继续。

## 血量变化-Unity3D 角色互动

* Health 表示血量，GameController：控制角色死亡等。

## 简介 UI 系统--锚点

* UI 使用 Rect Transform 属性控制位置、大小等。
  * Anchors：锚点，在布局自适应上非常有用。

## 简介 UI 系统---击杀和挨揍

* 两个 button 重叠，会根据在 canvas 的顺序，在前面的先画，在后面的后画（靠下）。

### 暂停菜单

* Button-OnClick 设置按钮点击事件。

* Application.LoadLevel(0)：重新绘制界面，也就是重新开始。
* Time.timeScale：控制游戏的快进或慢放，为 0 表示静止不动。