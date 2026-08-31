# Token 鉴权

**常见鉴权方式**

| 方式             | Android 实现要点                     |
| -------------- | -------------------------------- |
| Bearer Token   | Header:Authorization: Bearer xxx |
| JWT            | 解析 exp、判断过期（一般不在客户端验签）           |
| Refresh Token  | 双 Token 机制                       |
| Cookie+Session | CookieJar，较少见于新 App              |
| OAuth 2.0      | 微信/Google 等第三方登录                 |

<mark>JWT 是什么？</mark>

JWT：Json Web Token

JWT 签名，是用来证明这个 Token 没有被篡改，并且确实是服务端（或持有密钥的一方）签发的。

JWT 由三部分组成，用 . 连接：

`Header.Payload.Signature`

例如：

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0In0.xxx
   ↑ Header            ↑ Payload              ↑ Signature
```

三部分分别是：

| 部分        | 内容         | 是否明文       |
| --------- | ---------- | ---------- |
| Header    | 算法、类型      | Base64，可解码 |
| Payload   | 用户信息、exp 等 | Base64，可解码 |
| Signature | 签名         | 不可反推原文     |

* 签名是干什么的？
  
  JWT 的 Header 和 Payload 只是 Base64 编码，不是加密，任何人都能解码看到内容。
  
  那么问题来了：如果别人改了 Payload（比如把 userId = 123 改成 userId = 999），服务端怎么发现？
  
  答案就是：签名
  
  签名的作用：
  
  1. 防篡改：内容被改过，签名校验会失败
  
  2. 验证来源：只有持有密钥的服务端才能生成合法签名

* 签名怎么生成
  
  服务端用密钥（secret）对 Header.Payload 做哈希运算，得到 Signature。
  
  最终 JWT：
  
  Header.Payload.Signature

* 服务端怎么验证签名？
  
  客户端请求时带上 JWT：
  
  ```
  Authorization: Bearer Header.Payload.Signature
  ```
  
  服务端收到后会：
  
  1. 取出 Header 和 Payload
  
  2. 用同一把密钥再算一遍签名
  
  3. 和 Token 里的 Signature 对比
  
  4. 一致 -》 Token 合法，不一致 -〉 拒绝（401）

* 常见签名算法
  
  | 算法          | 类型    | 说明                  |
  | ----------- | ----- | ------------------- |
  | HS256       | 对称加密  | 同一把 secret，签发和验证都用它 |
  | HS384/HS512 | 对称加密  | 更强哈希                |
  | RS256       | 非对称加密 | 私钥签名，公钥验证           |
  | ES256       | 非对称加密 | 椭圆曲线，移动端/common     |
  
  * HS256（最常见、易理解）
    
    服务端签名：secret + 数据 -> Signature
    
    服务端验证：secret + 数据 -> 再算一遍对比
    
    特点：签发和验证都用同一个 secret
  
  * RS256（大型系统常见）
    
    私钥签名 -> 生成 Signature
    
    公钥签名 -> 校验 Signature
    
    特点：
    
    * 私钥只在认证服务
    
    * 其他微服务只拿公钥验证，拿不到私钥，更安全

**典型登录流程**

1. POST /auth/login -> 返回 access_token + refresh_token

2. Token 存本地（EncryptedSharedPreferences / DataStore）

3. OkHttp Interceptor 自动加 Authorization Header

4. access_token 过期 -> 401

5. Authenticator 用 refresh_token 换新 Token

6. 重试原请求

7. refresh 也失败 -> 跳转登陆页

GOMO 登陆：

1. 调用 login 接口，login 接口返回 access_token 和 refresh_token，将 access_token 和 refresh_token 保存在本地。
   
   refresh_token 只在调用 refresh_access_token 接口的时候使用了。
   
   登出或者 Token 失效时，会清除 access_token 和 refresh_token。

2. 后续需要 token 的请求的接口会使用 access_token，比如 getUserInfo。

3. OkHttpClient 添加 TokenInterceptor 监听响应体，判断 Token 是否过期，如果 Token 过期，则调用 refresh_access_token 接口（参数使用 refresh_token）刷新Token。
   
   * 使用 Mutes 加锁调用 refresh_access_token 接口（从发送请求到处理结果都在锁内），避免并发重复刷新。
   
   * 若其他请求已刷新过 Token，直接复用新的 Token。
   
   * Refresh 失败则发出 isTokenExpired 事件，引导重新登陆。
   
   * Refresh 成功只更新 access_token，并重试原请求。

**RESTful**

RESTful 指的是：API 设计遵循 REST（Representational State Transfer，表述性状态转移）风格。可以简单理解为：用 URL 表示资源，用 HTTP 方法表示操作。