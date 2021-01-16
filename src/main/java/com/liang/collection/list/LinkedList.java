package com.liang.collection.list;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * LinkedList是一个双向链表，实现了List接口和Deque接口。
 * 实现了所有的可选的list操作，允许包括null在内的所有元素。
 *
 * 所有的操作都与双向链表相同，
 * 索引链表中的元素时将会从链表头结点或者尾节点开始，这要取决于要索引的元素距离哪边比较近。
 *
 * LinkedList并不是同步的，如果多个线程并发的去访问一个链表，并且至少有一个线程对其进行改变，
 * 我们必须在外部进行加锁。我们可以使用如下方法来创建安全的链表：
 *   List list = Collections.synchronizedList(new LinkedList(...));
 *
 * 列表的iterator和listIterator方法返回的迭代器是快速失败的，
 * 通俗点说就是：如果在迭代器生成之后，除了调用迭代器的remove方法或add方法对链表进行修改的操作之外的任何其他方式的修改，
 * 都会导致迭代器抛出ConcurrentModificationException异常。
 * 也就是说，迭代器生成之后，你可以通过迭代器的remove或add方法去对列表进行修改，
 * 不能再调用链表的自身的add或remove方法对链表进行修改。
 */

@SuppressWarnings("all")
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{

    private static final long serialVersionUID = 876323262645176354L;

    // 默认的长度为0
    transient int size = 0;

    transient Node<E> first;

    transient Node<E> last;

    /**
     * Constructs an empty list.
     */
    public LinkedList() {
    }


    public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * 在队列头部添加元素
     */
    private void linkFirst(E e) {
        // 取出当前头部
        final Node<E> f = first;
        // 新头部
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        if (f == null)
            last = newNode;
        else
            f.prev = newNode;
        size++;
        modCount++;
    }

    /**
     * Links e as last element.
     */
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

    /**
     * Inserts element e before non-null Node succ.
     */
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

    /**
     * Unlinks non-null first node f.
     */
    private E unlinkFirst(Node<E> f) {
        // assert f == first && f != null;
        final E element = f.item;
        final Node<E> next = f.next;
        f.item = null;
        f.next = null; // help GC
        first = next;
        if (next == null)
            last = null;
        else
            next.prev = null;
        size--;
        modCount++;
        return element;
    }

    /**
     * Unlinks non-null last node l.
     */
    private E unlinkLast(Node<E> l) {
        // assert l == last && l != null;
        final E element = l.item;
        final Node<E> prev = l.prev;
        l.item = null;
        l.prev = null; // help GC
        last = prev;
        if (prev == null)
            first = null;
        else
            prev.next = null;
        size--;
        modCount++;
        return element;
    }

    /**
     * Unlinks non-null node x.
     */
    E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        modCount++;
        return element;
    }

    /**
     * 返回列表中的第一个元素
     */
    public E getFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return f.item;
    }

    /**
     * 返回列表中的最后一个元素
     */
    public E getLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }

    /**
     * 移除第一个元素
     */
    public E removeFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return unlinkFirst(f);
    }

    /**
     * 移除最后一个元素
     */
    public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }

    /**
     * 在头部插入元素
     */
    public void addFirst(E e) {
        linkFirst(e);
    }

    /**
     * 在尾部插入元素
     */
    public void addLast(E e) {
        linkLast(e);
    }

    /**
     * 在尾部插入元素
     */
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    /**
     * 移除指定元素
     */
    public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取指定index的元素
     */
    public E get(int index) {
        // 判断是否下标越界
        checkElementIndex(index);
        return node(index).item;
    }

    /**
     * 替换指定index的元素值
     * 返回旧值
     */
    public E set(int index, E element) {
        // 判断是否下标越界
        checkElementIndex(index);
        Node<E> x = node(index);
        E oldVal = x.item;
        x.item = element;
        return oldVal;
    }

    /**
     * 在指定下标插入元素
     */
    public void add(int index, E element) {
        checkPositionIndex(index);

        if (index == size)
            linkLast(element);
        else
            linkBefore(element, node(index));
    }

    /**
     * 移除指定下标的元素
     */
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    /**
     * Tells if the argument is the index of an existing element.
     */
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    /**
     * Tells if the argument is the index of a valid position for an
     * iterator or an add operation.
     */
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 返回对应下表的元素
     * 不为空 因为每次调用这个方法前都进行了越界检查
     */
    Node<E> node(int index) {
        //
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

    // 以下是队列相关的操作

    /**
     * 返回第一个元素
     * 不存在则返回null
     * 不进行删除
     */
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    /**
     * 返回第一个元素
     * 不存在则抛出异常
     */
    public E element() {
        return getFirst();
    }

    /**
     * 返回并移除第一个元素
     */
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     * 移除第一个元素
     * 不存在则抛出异常
     */
    public E remove() {
        return removeFirst();
    }

    /**
     * 在尾部插入元素
     */
    public boolean offer(E e) {
        return add(e);
    }

    /**
     * 在头部插入元素
     */
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * 在尾部插入元素
     */
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    /**
     * 返回第一个元素
     * 不存在则返回null
     * 不进行删除
     */
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
     }

    /**
     * 返回最后一个元素
     * 不存在则返回null
     * 不进行删除
     */
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

    /**
     * 返回并移除第一个元素
     */
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     * 返回并移除最后一个元素
     */
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }

    /**
     * 在头部插入元素
     */
    public void push(E e) {
        addFirst(e);
    }

    /**
     * 移除第一个元素
     */
    public E pop() {
        return removeFirst();
    }

    /**
     * 移除第一个出现的元素
     */
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    /**
     * 移除最后一个出现的元素
     */
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
}
