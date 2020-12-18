package com.liang.hashmap;

import javax.swing.tree.TreeNode;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;

/**
 * 散列表基于map接口实现。这实现提供了所有可选的映射操作，并允许key和value为null。
 * hashMap大致等同于散列表，但它是线程不安全的并允许空值
 * hashMap是无序的
 *
 * 这个实现为基本操作提供了恒等的性能
 * 假设哈希函数使元素尽可能在桶中分布均匀，
 * 迭代这个集合视图所需要的时间与hashMap实例加上键值对映射大小是成正比的
 * 因此，当非常看重迭代性能时，不能把容量设置过高或者装载因子设置过低
 *
 * hashMap有两个因素影响它的性能，分别是初始容量和装载因子
 * 容量就是散列表中桶的数量，简单的说可以认为是散列表刚被创建时的容量
 * 装载因子是在容量自动增长前描述散列表有多满的一个指标
 * 当散列表中键值对的数量超过 capacity * load factor
 * 散列表将重新哈希（也就是说，内部的数据与结构将被重新创建）
 * 以至于哈希表获得大约两倍的桶的数量
 *
 * 作为一个普遍的规则，默认的装载因子0.75提供了一个很好的在时间和空间花费上一个很好的权衡
 * 较高的值会减少空间的开销但增加了查找的时间（如 get、put操作）
 * hashMap中键值对数量因考虑预期数量及装载因子，以使rehash操作次数最小化
 * 如果初始容量 * 装载因子 小于 最大键值对数量，那么rehash操作将永远不会发生
 *
 * 如果大量的键值对被存储到hashMap实例中，
 * 允许更大的初始容量会比通过自动rehash操作增加哈希表更有效
 * 事实上大量的keys使用相同的hashCode会降低hashMap的性能
 * 而当keys具有比较性时便能打破这种联系
 *
 * 事实上hashMap实现不是线程安全的
 * 如果多个线程同时访问这个hashMap，并且至少有一个线程修改了hashMap的结构
 * 则必须进行外部同步
 *
 * 迭代是fast-fail机制，一次性返回了所有集合视图方法
 * 当迭代器被创建后，对hashMap进行结构化修改会引发并发修改异常
 * 该异常仅适用于检测错误
 */
@SuppressWarnings("all")
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /*
     * 注意事项
     * hashMap在一定情况下会转化为红黑树（太大）
     * 也会退回链表（变小）
     */

    /**
     * 默认的初始容量为16 一定是2的幂次方
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * hashMap最大容量 2^30
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认的装载因子为0.75
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 由列表转为红黑树的阈值为8
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 由红黑树转为列表的阈值为6
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 由列表转为红黑树最小表容量为64
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * hashMap中存储的为Node节点
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        // 链接下一个节点
        Node<K,V> next;
    }

    /**
     * 计算key的hash值
     * 如果key为null hash值为0
     * 否则将key的hashCode的低16位与高16位进行异或运算
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 将容量变为2的幂次方
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        // 无符号右移
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * table数组将会在第一次使用时初始化，并调整容量为2的幂次方
     */
    transient Node<K,V>[] table;

    /* ---------------- Public operations -------------- */

    /**
     * 构造函数
     * @param initialCapacity 初始化容量
     * @param loadFactor 装载因子
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;

        // 构造函数时便适配容量为2的幂次方
        this.threshold = tableSizeFor(initialCapacity);
    }

    /**
     * 通过键获取值
     * 不存在返回null
     */
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        // 如果节点不为空 && 节点长度大于0 && fisrt不等于空
        if ((tab = table) != null && (n = tab.length) > 0 &&
                // first 在这一步已经进行了一个与运算找出来在table数组中对于下表
            (first = tab[(n - 1) & hash]) != null) {
            // 如果哈希相等 && （key相等且不为空）
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            // 如果first列表中第一个不匹配 则判断是否有下一个节点元素
            if ((e = first.next) != null) {
                // 如果是红黑树
                if (first instanceof TreeNode) {
                    // 强转  并调用红黑树的获取方法
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                }
                // 列表的循环
                do {
                    // 如果哈希相等 && （key相等且不为空）
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }

    /**
     * 关联key、value并放入hashMap中
     * 如果事先存在，则对应value被替换
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * put的具体实现
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        // 如果hashMap中的数组为空或者长度为0
        if ((tab = table) == null || (n = tab.length) == 0)
            // 初始化数组并返回长度
            n = (tab = resize()).length;
        // 如果数组对应下标没有node 则直接放入
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
            // 存在第一个node 并且 key相等 则替换掉
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            // 红黑树操作
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            // 第一个node key不相等 并且是列表
            else {
                // 死循环
                for (int binCount = 0; ; ++binCount) {
                    // 找到列表最后一个node 并在其后面插入
                    // 尾插法
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        // 如果达到转化红黑树的阈值 这里不管
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    // 找着找着就找到了相同的下面就就行替换值
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    // 这里相当于p=p.next
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                // 没有实现的垃圾方法
                afterNodeAccess(e);
                return oldValue;
            }
        }
        // node的实际数量大于阈值就reHashed
        if (++size > threshold)
            resize();
        // 没有实现的垃圾方法
        afterNodeInsertion(evict);
        return null;
    }

    /**
     * 这里就是我们开头所说的reHashed操作
     * 初始化或者2倍扩容
     * 如果table为空 则分配初始化容量（默认16）
     * 否则进行扩容，扩容后的元素要么在原来的位置，要么移动到2的幂次方的位置（如原位置16，则移动后为32）
     */
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        // 如果老容量大于0 也就是说oldTab不为空
        if (oldCap > 0) {
            // 如果oldCab已经超过最大容量 则修改一下阈值便返回
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // 否则可以进行扩容
            // oldCab * 2 小于最大容量 && oldCab > 16
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                // oldThr * 2
                newThr = oldThr << 1; // double threshold
        }
        // oldCab为空 阈值却大于0
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            // 初始化
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        // 这种情况 oldCab存在 并且小于最大容量
        // newCab大于最大容量 或者 oldCap小于16
        if (newThr == 0) {
            // 直接计算出阈值 并不能大于Integer.MAX_VALUE
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        // 扩容完毕 真正的reHashed操作
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                // 如果oldTab中的第一个元素不为空
                if ((e = oldTab[j]) != null) {
                    // 置为空
                    oldTab[j] = null;
                    // 如果没有下一个node节点
                    if (e.next == null)
                        // 则重新计算hash进行放置
                        newTab[e.hash & (newCap - 1)] = e;
                    // 否则判断是否是红黑树
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    // 这里是列表的put操作 并且是下一个node节点不为空的情况下
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        // 看不懂的循环 拜拜 以后再说
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

    /**
     * 移除元素 返回被移除的袁术
     */
    public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }


    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        if ((tab = table) != null && (n = tab.length) > 0 &&
                // 找出数组对应下表
            (p = tab[index = (n - 1) & hash]) != null) {

            Node<K,V> node = null, e; K k; V v;
            // 第一个node就相等
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            // 判断是否有下一个元素
            else if ((e = p.next) != null) {
                if (p instanceof TreeNode)
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {
                    // 循环判断是否相等
                    do {
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        // 这里直接把e赋值给了p
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            // matchValue不比较值是否相等
            // 只要node不等于null就能进去
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                // 红黑树先不管
                if (node instanceof TreeNode)
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                //
                else if (node == p)
                    tab[index] = node.next;
                else
                    p.next = node.next;

                ++modCount;
                --size;
                // 回调 没有具体实现
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }
}
