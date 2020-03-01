# RabbitMQ 知识

## 概述

　　官方地址：https://www.rabbitmq.com/

　　官方教程：https://www.rabbitmq.com/tutorials/tutorial-one-java.html

　　MQ 全称为 Message Queue，消息队列（MQ）是一种应用程序对应用程序的通信方法，也就是信息中间件。

　　RabbitMQ 则是 MQ 的一种开源实现，遵循 AMQP（Advanced Message Queue，高级消息队列协议）协议，特点是消息转发是非同步并且可靠的。

　　MQ 的模型：

![](image/MQ模型.png)

　　RabbitMQ 比 MQ 模型有更加详细的模型概念：

![](image/rabbit模型.png)

　　RabbitMQ 主要用来处理应用程序之间消息的存储与转发，可让消费者和生产者解耦，消息是基于二进制的。

　　RabbitMQ 作为一个消息代理，主要负责接收、存储和转发消息，它提供了可靠的消息机制和灵活的消息路由，并支持消息集群和分布式部署，常用于应用解耦、耗时任务队列、流量削锋等场景。

## 概念

### Broker（Server）

　　接收客户端连接，实现 AMQP 消息队列和路由功能的进程，可以把 Broker 叫做 RabbitMQ 服务器。

### Virtual Host

　　一个虚拟概念，一个 Virtual Host 里面可以有若干个 Exchange 和 Queue，主要用于权限控制，隔离应用。

### Channel 信道

　　引入信道的原因：RabbitMQ 之间使用 TCP 连接，每次发布消息都要连接 TCP，这样会导致连接资源严重浪费，从而造成服务器性能瓶颈，所以引入信道，将需要发布消息的线程都包装成一条信道在 TCP 中传输，这样 RabbitMQ 为所有的线程只用一条 TCP 连接即可。

　　仅仅创建了客户端到 Broker 之间的连接 Connection 后，客户端还是不能发送消息的，需要在 Connection 的基础上创建 Channel，AMQP 协议规定只有通过 Channel 才能执行 AMQP 的命令，一个 Connection 可以包含多个 Channel。

　　一条 TCP 连接可以支持多个信道，模型如下：

![](image/信道模型.png)

### Queue 队列

　　消息队列用来保存消息直到发送给消费者。

　　它是消息的容器，也是消息的终点。

　　一个消息可投入一个或多个队列。

　　消息一直在队列里面，等待消费者连接到这个队列将其取走。

　　队列时先进先出的，默认情况下先存储的消息先被处理。

### Message

　　就是消息，由 Header 和 Body 组成，Header 是由生产者添加的各种属性的集合，包括 Message 是否被持久化、由哪个 Message Queue 接收、优先级是多少等，Body 是真正传输的数据，内容格式为 byte[] 。

### Connection

　　连接，对于 RabbitMQ 而言，其实就是一个位于客户端和 Broker 之间 TCP 连接。

### 绑定

　　绑定用于消息队列和交换器之间的关联。

　　一个绑定就是基于路由键将交换器和消息队列连接起来的路由规则，所以可以将交换器理解成一个由绑定构成的路由表，如下：

![](image/绑定图.png)

### Exchange 交换器

　　向 RabbitMQ 发送消息，实际上是把消息发到交换器上，再由交换器根据相关路由规则发到特定队列上，在队列上监听的消费者就可以进行消费了。所以生产者发送消息时会经有交换器（Exchange）来决定要给哪个队列（Queue）。

　　接收生产者发送的消息，并根据 Binging 规则将消息路由给服务器中的队列。ExchangeType 决定了 Exchange 路由消息的行为。

　　目前 RabbitMQ 共有四种类型：direct、fanot、topic、headers。

#### direct 交换器

　　生产者传送的消息中的路由键（ routing key ）必须和 Queue Binding 中的 Binding key 一致，交换器（exchange）就将消息发到对应的队列（queue）中。它是完全匹配、单播的模式。

![](image/direct交换器.png)

#### fanout 交换器

