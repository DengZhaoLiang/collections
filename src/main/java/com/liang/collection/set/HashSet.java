
package com.liang.collection.set;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * HashSet类实现了Set接口，底层由hash表支持（基于HashMap实现）。
 * 不能保证集合的迭代顺序；特别是它不能保证元素的顺序不随时间而改变
 * HashSet允许Null类型的元素。
 *
 * 如果hash函数能够在桶中合理的分散元素，HashSet能够为该类基本的操作（add、remove、contains、size）提供效率的保证。
 * 迭代HashSet集合需要的时间是和集合元素的数量以及桶的大小成比例的。
 * 由此，如果想提高效率，就不要将集合的初始容量设置太大（或者加载因子设置太小）
 *
 * HashSet类不是同步的，如果多个线程同时访问这个集合，并且至少一个线程对集合进行修改，那么必须要保证同步。
 * 典型的实现方式是：通过同步一些对象（该集合中的元素都报错在该对象中，例如同步HashSet集合中的map对象）。
 * 如果这种对象不存在，又想同步集合，可以这样写：
 *   Set s = Collections.synchronizedSet(new HashSet(...));
 *
 * 通过集合的iterator方法可以返回迭代器。
 * 这个迭代器实现了快速报错。快速报错（fail-fast）：
 * 如果在生成迭代器后，集合被修改（除了迭代器remove方法），迭代器将抛出异常ConcurrentModificationException。
 * 因此，在并发修改的情况下，迭代器会迅速失败，而不会去等待。
 * 注意，也不能保证在非并发修改的情况下，快速报错不会被触发，迭代器只能尽力而为。
 * 因此，不应该编写一段依赖ConcurrentModificationException异常的程序。迭代器的快速报错应该只用于检测Bug.
 */
@SuppressWarnings("all")
public class HashSet<E>
        extends AbstractSet<E>
        implements Set<E>, Cloneable, java.io.Serializable {
    static final long serialVersionUID = -5024744406713321676L;

    private transient HashMap<E, Object> map;

    // Dummy value to associate with an Object in the backing Map
    // 与后台映射中的对象相关联的空值
    private static final Object PRESENT = new Object();

    /**
     * 无参构造
     */
    public HashSet() {
        map = new HashMap<>();
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    /**
     * 集合如果为空则抛出NPE异常
     * (c.size() / .75f) + 1 一般建议hashMap和hashSet指定初始化容量时采用这种写法
     */
    public HashSet(Collection<? extends E> c) {
        map = new HashMap<>(Math.max((int) (c.size() / .75f) + 1, 16));
        addAll(c);
    }

    public HashSet(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }

    public HashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * 增加一个元素key
     * 返回的旧值如果为null则表明添加成功
     */
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    /**
     * 移除一个元素key
     * 返回的旧值如果为PRESENT则表明移除成功
     * 也就是说hashSet中的元素其实为 key 对应于hashMap中的值为new Object()
     */
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }
}
