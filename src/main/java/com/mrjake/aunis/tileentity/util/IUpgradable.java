package com.mrjake.aunis.tileentity.util;

import com.mrjake.aunis.util.EnumKeyInterface;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Iterator;
import java.util.stream.IntStream;

/**
 * Simple interface to allow upgrades insert into TE. `tryInsertStack` should be triggered in block class on interact
 */
public interface IUpgradable {

    public default boolean hasUpgrade(EnumKeyInterface<Item> upgrade) {
        return hasUpgrade(upgrade.getKey());
    }

    public default boolean hasUpgrade(Item item) {
        final Iterator<Integer> iter = getUpgradeSlotsIterator();

        while (iter.hasNext()) {
            int slot = iter.next();
            if(getStackInSlot(slot) != null && getStackInSlot(slot).getItem() == item) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get upgrade slot iterator. Used in interface. You can use `IntStream.range(min, max).iterator()`
     * @return upgrade slot iterator
     */
    public default Iterator<Integer> getUpgradeSlotsIterator(){
        return IntStream.range(0, getSizeInventory()).iterator();
    }

    /**
     * Try insert upgrade item into TE
     * @param player player who inserted upgrade
     * @return true if inserted successfully, false if not
     */
    public default boolean tryInsertUpgrade(EntityPlayer player){
        ItemStack stack = player.getHeldItem();
        if(stack == null)
            return false;

        Iterator<Integer> iter = getUpgradeSlotsIterator();
        while (iter.hasNext()) {
            int slot = iter.next();
            if(getStackInSlot(slot) == null && isItemValidForSlot(slot, stack)) {
                // Maybe should not take item in creative mode
                player.setCurrentItemOrArmor(0, insertItem(slot, stack, false));
                return true;
            }
        }
        return false;
    }

    /**
     * Get the stack in the given slot.
     * @param slot The slot to retrieve from.
     * @return The stack in the slot. May be null.
     */
    ItemStack getStackInSlot(int slot);

    /**
     * Inserts an item into the given slot and returns any remainder.
     * @param slot The slot to insert into.
     * @param stack The stack to insert.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining stack that was not inserted (if the entire stack is accepted, then this returns null).
     */
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    /**
     * Checks if the given stack is valid for the given slot.
     * @param slot The slot to check for validity
     * @param stack The stack to check for validity
     * @return True if the stack can be inserted
     */
    boolean isItemValidForSlot(int slot, ItemStack stack);

    /**
     * Returns the number of slots in the inventory.
     * @return The number of slots in the inventory.
     */
    int getSizeInventory();
}