　　每个发送到 fanout 交换器中的消息，它不会去匹配路由键，直接把消息投递到所有绑定到 fanout 交换器中的队列上，它就像一个广播站一样，它会向所有收听广播的用户发送消息。简单来说就是轮流把消息放进每个队列中。

　　对应到系统上，它允许你针对一个消息作不同操作，比如用户上传了一张新的图片，系统要同时对这个事件进行不同的操作，比如删除旧的图片缓存、增加积分奖励等等，这样旧大大降低了系统之间的耦合度。

![](image/fanout交换器.jpg)

#### topic 交换器

　　topic 交换器有点类似于 direct 交换器，但是 topic 交换器使用部分匹配比 direct 交换器多了更多弹性。

　　它通过模式匹配分配消息的路由键属性，将路由键和某个模式进行匹配，此时队列需要绑定到一个模式上。它将路由键和绑定键的字符串切分成单词，这些单词之间用点隔开。

　　它同样也会识别两个通配符：符号 “ # ” 和符号 “ * ”：

* “ # ” 匹配 0 个或多个单词。
* “ * ” 匹配不多不少一个单词。

![](image/topic交换器.png)

#### header 交换器

　　header 类型路由规则和上面的三个都不一样，header 交换器不是通过路由键（routing key）进行路由的，而是通过消息的 headers。

　　而且 headers 交换器和 direct 交换器完全一致，但性能差很多，目前几乎用不到了。

#### 交换器小结

　　RabbitMQ 的交换器（exchange）的作用是路由消息，可以根据应用场景的不同选择合适的交换机。

　　如果需要精准路由到队列，或者对消息进行单一维度分类（只对日志的严重程度这一维度进行分配）可以使用 direct 类型交换器；如果需要广播消息，可以使用 fanout 类型交换器；如果对消息进行多维度分类，可以使用 topic 交换器；如果消息归类的逻辑包含了较多的 AND/OR 逻辑判断，可以使用 header 交换器（开发中很少用到 header 交换器）。　　

　　生产者 Producer 和消费者 Consumer 都是 RabbitMQ 的客户端，Producer 负责发送消息，Consumer 负责消费消息。

## 命令

### 基本控制命令

　　基本控制命令主要用于启动、停止应用程序、runtime 等。

```java
rabbitmqctl shutdown //停止 rabbitmq 和 runtime
rabbitmqctl stop //停止 erlang 节点
rabbitmqctl start_app //启用 rabbitmq
rabbitmqctl stop_app //停止 rabbitmq
rabbitmqctl status //查看状态
rabbitmqctl environment //查看环境
rabbitmqctl reset // rabbitmq 恢复最初状态，内部的 exchange 和 queue 都清除
```

### 服务状态管理命令

　　这些命令主要用于查看 exchange、channel、binding、queue、consumers。

```java
list_queues [-p <vhostpath>] [<queueinfoitem> ...] //返回 queue 的消息
list_exchanges [-p <vhostpath>] [<exchangeinfoitem> ...] //返回 exchange 的消息
list_bindings [-p <vhostpath>] [<bindinginfoitem> ...] //返回绑定消息
list_connections [<connectionitem> ...] //返回链接信息
list_channels [<channelinfoitem> ...] //返回目前所有的 channels
list_consumers [-p <vhostpath>] // 返回 consumers
```

### 用户管理命令

　　这些命令主要用于添加、修改、删除用户及管理用户权限。

```java
add_user <username> <password> //在 rabbitmq 的内部数据库添加用户
delete_user <username> //删除一个用户
change_password <username> <newpassword> //改变用户密码
clear_password <username> //清除用户密码，禁止用户登陆
set_user_tags <username> <tag> //设置用户 tag，就是设置用户角色
list_users //查看用户列表
add_vhost <vhostpath> //创建一个 vhost
delete_vhost <vhostpath> //删除一个 vhosts
list_vhosts [<chostinfoitem> ...] //列出 vhosts
set_permissions [-p <vhostpath>] <user> <conf> <write> <read> //针对一个vhosts给用户赋予相关权限
clear_permissions [-p <vhostpath>] <username> //清除一个用户对 vhost 的权限
list_permissions [-p <vhostpath>] //列出所有用户对某一 vhost 的权限
list_user_permissions <username> //列出某用户的访问权限
```

