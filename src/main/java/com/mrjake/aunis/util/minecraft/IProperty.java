package com.mrjake.aunis.util.minecraft;

import com.google.common.base.Optional;

import java.util.Collection;

public interface IProperty<T extends Comparable> {

    String getName();

    Collection<T> getAllowedValues();

    Class<T> getValueClass();

    /**
     * Get the name for the given value.
     */
    String getName(T value);
}
