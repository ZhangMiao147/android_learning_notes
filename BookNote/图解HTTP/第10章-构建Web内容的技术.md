# 第 10 章 构建 Web 内容的技术

## 10.1. HTML

### 10.1.1. Web 页面几乎由 HTML 构成

​		HTML ( HyperText Markup Language，超文本标记语言 ) 是为了发送 Web 上的超文本（ Hypertext ）而开发的标记语言。

​		超文本是一种文档系统，可将文档中任意位置的信息与其他信息（ 文本或图片等 ）建立关联，即超链接文本。

​		标记语言是指通过在文档的某部分穿插特别的字符串标签，用来修饰文档的语言。

​		把出现在 HTML 文档内的这种特殊字符串叫做 HTML 标签（Tag）。

​		由 HTML 构成的文档经过浏览器的解析、渲染后，呈现出来的结果就是 Web 页面。

### 10.1.2. HTML 的版本

​		能够被 Mosaic 解析的 HTML，统一标准后即作为 HTML 1.0 发布。

​		目前的最新版本是 HTML 4.01 标准，1999 年 12 月 W3C ( World Wide Web Consortium ) 组织推荐使用这一版本。下一个版本，预计会在 2014 年左右正式推荐使用 HTML5 标准。

​		HTML5 标准不仅解决了浏览器之间的兼容性问题，并且可把文本作为数据对待，更容易复用，动画等效果也变的更生动。

​		时至今日，HTML 仍存在较多悬而未决问题。有些浏览器未遵循 HTML 标准实现，或扩展自用标签等，这都反映了 HTML 的标准实际上尚未统一这一现状。

### 10.1.3. 设计应用 CSS

​		CSS ( Cascading Style Sheets ，层叠样式表 )  可以指定如何展现 HTML 内的各种元素，属于样式表准备之一。即使是相同的 HTML 文档，通过改变应用的 CSS，用浏览器看到的页面外观也会随之改变。CSS 的理念就是让文档的结构和设计分离，达到解耦的目的。

## 10.2. 动态 HTML

### 10.2.1. 让 Web 页面动起来的动态 HTML

​		所谓动态 HTML（ Dynamic HTML ），是指使用客户端脚本语言将静态的 HTML 内容变成动态的技术的总称。鼠标单击点开的新闻、Google Maps 等可滚动的地图就用到了动态 HTML。

​		动态 HTML 技术是通过调用客户端脚本语言 JavaScript，实现对 HTML 的 Web 页面的动态改造。利用 DOM（Document Object Model，文档对象模型）可指定欲发生动态变化的 HTML 元素。

### 10.2.2. 更易控制 HTML 的 DOM

​		DOM 是用以控制 HTML 文档和 XML 文档的 API（Application Programming Interface，应用编程接口）。使用 DOM 可以将 HTML 内的元素当作对象操作，如取出元素内的字符串、改变那个 CSS 的属性等，使页面的设计发生改变。

​		通过调用 JavaScript 等脚本语言对 DOM 操作，可以以更为简单的方式控制 HTML 的改变。

## 10.3. Web 应用

### 10.3.1. 通过 Web 提供功能的 Web 应用

​		Web 应用是指通过 Web 功能提供的应用程序。比如购物网站、网上银行、SNS、BBS、搜索引擎和 elearning 等。互联网（Internet）或企业内网（Intranet）上遍布各式各样的 Web 应用。

由程序创建的内容成为动态内容，而事先准备好的内容成为静态内容。Web 应用则作用与动态内容之上。

### 10.3.2. 与 Web 服务器及程序协作的 CGI

​		CGI ( Common Gateway Interface，通用网管接口 ) 是指 Web 服务器在接收到客户端发送过来的请求后转发给程序的一组机制。在 CGI 的作用下，程序会对请求内容作出相应的动作，比如创建 HTML 等动态内容。

​		创建 CGI 的程序叫做 CGI 程序，通常是用 Perl、PHP、Ruby 和 C 等编程语言编写而成。

### 10.3.3. 因 Java 而普及的 Servlet

​		Servlet 是一种能够在服务器上创建动态内容的程序。Servlet 是用 Java 语言实现的一个接口，属于面向企业级 Java（ JavaEE，Java Enterprise Edition ）的一部分。

​		CGI ，由于每次接到请求，程序都要跟着启动一次。因此一旦访问量过大，Web 服务器要承担相当大的负载。而 Servlet 运行在与 Web 服务器相同的进程中，因此收到的负载较小。Servlet 的运行环境叫做 Web 容器或 Servlet 容器。

​		Servlet 作为解决 CGI 问题的对抗技术，随 Java 一起得到了普及。

​		随着 CGI 的普及，每次请求都要启动新 CGI 程序的 CGI 运行机制逐渐变成了性能瓶颈，所以之后 Serlvet 和 mod_perl 等可直接在 Web 服务器上运行的程序才得以开发、普及。

## 10.4. 数据发布的格式及语言

### 10.4.1. 可扩展标记语言

​		XML（ extensible Markup Language，可扩展标记语言 ）是一种可按应用目标进行扩展的通用标记语言。旨在通过使用 XML，使互联网数据共享变得更容易。

​		XML 和 HTML 都是从标准通用标记语言 SGML（ Standard Generalized Markup Language ）简化而成。与 HTML 相比，它对数据的记录方式做了特殊处理。

​		为了保持数据的正确读取，HTML 不适合用来记录数据结构。

​		XML 和 HTML 一样，使用标签构成树形结构，并且可自定义扩展标签。

​		从 XML 文档中读取数据比起 HTML 更为简单。由于 XML 的结构基本上都是用标签分割而成的树形结构，因此通过语法分析器（ Parser ）的解析功能解析 XML 结构并取出数据元素，可更容易地对数据进行读取。

​		更容易地复用数据使得 XML 在互联网上被广泛接受。比如，可用在 2 个不同的应用之间的交换数据格式化。

### 10.4.2. 发布更新信息的 RSS/Atom

​		RSS（简易信息聚合，也叫聚合内容）和 Atom 都是发布新闻或博客日志等更新信息文档的格式的总称。两者都用到了 XML。

### 10.4.3. JavaScript 衍生的轻量级易用 JSON

​		JSON（ JavaScript Object Notation ）是一种以 JavaScript（ ECMAScript ） 的对象表示法为基础的轻量级数据标记语言。能够处理的数据类型有 false/null/true/对象/数组/数字/字符串，这 7 种类型。

​		JSON 让数据更轻更纯粹，并且 JSON 的字符串形式可被 JavaScript  轻易地读入。当初配合 XML 使用的 Ajax 技术可让 JSON 的应用变得更为广泛。另外，其他各种编程语言也提供丰富的库类，以达到轻便操作 JSON 的目的。