### 集群管理命令

```java
join_cluster <clusternode> [--ram] //clusternode 表示 node 名称，--ram 表示 node 以 ram node 加入集群中。默认 node 以 disc node 加入集群，在一个 node 加入 cluster 之前，必须先停止该 node 的 rabbitmq 应用，即先执行 stop_app.
cluster_status //显示 cluster 中的所有 node
set_cluster_name <clustername> //设置集群名字
rename_cluster_node <oldname> <newname> //修改集群名字
change_cluster_node_type <disc | ram> //改变一个 cluster 中 node 的模式，该节点在转换前必须先停止，不能把一个集群中唯一的 disk node 转换为 ram node
rabbitmqctl forget_cluster_node rabbit@rabbit1 //远程删除一个节点，删除前该节点必须先停止
sync_queue <queuename> //同步镜像队列
cancel_sync_queue <queuename> //取消同步队列
purge_queue [-p vhost] <queuename> //晴空队列中所有消息
```







## 消息确认

　　在一些场合，如转账、付费时每一条消息都必须保证成功的被处理。

　　AMQP 时金融级的消息队列协议，有很高的可靠性。

　　RabbitMQ 通过消息确认来保证消息被成功处理。消息确认可以分为两种：

1. 生产者发送消息到服务（ Broker ）时，Broker 给生产者发送确认回执，用于告诉生产者消息已被成功发送到 Broker。
2. 消费者接收到 Broker 发送的消息时，消费者给 Broker 发送确认回执，用于通知消息已成功被消费者接收。

### 生产者端消息确认

　　生产者端的消息确认：当生产者将消息发送给 Broker，Broker 接收到消息给生产者发送确认回执。

　　生产者端的消息确认有两种方式：tx 机制和 confirm 模式。

#### tx 机制

　　tx 机制可以叫做事务机制，RabbitMQ 中有三个与 tx 机制的方法：txSelect()、txCommit() 和  txRollback()。channel.txSelect()  用于将当前 channel 设置成 transaction 模式，channel.txCommit() 提交事务，channel.txRollback() 回滚事务。

　　使用 tx 机制，首先要通过 txSelect 方法开启事务，然后发布消息给 broker 服务器，如果 txCommit 提交成功了，则说明消息成功被 broker 接收了，如果在 txCommit 执行之前 broker 异常奔溃或者由于其他原因抛出异常，这个时候就可以捕获异常，通过 tcRollback 回滚事务。

#### confirm 模式

　　有三个与 confirm 模式像是的方法：ConfirmSelect()、WaitForConfirms() 和 WaitForConfirmsOrDie()。channel.ConfirmSelect() 表示开启 Confirm 模式，channel.WaitForConfirms() 等待所有消息确认，如果所有的消息都被服务端成功接收返回 true，只要有一条没有被成功接收就返回 false。channel.WaitForConfirmsOrDie() 和 WaitForConfirms 作用类似，也是等待所有消息确认，区别在于该方法没有返回值（void），如果有任意一条消息没有被成功接收，还放啊会立即抛出一个 OperationInterrupedException 类型异常。



### 消费者消息确认

　　从 Broker 发送到消费者时，RabbitMQ 提供了两种消息确认的方式：自动确认和显示确认。

#### 自动确认

　　自动确认：当 RabbitMQ 将消息发送给消费者后，消费者端接收到消息后，不等待消息处理结束，立即自动回送一个确认回执。

　　自动确认的用法十分简单，设置消费放啊的参数 autoAck 为 true 即可。

　　Broker 会在接收到确认回执时删除消息，如果消费者接收到消息并返回了确认回执，然后这个消费者在处理消息时挂了，那么这条消息就再也找不回来了。

#### 显示确认

