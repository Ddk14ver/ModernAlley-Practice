package dev.revere.alley.common.collection.internal;

import dev.revere.alley.common.collection.Triple;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a mutable container for three objects of potentially different types.
 * 表示一个可变的容器，用于存放三个可能不同类型的对象。
 * Provides a simple implementation for a collection tuple.
 * 提供了一个集合元组的简单实现。
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
@Getter
@Setter
public class MutableTriple<A, B, C> implements Triple<A, B, C> {
    private A a;
    private B b;
    private C c;

    /**
     * Constructor for the MutableTriple class.
     * MutableTriple类的构造函数。
     *
     * @param a the first object
     *        第一个对象
     * @param b the second object
     *        第二个对象
     * @param c the third object
     *        第三个对象
     */
    public MutableTriple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}