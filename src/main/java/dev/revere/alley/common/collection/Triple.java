package dev.revere.alley.common.collection;


/**
 * A generic interface for a container that holds three objects of different types.
 * 一个泛型接口，用于存放三个不同类型的对象。
 * Provides methods to retrieve each of the three objects.
 * 提供获取三个对象中每一个的方法。
 *
 * @param <A> the type of the first object
 *        第一个对象的类型
 * @param <B> the type of the second object
 *        第二个对象的类型
 * @param <C> the type of the third object
 *        第三个对象的类型
 * @author Remi
 * @project Alley
 * @date 5/27/2024
 */
public interface Triple<A, B, C> {
    /**
     * Returns the first object in the collection.
     * 返回集合中的第一个对象。
     *
     * @return the first object
     *         第一个对象
     */
    A getA();

    /**
     * Returns the second object in the collection.
     * 返回集合中的第二个对象。
     *
     * @return the second object
     *         第二个对象
     */
    B getB();

    /**
     * Returns the third object in the collection.
     * 返回集合中的第三个对象。
     *
     * @return the third object
     *         第三个对象
     */
    C getC();
}