package com.liang.collection.list;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

/**
 * 写在前面：
 * ArrayList是最常用的List实现类，
 * 内部是通过数组实现的，
 * 它允许对元素进行快速随机访问。
 * 数组的缺点是每个元素之间不能有间隔，
 * 当数组大小不满足时需要增加存储能力，就要将已经有数组的数据复制到新的存储空间中。
 *
 * 当从ArrayList的中间位置插入或者删除元素时，需要对数组进行复制、移动、代价比较高。因此，它适合随机查找和遍历，不适合插入和删除。
 *
 * Vector与ArrayList一样，也是通过数组实现的，不同的是它支持线程的同步，
 * 即某一时刻只有一个线程能够写Vector，避免多线程同时写而引起的不一致性，
 * 但实现同步需要很高的花费，因此，访问它比访问ArrayList慢。
 *
 * LinkedList是用链表结构存储数据的，很适合数据的动态插入和删除，随机访问和遍历速度比较慢。
 * 另外，他还提供了List接口中没有定义的方法，专门用于操作表头和表尾元素，可以当作堆栈、队列和双向队列使用。
 *
 * vector是线程（Thread）同步（Synchronized）的，所以它也是线程安全的，而Arraylist是线程异步（ASynchronized）的，是不安全的。
 * 如果不考虑到线程的安全因素，一般用Arraylist效率比较高。
 *
 * 如果集合中的元素的数目大于目前集合数组的长度时，vector增长率为目前数组长度的100%,
 * 而arraylist增长率为目前数组长度的50%.如过在集合中使用数据量比较大的数据，用vector有一定的优势。
 *
 * 如果查找一个指定位置的数据，vector和arraylist使用的时间是相同的，都是0(1),这个时候使用vector和arraylist都可以。
 * 而如果移动一个指定位置的数据花费的时间为0(n-i)n为总长度，这个时候就应该考虑到使用Linkedlist,
 * 因为它移动一个指定位置的数据所花费的时间为0(1),而查询一个指定位置的数据时花费的时间为0(i)。
 *
 * ArrayList和Vector是采用数组方式存储数据，此数组元素数大于实际存储的数据以便增加和插入元素，
 * 都允许直接序号索引元素，但是插入数据要设计到数组元素移动 等内存操作，所以索引数据快插入数据慢，
 * Vector由于使用了synchronized方法（线程安全）所以性能上比ArrayList要差，
 * LinkedList使用双向链表实现存储，按序号索引数据需要进行向前或向后遍历，但是插入数据时只需要记录本项的前后项即可，所以插入数度较快！
 *
 * 笼统来说：LinkedList：增删改快 ArrayList：查询快（有索引的存在）
 *
 *
 * ArrayList是List接口基于List相关操作的可扩容数组实现
 * 允许空值null的存在
 * ArrayList是线程不安全的
 *
 * size，isEmpty，get，set，iterator，还有listIterator操作的时间是常数时间O(1)，
 * add操作在均摊时间为常数时间，即添加n个元素需要O（n）时间复杂度。
 * 其它所有的操作都在线性时间内运行（粗略地说）。
 * 与LinkedList实现相比，ArrayList常数因子较低
 *
 * 每个ArrayList实例都有一个容量。
 * 容量用来存储列表中元素的数组的大小。其容量始终至少和列表大小一样大。
 * 随着元素被加到ArrayList中，其容量也会自动增长。
 * 除了添加元素具有恒定的均摊时间成本这一事实之外，并未指定增长策略的详细信息。
 *
 * 使用ensureCapacity操作添加大量的元素之前，应用程序可以增加ArrayList实例的容量。
 * 这可能会减少增量带来的重新分配的次数。
 *
 * 注意，这个实现是不同步的。
 * 如果多个线程同时访问一个ArrayList实例，并且至少有一个线程在结构上修改表，则必须在外部同步该列表。
 * （结构修改是添加或删除一个或多个元素、或显示调整备份数组的任何操作，仅设置元素的值而不是结构修改。）
 * 这通常是通过同步一些自然封装到列表的类来完成的。
 * 如果没有这样的对象存在，应该用Collection.synchronize方法“包装”这个列表
 *
 * 此类的iterator和listIterator方法返回的iterator是快速失败的：
 * 如果在创建iterator之后对list进行结构修改，除了iterator本身的remove和add方法外iterator将会抛出ConcurrentModificationException。
 * 因此在并发修改的情况下，iterator会快速而清晰地失败，而不是在未来不确定的时间内冒任意、不确定的风险。
 */
