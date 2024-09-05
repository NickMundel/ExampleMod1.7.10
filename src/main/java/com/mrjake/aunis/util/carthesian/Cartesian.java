package com.mrjake.aunis.util.carthesian;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.lang.reflect.Array;
import java.util.List;

public class Cartesian {

    public static <T> Iterable<T[]> cartesianProduct(Class<T> clazz, Iterable<? extends Iterable<? extends T>> sets) {
        return new Product(clazz, (Iterable[]) toArray(Iterable.class, sets));
    }

    public static <T> Iterable<List<T>> cartesianProduct(Iterable<? extends Iterable<? extends T>> sets) {
        /**
         * Convert an Iterable of Arrays (Object[]) to an Iterable of Lists
         */
        return arraysAsLists(cartesianProduct(Object.class, sets));
    }

    static <T> Iterable<List<T>> arraysAsLists(Iterable<Object[]> arrays) {
        return Iterables.transform(arrays, new GetList());
    }

    static <T> T[] toArray(Class<? super T> clazz, Iterable<? extends T> it) {
        List<T> list = Lists.<T>newArrayList();

        for (T t : it) {
            list.add(t);
        }

        return (T[]) ((Object[]) list.toArray(createArray(clazz, list.size())));
    }

    static <T> T[] createArray(Class<? super T> p_179319_0_, int p_179319_1_) {
        return (T[]) ((Object[]) ((Object[]) Array.newInstance(p_179319_0_, p_179319_1_)));
    }
}
