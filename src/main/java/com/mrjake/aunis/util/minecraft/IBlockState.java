package com.mrjake.aunis.util.minecraft;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;

import com.mrjake.aunis.util.BlockFaceShape;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public interface IBlockState {

    Collection < IProperty<? >> getPropertyKeys();

    <T extends Comparable<T>> T getValue(IProperty<T> property);

    <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value);

    <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property);

    ImmutableMap<IProperty, Comparable> getProperties();

    Block getBlock();
}