@SuppressWarnings("all")
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * 初始化容量为10
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 用于空实例的共享空数组实例。
     * 避免了空实例多出创建
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * 共享的空数组实例用于默认大小的空实例。
     * 我们将其与EMPTY_ELEMENTDATA区分开来，以了解添加第一个元素时应该膨胀多少。
     * 具体可以参见public ArrayList()无参构造方法
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * 这个ArrayList的元素被存储到这个数组缓冲区Object[] elementData中；
     * ArrayList的存储层容量就是这个数组缓冲区的长度
     * 当添加第一个元素时，任何带有elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA的空ArrayList都将扩展为DEFAULT_CAPACITY。
     */
    transient Object[] elementData; // non-private to simplify nested class access

    /**
     * 有效数据的大小
     */
    private int size;

    /**
     * 指定容量（存储层）构造一个空的（抽象层）ArrayList
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                    initialCapacity);
        }
    }

    /**
     * 构造一个存储容量为10的空List
     */
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * 构造一个包含指定元素的列表
     * 集合的迭代器按它们返回的顺序排列。
     */
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // replace with empty array.
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * 将这个ArrayList实例的容量capacity修剪为列表的当前大小size。
     * 可以最小化ArrayList实例。
     */
    public void trimToSize() {
        modCount++;
        if (size < elementData.length) {
            elementData = (size == 0)
                    ? EMPTY_ELEMENTDATA
                    : Arrays.copyOf(elementData, size);
        }
    }

    /**
     * 如果需要，增加这个ArrayList实例的容量，
     * 以确保它至少可以容纳由最小容量参数指定的元素数目。
     */
    public void ensureCapacity(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
                // any size if not default element table
                ? 0
                // larger than default for default empty table. It's already
                // supposed to be at default size.
                : DEFAULT_CAPACITY;

        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    /**
     * 此方法必须通过无参构造才能够进入判断 返回最小为10的容量
     * 否则直接返回minCapacity
     */
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }

    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    /**
     * 数组分配的最大容量
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 增加容量，确保它至少能容纳增加容量，确保它至少能容纳最小容量参数指定的元素数目。
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        // 扩容1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        // 扩容后如果比最小所需容量还小 则直接采用最小所需容量
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        // 如果新容量超过最大数组数量 最小容量取  MAX_ARRAY_SIZE
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    /**
     * 返回特定位置的元素
     */
    public E get(int index) {
        // 判断数组是越界
        rangeCheck(index);

        return elementData(index);
    }

    /**
     * 替换对应index中的元素并返回旧值
     */
    public E set(int index, E element) {
        // 判断数组是越界
        rangeCheck(index);

        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    /**
     * 在数组最后添加一个元素
     */
    public boolean add(E e) {
        // 判断是否需要扩容
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }

    /**
     * 在特定index插入元素
     */
    public void add(int index, E element) {
        // 判断是否越界
        rangeCheckForAdd(index);
        // 判断是否需要扩容
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        /**
         * 调用本地方法进行数组的数组
         * 参数如下
         * 源数组
         * 复制起点
         * 目标数组
         * 存放起点
         * 复制长度
         */
        System.arraycopy(elementData, index, elementData, index + 1,
                size - index);
        elementData[index] = element;
        size++;
    }

    /**
     * 移除指定位置的元素
     */
    public E remove(int index) {
        // 判断是否越界
        rangeCheck(index);

        modCount++;
        // 获取旧值
        E oldValue = elementData(index);

        fastRemove(index);

        return oldValue;
    }

    /**
     * 移除指定元素
     */
    public boolean remove(Object o) {
        // 这里只是多出一步循环equals（）
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                    numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

}
