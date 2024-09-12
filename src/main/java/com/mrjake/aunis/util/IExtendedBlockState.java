package com.mrjake.aunis.util;

import com.google.common.collect.ImmutableMap;
import com.mrjake.aunis.util.minecraft.IBlockState;

import java.util.Collection;
import java.util.Optional;

public interface IExtendedBlockState extends IBlockState {
    Collection<IUnlistedProperty<?>> getUnlistedNames();

    <V> V getValue(IUnlistedProperty<V> var1);

    <V> IExtendedBlockState withProperty(IUnlistedProperty<V> var1, V var2);

    ImmutableMap<IUnlistedProperty<?>, Optional<?>> getUnlistedProperties();

    IBlockState getClean();
}