　　自动确认可能会出现消息丢失的问题，而显示确认就会避免这个问题，显示确认可以让消费者在接收消息时不立即返回确认回执，等到消息处理完成后（或者完成一部分的逻辑）再返回确认回执，这样就保证消费端不会丢失消息了！

　　使用显示确认也比较简单，首先将 resume 方法的参数 autoACK 设置为 false，然后在消费端使用代码 channel.BasicAck() / BasicReject() 等方法来确认和拒绝消息。

　　使用显示确认时，如果消费者处理完消息不发送确认回执，那么消息不会被删除，消息的状态一直是 Unacked，这条消息也不会再发送给其他消息者。如果一个消费者在处理消息时尚未发送确认回执的情况下挂掉了，那么消息会被重新放入队列（ 状态从 Unacked 变成 Ready ），有其他消费者时，消息会发送给其他消费者。

## 消息持久化与优先级

### 消息持久化（Persistent）

　　把 exchange 和 queue 的 durable 属性设置为 true，重启 RabbitMQ 服务时，exchange 和 queue 也会恢复。

　　如果 queue 设置 durable = true，RabbitMQ 服务重启后队列虽然会存在，但是队列内的消息会全部丢失。这时就会需要实现消息的持久化。

　　消息的持久化实现的方法很简单：将 exchange 和 queue 都设置 durable = true，然后在消息发布的时候设置 persistent = true 即可。

#### 消息优先级（Priority）

　　queue 时先进先出的，即先发送的消息，先被消费。设置了优先级后，优先级高的消息就会优先被消费。

　　消息实现优先级控制的实现方式是：首先在声明 queue 是设置队列的 x-max-priority 属性，然后在 publish 消息时，设置消息的优先级等级即可。x-max-priority 设置的是队列优先级的最大值。

## RabbitMQ 的消费模式

　　RabbitMQ 中的消费模式有：EventingBasicConsumer、BasicGET 和 QueueBasicConsumer。

　　QueueBasicConsumer 的用法和 Get 类似，QueueBasicConsumer 在官方 API 中标记已过时。

　　EventingBasicConsumer 是基于长连接，发布订阅模式的消费方式，节省资源且实时性好，这是开发中最常用的消费模式。在一些消费者主动获取消息的场合，可以使用 Get 方式，Get 方式时基于短连接的，请求响应模式的消费方式。

### EventingBasicConsumer 介绍

　　EventingBasicConsumer 是发布 / 订阅模式的消费者，即只要订阅的 queue 中有了新消息，Broker 就会立即把消息推送给消费者，这种模式可以保证消息及时地被消费者接收到。

　　EventingBasicConsumer 是长链接：只需要创建一个 Connection，然后在 Connection 的基础上创建通道 channel，消息的发送都是通过 channel 来执行的，这样可以减少 Connection 的创建，比较节省资源。

### BasicGet 方法介绍

　　使用 EventingBasicConsumer 可以让消费者最及时地获取到消息，使用 EventingBasicConsumer 模式时消费者在被动的接收消息，即消息是推送过来的，Broker 时自动的一方。那么能不能让消费者作为自动的一方，消费者什么时候想要消息了，就自己发送一个请求去找 Broker 要？答案是使用 Get 方式。

　　Get 方式是短连接的，消费者每次想要消息的时候，首先建立一个 Connection，发送一次消息，Broker 接收到请求后，响应一条消息给消费者，然后断开链接。

　　RabbitMQ 中 Get 方式和 HTTP 的请求响应流程基本一样，Get 方式的实时性比较差，也比较耗费资源。

　　channel.BasicGet() 一次只获取一条消息，获取到消息后就把连接断开了。

### Qos（服务质量） 介绍

　　在使用 EventingBasicConsumer 的时候，当生产者发送了 100 条消息到 Broker，消费端采用自动确认，执行生产者程序后，queue 中会有 100 条 ready 状态的消息，然后开始执行消费者，消费者执行后，Broker 会将全部消息发送过去，也就是说消费者可能还没有处理完消息，但是 queue 中的消息都已经删除了。如果在处理消息的途中消费者挂掉了，所有未处理的消息就会丢失。

