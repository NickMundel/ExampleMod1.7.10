package com.mrjake.aunis.util.blockstates;

import com.google.common.collect.ImmutableMap;
import com.mrjake.aunis.util.minecraft.IBlockState;
import com.mrjake.aunis.util.minecraft.IProperty;
import net.minecraft.block.Block;

import java.util.Collection;
import java.util.List;

public class MetaBlockState implements IBlockState {

    protected Block block;
    public int meta;

    public MetaBlockState(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }

    @Override
    public Collection<IProperty<?>> getPropertyKeys() {
        return List.of();
    }

    public <T extends Comparable<T>> T getValue(IProperty<T> property) {
        return null;
    }

    public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
        return null;
    }

    public <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property) {
        return null;
    }

    public ImmutableMap<IProperty, Comparable> getProperties() {
        return null;
    }

    public Block getBlock() {
        return block;
    }

}
