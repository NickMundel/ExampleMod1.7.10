package com.mrjake.aunis.util;

public interface IUnlistedProperty<V>
{
    String getName();

    boolean isValid(V value);

    Class<V> getType();

    String valueToString(V value);
}