　　对于上面的问题，可以使用显示确认来保证消息不会丢失：将 BasicConsume 方法的 autoAck 设置为 false，然后处理一条消息后手动确认一下，这样的话一处理的消息在接收到确认回执时被删除，未处理的消息以 Unacked 状态存放在 queue 中。如果消费者挂掉了，Unacked 状态的消息会自动重新变成 Ready 状态，如此依赖就不用担心消息丢失了。

　　通过显式确认的方式可以解决消息丢失的问题，但这种方式也存在问题：

1. 当消息上万时，一股脑的把消息发送给消费者，可能会造成消费者内存爆满；
2. 当消息处理比较慢时，单一的消费者处理这些消息可能很长时间，自然会想要再添加一个消费者加快消息的处理速度，但是这些消息都被原来的消费者接收了，状态为 Unacked，所以这些消息不回再发送给新添加的消费者。

　　对于上面的问题，RabbitMQ 提供了 Qos（服务质量）可以解决。使用 Qos 时，Broker 不会再把消息一股脑的发送给消费者，可以设置每次传输给消费者的消息条数 n，消费者把这 n 条消息处理完成后，再获取 n 条数据进行处理，这样就不用担心消息丢失、服务端内存爆满的问题了，因为没有发送的消息状态都是 Ready，所以当新增一个消费者时，消息也可以立马发送给新增的消费者。

　　Qos 只有在消费端使用显示确认时才有效，使用 Qos 的方式非常简单，在消费端调用 channel.BasicQos() 方法即可。

```java
channel.BasicQos(int prefetchSize, int prefetchCount, boolean global)
```

　　prefetchSize：表示预取的长度，一旦设置为 0 即可，表示长度不限。

　　prefetchCount：表示预取的条数，即发送的最大消息条数。

　　global：表示是否在 Connection 中全局设置，true 表示 Connection 下的所有 channel 都设置为这个位置。

　　Qos 可以设置消费者一次接收消息的最大条数，能够解决消息拥堵时造成的消费者内存爆满问题。Qos 也比较适用于好事队列，当任务队列中的任务很多时，使用 Qos 后可以随时添加新的消费者来提高任务的处理效率。

## 集群

　　搭建的普通集群中节点可以共享集群中的 exchange、routing key 和 queue，但是 queue 中的消息只保存在首次声明 queue 节点中，而任意节点的消费者都可以消费其他节点的消息。

　　因为 queue 中的消息只保存在首次声明 queue 的节点中，这样就有一个问题：如果某一个 node 节点挂掉了，那么只能等待该节点重新连接才能继续处理该节点内的消息（如果没有设置持久化的话，节点挂掉后消息会直接丢失）。

　　对于上面的问题，如果可以让 RabbitMQ 中的节点像 redis 集群的节点一样，每一个节点都保存所有的消息，这就是 RabbitMQ 的一个功能：镜像队列。镜像队列由一个 master 和多个 slave 组成，使用镜像队列消息会自动在景象节点间同步，而不是在 consumer 取数据时临时拉取。

　　而使用镜像队列，因为各个节点要同步消息，所以比较耗费资源，一般在可靠性比较高的场景使用镜像队列。 

　　RabbitMQ 的集群默认不支持负载均衡的。可以根据设备的性能，使用 Qos 给各个消费者指定合适的最大发送条数，这样可以在一定程度上实现负载均衡，也可以通过 Haproxy 实现 RabbitMQ 集群的负载均衡。为什么使用 Haproxy 而不用 Ngnix 呢？这是因为 Haproxy 支持四层（tcp、udp 等）和七层（http、https、emal 等）的负载均衡，而 Nginx 只支持七层的负载均衡，而 RabbitMQ 时通过 TCP 传输的。

## 参考文章

[快速掌握 RabbitMQ](https://www.cnblogs.com/wyy1234/p/10743567.html)