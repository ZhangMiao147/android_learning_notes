# HTTP

## HTTP 简介
　　HTTP 协议是 Hyper Text Transfer Protocol(超文本传输协议)的缩写，是用于从万维网（WWW:World Wide Web）服务器传输超文本到本地浏览器的传送协议。

　　HTTP 是基于 TCP/IP 通信协议来传输数据（HTML 文件，图片文件，查询结果等）。

#### HTTP 工作原理
　　HTTP 协议工作于客户端-服务端架构上。浏览器作为 HTTP 客户端通过 URL 向 HTTP 服务端发送所有请求。

　　Web 服务器有：Apache 服务器，IIS 服务器（Internet Information Service）等。

　　Web 服务器根据接收到的请求后，向客户端发送响应信息。

　　HTTP 默认端口号为 80，也可以改为 8080 或者其他端口。

**HTTP 三点注意事项：**
* HTTP 是无连接：无连接的含义是限制每次连接只处理一个请求。服务端处理完客户的请求，并收到客户的应答后，即断开连接。采用这种当时可以节省传输时间。
* HTTP 是媒体独立的：这意味着，只要客户端和服务端知道如何处理的数据内容，任何类型的数据都可以通过 HTTP 发送。客户端以及服务端指定使用合适的 MIME-type 内容类型。
* HTTP 是无状态：HTTP 协议是无状态协议。无状态是指协议对于事务处理没有记忆能力。缺少状态意味着如果后续处理需要前面的信息，则它必须重传，这样可能导致每次连接传送的数据量增大。另一方面，在服务器不需要先前信息时它的应答就较快。

　　下图是 HTTP 协议通信流程：
![](./HTTP协议通信流程.gif)

　　浏览器显示的内容都有 HTML、XML、GIF、FLASH 等，浏览器是通过 MIME Type 区分它们，决定用什么内容什么形式来显示。

　　MIME Type 是该资源的媒体类型，MIME Type 不是个人指定的，是通过互联网（IETF）组织协商，以 RFC(是一系列以编号排定的文件，几乎所有的互联网标准都有收录在其中)的形式作为建议的标准发布在网上的，大多数的 Web 服务器和用户代理都会支持这一规范（顺便说一句，Email 附件的类型也是通过 MIME Type 指定的）。

　　媒体类型通常通过 HTTP 协议，由 Web 服务器告知浏览器的，更准确的说，是通过 Content-Type 来表示的。

　　如果是某个客户端子机定义的格式，MIME Type 一点只能以 application/x- 开头。

## HTTP 消息结构
　　HTTP 是基于客户端/服务端（C/S）的架构模型，通过一个可靠的链接来交换信息，是一个无状态的请求/响应协议。

　　一个 HTTP "客户端"是一个应用程序（Web 浏览器或其他任何客户端），通过链接服务器达到发送一个或多个 HTTP 的请求的目的。

　　一个 HTTP “服务器”同样也是一个应用程序（通常是一个 Web 服务，如 Apache Web 服务器或 IIS 服务器等），通过接收客户端的请求并向客户端发送 HTTP 响应数据。

　　HTTP 使用统一资源标识符（Uniform Resource Identifiers,URL） 来传输数据和建立连接。

　　一旦建立连接后，数据消息就通过类似 Internet 邮件所适用的格式和多用途 Internet 邮件扩展（MIME）来传送。

#### 客户端请求消息
　　客户端发送一个 HTTP 请求到服务器的请求消息包括以下格式：请求行（request line）、请求头部（header）、空行和请求数据四个部分组成，下图给出了请求报文的一般格式：
![](./请求报文的一般格式.png)

#### 服务器响应消息
　　HTTP 响应也由四个部分组成，分别是：状态行、消息报头、空行和响应正文。
![](./HTTP服务器响应消息.jpg)

#### 实例
　　下面实例是一些典型的使用 GET 来传递数据的实例：

　　客户端请求：
```
GET /hello.txt HTTP/1.1
User-Agent: curl/7.16.3 libcurl/7.16.3 OpenSSL/0.9.7l zlib/1.2.3
Host: www.example.com
Accept-Language: en, mi
```

