package com.mrjake.aunis.tileentity.util;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;

public class ComparatorHelper {

    public static int getComparatorLevel(IInventory inventory, int startingIndex) {
        return calcRedstoneFromInventory(inventory, startingIndex);
    }

    public static int getComparatorLevel(FluidTank fluidTank) {
        if (fluidTank.getFluidAmount() == 0)
            return 0;

        float percent = fluidTank.getFluidAmount() / (float)fluidTank.getCapacity();
        return Math.round(1 + (percent * 14));
    }

    public static int getComparatorLevel(IEnergyReceiver energyReceiver) {
        if (energyReceiver.getEnergyStored(null) == 0)
            return 0;

        float percent = energyReceiver.getEnergyStored(null) / (float)energyReceiver.getMaxEnergyStored(null);
        return Math.round(1 + (percent * 14));
    }

    /**
     * Copied from {@link Container#calcRedstoneFromInventory(IInventory)}. Added starting index
     * This method uses the standard vanilla algorithm to calculate a comparator output for how "full" the inventory is.
     * @param inv The inventory to test.
     * @return A redstone value in the range [0,15] representing how "full" this inventory is.
     */
    public static int calcRedstoneFromInventory(IInventory inv, int startingIndex) {
        if (inv == null) {
            return 0;
        } else {
            int itemsFound = 0;
            float proportion = 0.0F;

            for (int j = startingIndex; j < inv.getSizeInventory(); ++j) {
                ItemStack itemstack = inv.getStackInSlot(j);

                if (itemstack != null) {
                    proportion += (float)itemstack.stackSize / (float)Math.min(inv.getInventoryStackLimit(), itemstack.getMaxStackSize());
                    ++itemsFound;
                }
            }

            proportion = proportion / (float)inv.getSizeInventory();
            return MathHelper.floor_float(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
        }
    }
}
