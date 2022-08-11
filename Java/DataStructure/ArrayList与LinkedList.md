# ArrayList 与 LinkedList

## 1. ArrayList
　　ArrayList：底层是基于动态数组，根据下标随机访问数组元素的效率高，向数组尾部添加元素的效率高；但是，删除数组中的数据以及向数组中间添加数据效率低，因为需要移动数组。

　　而之所以称为动态数组，是因为 ArrayList 在数组元素超过其容量时，ArrayList 可以进行扩容（针对 JDK 1.8，数组扩容后的容量是扩容前的 1.5 倍）。

　　ArrayList 源码中最大的数组容量是 Integer.MAX_VALUE-8，对于空出的 8 位，目前解释是：

1. 存储 Headerwords。
2. 避免一些机器内存溢出，减少出错几率，所以少分配。
3. 最大还是能支持到 Integer.MAX_VALUE(当 Integer.MAX_VALUE - 8 依旧无法满足需求时)。

　　ArrayList 的部分源码：
```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
	 // 最大的数组容量
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    public boolean add(E e) {
		// 先确定数组容量
        ensureCapacityInternal(size + 1);  // Increments modCount!!
		// 将新数据添加到数组尾部
        elementData[size++] = e;
        return true;
    }

	 // 将数据插入到指定位置
    public void add(int index, E element) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
		// 先确定数组容量
        ensureCapacityInternal(size + 1);  // Increments modCount!!
		// 将 index 之后的数据向后移一位
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
		// 将数据插入 index
        elementData[index] = element;
        size++;
    }
	
	
	...
	private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
	...
	private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
		// 将数组容量进行扩容
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
	private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
		// 如果 Interger.MAX_VALUE-8 依旧无法满足内存需求时，取 Integer.MAX_VALUE
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

	 // 获取数据很方便
    public E get(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));

        return (E) elementData[index];
    }

}
```
　　可以看到，只要 ArrayList 的当前容量足够大，add() 操作向数组的尾部插入数据的效率是非常高的，当向数组指定位置添加数据时，会进行大量的数组移动复制操作。而数组复制时，最终将调用 System.arraycopy() 方法，因此 add() 操作的效率还是相当高的。

　　尽管这样当向指定位置添加数据时也还是比 LinkedList 慢，后者添加数据只需要改变指针指向即可。ArrayList 删除数组也需要移动数组，效率较慢。但是 ArrayList 获取数据是很快的。

## 2. LinkedList
　　LinkedList 是基于链表的动态数组，数据添加删除效率高，只需要改变指针指向即可，但是访问数据的平均效率低，需要对链表进行遍历。

　　LinkedList 的部分源码：
```java
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
    public boolean add(E e) {
        linkLast(e);
        return true;
    }
	// 插入数据到指定位置
	public void add(int index, E element) {
        checkPositionIndex(index);

        if (index == size)
            linkLast(element);
        else
            linkBefore(element, node(index)); // 需要先找到当前 index 位置的元素
    }
	
    /**
     * Links e as last element.
     */
	 // 添加数据到链表尾端
	 // 直接插入数据，修改位置就 OK 了
    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
        modCount++;
    }
	// 将数据插入到 succ 之前
	// 只需要修改位置就 OK 了
	void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
        final Node<E> pred = succ.prev;
        final Node<E> newNode = new Node<>(pred, e, succ);
        succ.prev = newNode;
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        size++;
        modCount++;
    }
	// 获取指定位置的数据
	public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }
	// 查找 index 位置的数据
	// 需要找到 index 的位置，效率低
	Node<E> node(int index) {
        // assert isElementIndex(index);

        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

}
```
　　可以看出 LinkedList 的添加元素到末尾效率是非常高的，直接插入数据，remove 也是相同的，效率很高。在插入数据到指定位置，需要先找到插入的位置，然后再插入，效率也不是很高，并且 LinkedList 的访问数据 get 是非常慢的，它需要先找到 index 的位置上的数据，然后才能取出数据。

## 3. ArrayList 与 LinkedList 的区别

1. ArrayList 是基于动态数组的数据结构，LinkedList 是基于链表的数据结构。（LinkedList 是双向链表，有 next 也有 previous）。

2. 对于随机访问 get 和 set，ArrayList 要优于 LinkedList，因为 LinkedList 要移动指针。

   ArrayList 是线性表（数组），get() 直接读取第几个下标，复杂度 O(1)。

   LinkedList 是链表的操作，get() 获取第几个元素，依次遍历，复杂度O(n)。

3. 对于新增和删除操作 add 和 remove，LinkedList 比较占优势，因为 ArrayList 要移动数据。

   ArrayList：add(E) 添加元素，直接在后面添加，复杂度 O(1)；
   add(index,E) 添加元素，在第几个元素后面插入，后面的元素需要向后移动，复杂度 O(n)；remove() 删除元素，后面的元素需要逐个移动，复杂度 O(n)。

   LinkedList：add(E) 添加到末尾，复杂度0(1)；add(index,E) 添加第几个元素后，需要先查找到第几个元素，直接指针指向操作，复杂度 O(n)；remove() 删除元素，直接指针指向操作，复杂度 O(1)。

## 4. 总结

　　ArrayList 和 LinkedList 在性能上各有优缺点，都有各自所适用的地方，总的说来可以描述如下：

1. 对 ArrayList 和 LinkedList 而言，在列表末尾增加一个元素所花的开销都是固定的。对 ArrayList 而言，主要是在内部数组中增加一项，指向所添加的元素，偶尔可能会导致对数组重新进行分配；而对 LinkedList 而言，这个开销是统一的，分配一个内部 Entry 对象。
2. 在 ArrayList 的中间插入或删除一个元素意味着这个列表中剩余的元素都会被移动；而在 LinkedList 的中间插入或删除一个元素的开销是固定的。
3. LinkedList 不支持高效的随机元素访问。
4. ArrayList 的空间浪费主要体现在在 list 列表的结尾预留一定的容量空间，而 LinkedList 的空间花费则体现在它的每一个元素都需要消耗相当的空间。

　　可以这样说：当操作是在一列数据的后面添加数据而不是在前面或中间，并且需要随机地访问其中的元素时，使用 ArrayList 会提供比较好的性能；当操作是在一列数据的前面或中间添加或删除数据，并且按照顺序访问其中的元素时，就应该使用 LinkedList 了。

　　时间复杂度比较：

　　首先一点关键的是，ArrayList 的内部实现是基于基础的对象数组的，因此，它使用 get 方法访问列表中的任意一个元素时（random access），它的速度要比 LinkedList 快。LinkedList 中的 get 方法是按照顺序从列表的一端开始查找，直到另外一端。对 LinkedList 而言，访问列表中的某个指定元素没有更快的方法了。

　　但在某些情况下 LinkedList 的表现要优于 ArrayList，有些算法在 LinkedList 中实现时效率更高。比如说，利用 Collections.reverse() 方法对列表进行反转时，其性能就要好些。当要对 list 进行大量的插入和删除操作时，LinkedList 也是一个较好的选择。

## 5. 参考文章

1. [ArrayList和LinkedList的区别](https://www.cnblogs.com/one-apple-pie/p/11033048.html)



