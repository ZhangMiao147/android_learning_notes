## 1. 常用命令

### 1.1 基本控制命令

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

### 1.2 服务状态管理命令

　　这些命令主要用于查看 exchange、channel、binding、queue、consumers。

```java
list_queues [-p <vhostpath>] [<queueinfoitem> ...] //返回 queue 的消息
list_exchanges [-p <vhostpath>] [<exchangeinfoitem> ...] //返回 exchange 的消息
list_bindings [-p <vhostpath>] [<bindinginfoitem> ...] //返回绑定消息
list_connections [<connectionitem> ...] //返回链接信息
list_channels [<channelinfoitem> ...] //返回目前所有的 channels
list_consumers [-p <vhostpath>] // 返回 consumers
```

### 1.3 用户管理命令

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

### 1.4 集群管理命令

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



## 2. 方法详解

### 2.1 QueueDeclare 方法详解

　　QueueDeclare 方法用来声明队列。

```java
QueueDeclareOk QueueDeclare(String queue, boolean durable,boolean exclusive,boolean autoDelete,IDictionary< String,Object > arguments)；
```

　　方法通过参数设置队列的特性。参数解析如下：

**queue**：队列名字；

**durable**：是否持久化。设置为 true 时，队列信息保存在 rabbitmq 的内置数据库中，服务器重启时队列也会恢复；

**exclusive**：是否排外。设置为 true 时只有首次声明该队列的 Connection 可以访问，其他 Connection 不能访问该队列；且在 Connection 断开时，队列会被删除（即时 duration 设置为 true 也会被删除）；

**autoDelete**：是否自动删除。设置为 true 时，表示在最后一条使用该队列的连接（Connection）断开时，将自动删除这个队列；

**arguments**：设置队列的一些其他属性，为 Dictionary< String,Object > 类型，下表总结了 argument 中可以设置的常用属性。 

| 参数名                  | 作用                                                         | 示例                                                         |
| ----------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| Message TTL             | 设置队列中消息的有效时间                                     | {"x-message-ttl",1000*8}，设置队列中的所有消息的有效期为 8s。 |
| Auto expire             | 队列能保存消息的最大条数                                     | {"x-expires",1000*6}，设置队列的过期时长为 60s，如果 60s 没有队列被访问，则删除队列。 |
| Max length              | 队列能保存消息的最大条数                                     | {"x-max-length",100}，设置队列最多保存 100 条消息。          |
| Max length bytes        | 队列中 ready 类型消息的总字节数                              | {"x-max-length-bytes",1000}，设置队列中 ready 类型消息总共不能超过 1000 字节。 |
| Overflow  behaviour     | 当队列消息满了时，再接收消息时的处理方法。有两种处理方案：默认为 “drop-head” 模式，表示从队列头部丢弃消息；"reject-publish" 表示不接收后续的消息。 | {"x-overflow","reject-publish"}，设置当前队列消息满了时，丢失传来后续消息。 |
| Dead Letter exchange    | 用于存储被丢弃的消息的交换机名。Overflow behaviour 的两种处理方案中丢弃的消息都会发送到这个交换机。 | {"x-dead-letter-exchange","beiyongExchange"}，设置丢弃的消息发送到名字为 beiyongExchange 的交换机。 |
| Dead letter routing key | 被丢弃的消息发送到 Dead letter exchange 时的使用的 routing key。 | {“x-dead-letter-routing-key”,"deadkey"}，设置丢弃的消息发送到 beiyongExchange 交换机时的 RoutingKey 值是 “deadKey”。 |
| Maximum priority        | 设置队列中消息优先级的最大等级，在 publish 消息时可以设置单条消息的优先级等级。 | {"x-max-priority",10}，设置中消息优先级的最大等级为 10。     |
| Lazy mode               | 设置队列的模式，如果设置为 Lazy 表示队列中消息尽可能存放在磁盘中，以减少内存占用；不设置时消息都存放在队列中，用以尽可能快的处理消息。 | {"x-queue-mode","lazy"}，3.6 以后版本可用，设置队列中消息尽可能存放在磁盘中，以减少内存占用。在消息拥堵时和消息持久化配置使用可以减少内存占用。 |

### 2.2 ExchangeDeclare 方法详解

　　该方法用于声明交换机。

```java
void ExchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, IDictionary<String,Object> argument);
```

　　参数解析如下：

**exchange**：交换机名字。

**type**：交换机类型。exchange 有 direct、fnaout、topic、header 四种类型。

**durable**：是否持久化。设置为 true 时，交换机信息保存在 rabbitmq 的内置数据库中，服务器重启时交换机信息也会恢复。

**autoDelete**：是否自动删除。设置为 true 时，表示在最后一条使用该交换机的连接（Connection）断开时，自动删除这个 exchange。

**argument**：其他的一些参数，类型为 Dictionary< Stirng,Object >。

## 参考文章

[快速掌握 RabbitMQ](https://www.cnblogs.com/wyy1234/p/10743567.html)