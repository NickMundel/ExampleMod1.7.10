package com.mrjake.aunis.util.blockstates;

import com.google.common.collect.*;
import com.mrjake.aunis.util.minecraft.IBlockState;
import com.mrjake.aunis.util.minecraft.IProperty;
import net.minecraft.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StateImplementation extends BlockStateBase {

    private final Block block;
    private final ImmutableMap<IProperty, Comparable> properties;
    protected ImmutableTable<IProperty, Comparable, IBlockState> propertyValueTable;

    protected StateImplementation(Block blockIn, ImmutableMap<IProperty, Comparable> propertiesIn) {
        this.block = blockIn;
        this.properties = propertiesIn;
    }

    @Override
    public Collection<IProperty<?>> getPropertyKeys() {
        return Collections. < IProperty<? >> unmodifiableCollection((Collection<? extends IProperty<?>>) this.properties.keySet());
    }

    public <T extends Comparable<T>> T getValue(IProperty<T> property) {
        if (!this.properties.containsKey(property)) {
            throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + this);
        } else {
            return (T) ((Comparable) property.getValueClass().cast(this.properties.get(property)));
        }
    }

    public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
        if (!this.properties.containsKey(property)) {
            throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this);
        } else if (!property.getAllowedValues().contains(value)) {
            throw new IllegalArgumentException(
                "Cannot set property " + property
                    + " to "
                    + value
                    + " on block "
                    + Block.blockRegistry.getNameForObject(this.block)
                    + ", it is not an allowed value");
        } else {
            return (IBlockState) (this.properties.get(property) == value ? this
                : (IBlockState) this.propertyValueTable.get(property, value));
        }
    }

    public ImmutableMap<IProperty, Comparable> getProperties() {
        return this.properties;
    }

    public Block getBlock() {
        return this.block;
    }

    public boolean equals(Object p_equals_1_) {
        return this == p_equals_1_;
    }

    public int hashCode() {
        return this.properties.hashCode();
    }

    public void buildPropertyValueTable(Map<Map<IProperty, Comparable>, StateImplementation> map) {
        if (this.propertyValueTable != null) {
            throw new IllegalStateException();
        } else {
            Table<IProperty, Comparable, IBlockState> table = HashBasedTable
                .<IProperty, Comparable, IBlockState>create();

            for (IProperty<? extends Comparable> iproperty : this.properties.keySet()) {
                for (Comparable comparable : iproperty.getAllowedValues()) {
                    if (comparable != this.properties.get(iproperty)) {
                        table.put(iproperty, comparable, map.get(this.getPropertiesWithValue(iproperty, comparable)));
                    }
                }
            }

            this.propertyValueTable = ImmutableTable.<IProperty, Comparable, IBlockState>copyOf(table);
        }
    }

    private Map<IProperty, Comparable> getPropertiesWithValue(IProperty property, Comparable value) {
        Map<IProperty, Comparable> map = Maps.<IProperty, Comparable>newHashMap(this.properties);
        map.put(property, value);
        return map;
    }

    public ImmutableTable<IProperty, Comparable, IBlockState> getPropertyValueTable() {
        /** Lookup-table for IBlockState instances. This is a Table<Property, Value, State>. */
        return propertyValueTable;
    }
}