　　服务端响应：
```
HTTP/1.1 200 OK
Date: Mon, 27 Jul 2009 12:28:53 GMT
Server: Apache
Last-Modified: Wed, 22 Jul 2009 19:15:56 GMT
ETag: "34aa387-d-1568eb00"
Accept-Ranges: bytes
Content-Length: 51
Vary: Accept-Encoding
Content-Type: text/plain
```

　　输出结果：
```
Hello World! My payload includes a trailing CRLF.
```

## HTTP 请求方法
　　根据 HTTP 标准，HTTP 请求可以使用多种请求方法。

　　HTTP 1.0 定义了三种请求方法：GET、POST 和 HEAD 方法。

　　HTTP 1.1 新增了六种请求方法：OPTIONS、PUT、PATCH、DELETE、TRACE 和 CONNECT 方法。

| 方法 | 描述 |
| -------- | -------- |
| GET | 请求指定的页面信息，并返回实体主体。 |
| HEAD | 类似于 GET 请求，只不过返回的响应中没有具体的内容，用于获取报头。 |
| POST | 向指定资源提交数据进行处理请求（例如提交表单或者上传文件）。数据被包含在请求体重。POST 请求可能会导致新的资源的建立或已有资源的修改。 |
| PUT | 从客户端向服务器传送的数据取代指定的文档的内容。 |
| DELETE | 请求服务器删除指定的页面。 |
| CONNECT | HTTP/1.1 协议中预留给能够将连接改为管道方式的代理服务器。 |
| OPTIONS | 允许客户算查看服务器的性能。 |
| TRACE | 回显服务器收到的请求，主要用于测试或诊断。 |
| PATCH | 是对 PUT 方法的补充，用来对已知资源进行局部更新。 |

## HTTP 响应头信息
　　HTTP 响应头提供了关于请求、响应或者其他的发送实体的信息。

