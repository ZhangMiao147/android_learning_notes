# Queue 的 add 与 offer 的区别

Java Queue中 add/offer，element/peek，remove/poll中的三个方法均为重复的方法，在选择使用时不免有所疑惑，这里简单区别一下：

## add() 和 offer()区别

add() 和 offer()都是向[队列](https://so.csdn.net/so/search?q=队列&spm=1001.2101.3001.7020)中添加一个元素。一些队列有大小限制，因此如果想在一个满的队列中加入一个新项，调用 add() 方法就会抛出一个 unchecked 异常，而调用 offer() 方法会返回 false。因此就可以在程序中进行有效的判断！

## poll() 和 remove() 区别

remove() 和 poll() 方法都是从队列中删除第一个元素。如果队列元素为空，调用 remove() 的行为与 Collection 接口的版本相似会抛出异常，但是新的 poll() 方法在用空集合调用时只是返回 null。因此新的方法更适合容易出现异常条件的情况。

## element() 和 peek() 区别

element() 和 peek() 用于在队列的头部查询元素。与 remove() 方法类似，在队列为空时， element() 抛出一个异常，而 peek() 返回 null。

##  Java 中 Queue 的一些常用方法

* add：增加一个元索，如果队列已满，则抛出一个 IIIegaISlabEepeplian 异常。
* remove：移除并返回队列头部的元素，如果队列为空，则抛出一个 NoSuchElementException 异常。
* element：返回队列头部的元素，如果队列为空，则抛出一个 NoSuchElementException 异常。
* offer：添加一个元素并返回 true，如果队列已满，则返回 false。
* poll：移除并返问队列头部的元素，如果队列为空，则返回 null。
* peek：返回队列头部的元素，如果队列为空，则返回 null。
* put：添加一个元素，如果队列满，则阻塞。
* take：移除并返回队列头部的元素

## 参考文章

1. [Queue中 add/offer ....区别](https://blog.csdn.net/xiaozhegaa/article/details/106136105)