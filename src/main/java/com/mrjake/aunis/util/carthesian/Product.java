package com.mrjake.aunis.util.carthesian;

import java.util.Collections;
import java.util.Iterator;

public class Product<T> implements Iterable<T[]> {

    private final Class<T> clazz;
    private final Iterable<? extends T>[] iterables;

    Product(Class<T> clazz, Iterable<? extends T>[] iterables) {
        this.clazz = clazz;
        this.iterables = iterables;
    }

    public Iterator<T[]> iterator() {
        if (this.iterables.length <= 0) {
            return Collections.singletonList((T[]) Cartesian.createArray(this.clazz, 0)).iterator();
        } else {
            return new ProductIterator<>(this.clazz, this.iterables);
        }
    }

}