| 应答头 | 说明 |
| -------- | -------- |
| Allow | 服务器支持哪些请求方法（如 GET、POST 等） |
| Content-Encoding | 文档的编码（Encode）方法。只有在解码之后才可以得到 Content-Type 头指定的内容类型。利用 gzip 压缩文档能够显著地减少 HTML 文档的下载时间。Java 的 GZIPOutputStream 可以很方便地进行 gzip 压缩，但只有 Unix 上的 Netscape 和 Windows 上的 IE 4、IE 5 才支持它。因此，Servlet 应该通过查看 Accept-Encoding 头（即 request.getHeader("Accept-Encoding")）检查浏览器是否支持 gzip，为支持 gzip 的浏览器返回经 gzip 压缩的 HTML 页面，为其他浏览器返回普通页面。 |
| Content-Length | 表示内容长度。只有当浏览器使用持久 HTTP 连接时才需要这个数据。 如果你想要利用持久连接的优势，可以把输出文档写入 ByteArrayOutputStream，完成后查看其大小，然后把该值放入 Content-Length 头，最后通过 byteArrayStream.write(response.getOutputStream))发送内容。|
| Content-Type | 表示后面的文档属于什么 MIME 类型。Servlet 默认为 text/plain，但通常需要显式地指定为 text/html。由于经常要设置 Content-Type，因此 HttpServletResponse 提供了一个专用的方法 setContentType。 |
| Date | 当前的 GMT 时间。你可以用 setDateHeader 来设置这个头以避免转换时间格式的麻烦。 |
| Expires | 应该在什么时候认为文档已经过期，从而不再缓存它。 |
| Last-Modified | 文档的最后改动时间。客户可以通过 If-Modified-Since 请求头提供一个日期，该请求将被视为一个条件 GET，只有改动时间迟于指定时间的文档才会返回，否则返回一个 304（Not Modified）状态。Last-Modified 也可用 setDateHeader 方法来设置。 |
| Location | 表示客户应当到哪里去提取文档。Location 通常不是直接设置的，而是通过 HttpServletResponse 的 sendRedirect 方法，该方法同时设置状态代码为 302。 |
| Refresh | 表示浏览器应该在多少时间之后刷新文档，以秒计。除了刷新当前文档之外，你还可以通过 setHeader("Refresh","5;URL=http://host/path") 让浏览器读取指定的页面。<a> </a>注意这种功能通常是通过设置 HTML 页面 HEAD 区的 < META HTTP-EQUIV="Refresh" CONTENT="5;URL=http://host/path"> 实现，这是因为，自动刷新或重定向对于那些不能使用 CGI 或 Servlet 的 HTML 编写者十分重要。但是，对于 Servlet 来说，直接设置 Refresh 头更加方便。
注意 Refresh 的意义是 “N 秒之后刷新本页面或访问指定页面”，而不是“每隔 N 秒刷新本页面或访问指定页面”。因此，连续刷新要求每次都发送一个 Refresh 头，而发送 204 状态代码则可以阻止浏览器继续刷新，不管是使用 Refresh 头还是 < META HTTP-EQUIV="Refrsh"... > <a></a> 注意 Refrsh 头不属于 HTTP 1.1 正式规范的一部分，而是一个扩展，但 Netscape 和 IE 都支持它。|
| Server | 服务器名字。Servlet 一般不设置这个值，而是由 Web 服务器自己设置。 |
| Set-Cookie | 设置和页面关联的 Cookie。Servlet 不应使用 response.setHeader("Set-Cookie",...)，而是应使用 HttpServletResponse 提供的专用方法 addCookie。|
| WWW-Authenticate | 客户应该在 Authorization 头中提供什么类型的授权信息？在包含 401 （Unauthorized） 状态行的应答中这个头是必须的。例如，response.setHeader("WWW-Authenticate","BASIC realm="executives")。注意 Servlet 一般不进行这方面的处理，而是让 Web 服务器的专门机制来控制受密码保护页面的访问（例如 htaccess）。 |

## HTTP 状态码
　　当浏览器访问一个网页时，浏览者的浏览器会向网页所在服务器发出请求。当浏览器接收并显示网页前，此网页所在的服务器会返回一个包含 HTTP 状态码的信息头（server header）用以响应浏览器的请求。

　　下面是常见的 HTTP 状态码：
* 200 - 请求成功
* 301 - 资源（网页等）被永久转义到其他 URL
* 404 - 请求的资源（网页等）不存在
* 500 - 内部服务器错误

#### HTTP 状态码分类
　　HTTP 状态码由三个十进制数字组成，第一个十进制数字定义了状态码的类型，后两个数字没有分类的作用。HTTP 状态码共分为 5 种类型：

　　HTTP 状态码分类：

| 分类 | 分类描述 |
| -------- | -------- |
| 1xx | 信息，服务器收到请求，需要请求者继续执行操作 |
| 2xx | 成功，操作被成功接收并处理 |
| 3xx | 重定向，需要进一步的操作以完成请求 |
| 4xx | 客户端错误，请求包含语法错误或无法完成请求 |
| 5xx | 服务器错误，服务器在处理请求的过程中发生了错误 |

　　HTTP 状态码列表：

| 状态码 | 状态码英文名称 | 中文描述 |
| -------- | -------- | -------- |
| 100 | Continue | 继续，客户端应继续其请求 |
| 101 | Switching Protocols | 切换协议。服务器根据客户端的请求切换协议。只能切换到更高级的协议，例如，切换到 HTTP 的新版本协议 |
| 200 | OK | 请求成功。一般用于 GET 与 POST 请求 |
| 201 |  Created | 已创建。成功请求并创建了新的资源 |
| 202 | Accepted | 已接受。已经接受请求，但未处理完成 |
| 203 | Non-Authoritative Information | 非授权信息。请求成功。但返回的 meta 信息不在原始的服务器，而是一个副本 |
| 204 | No Content | 无内容。服务器成功处理，但未返回内容。在未更新网页的情况下，可确保浏览器继续显示当前文档 |
| 205 | Reset Content | 重置内容。服务器处理成功，用户终端（例如：浏览器）应重置文档视图。可通过此返回码清楚浏览器的表单域 |
| 206 | Partial Content | 部分内容。服务器成功处理了部分 GET 请求 |




## HTTP content-type

## 参考文章
[HTTP 教程](https://www.runoob.com/http/http-tutorial.html)
[网络：HTTP](https://blog.csdn.net/oldwang1999/article/details/98526414)
[网络之 HTTP 协议](https://blog.csdn.net/qq_42725815/article/details/87892480)


