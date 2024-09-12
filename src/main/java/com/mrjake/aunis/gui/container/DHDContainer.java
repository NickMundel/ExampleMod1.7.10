package com.mrjake.aunis.gui.container;

import com.mrjake.aunis.item.AunisItems;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.packet.StateUpdatePacketToClient;
import com.mrjake.aunis.state.StateTypeEnum;
import com.mrjake.aunis.tileentity.DHDTile;
import com.mrjake.aunis.tileentity.util.ReactorStateEnum;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.minecraft.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;

public class DHDContainer extends Container implements OpenTabHolderInterface {

	public Slot slotCrystal;
	public FluidTank tankNaquadah;
	public DHDTile dhdTile;

	private BlockPos pos;
	private int tankLastAmount;
	private ReactorStateEnum lastReactorState;
	private boolean lastLinked;
	private int openTabId = -1;

	@Override
	public int getOpenTabId() {
		return openTabId;
	}

	@Override
	public void setOpenTabId(int tabId) {
		openTabId = tabId;
	}

	public DHDContainer(IInventory playerInventory, World world, int x, int y, int z) {
		pos = new BlockPos(x, y, z);
		dhdTile = (DHDTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
		IItemHandler itemHandler = dhdTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		// Crystal slot (index 0)
		slotCrystal = new SlotItemHandler(itemHandler, 0, 80, 35);
		addSlotToContainer(slotCrystal);

		tankNaquadah = (FluidTank) dhdTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);

		// Upgrades (index 1-4)
		for (int row=0; row<2; row++) {
			for (int col=0; col<2; col++) {
				addSlotToContainer(new SlotItemHandler(itemHandler, row*2+col+1, 9+18*col, 18+18*row));
			}
		}

		// Biome overlay slot (index 5)
		addSlotToContainer(new SlotItemHandler(itemHandler, 5, 0, 0));

		for (Slot slot : ContainerHelper.generatePlayerSlots(playerInventory, 86))
			addSlotToContainer(slot);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack stack = getSlot(index).getStack();

		// Transfering from DHD to player's inventory
        if (index < 6) {
        	if (!mergeItemStack(stack, 5, inventorySlots.size(), false)) {
        		return null;
        	}

        	putStackInSlot(index, null);
        }

		// Transfering from player's inventory to DHD
        else {
        	if (stack.getItem() == AunisItems.CRYSTAL_CONTROL_DHD) {
        		if (!slotCrystal.getHasStack()) {
        			ItemStack stack1 = stack.copy();
    				BaseUtils.setCount(stack1, 1);
        			slotCrystal.putStack(stack1);

    				BaseUtils.shrink(stack, 1);

                	return null;
        		}
        	}

        	else if (DHDTile.SUPPORTED_UPGRADES.contains(stack.getItem()) && !dhdTile.hasUpgrade(stack.getItem())) {
        		for (int i=1; i<5; i++) {
        			if (!getSlot(i).getHasStack()) {
        				ItemStack stack1 = stack.copy();
        				BaseUtils.setCount(stack1, 1);

        				putStackInSlot(i, stack1);
        				BaseUtils.shrink(stack, 1);

        				return stack;
        			}
        		}
        	}

        	// Biome override blocks
        	else if (openTabId == 0 && getSlot(5).isItemValid(stack)) {
        		if (!getSlot(5).getHasStack()) {
        			ItemStack stack1 = stack.copy();
    				BaseUtils.setCount(stack1, 1);

    				putStackInSlot(5, stack1);
    				BaseUtils.shrink(stack, 1);

    				return null;
        		}
        	}

        	return null;
        }

        return stack;
    }

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		if (tankLastAmount != tankNaquadah.getFluidAmount() || lastReactorState != dhdTile.getReactorState() || lastLinked != dhdTile.isLinked()) {
			for (IContainerListener listener : listeners) {
				if (listener instanceof EntityPlayerMP) {
					AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, StateTypeEnum.GUI_UPDATE, new DHDContainerGuiUpdate(tankNaquadah.getFluidAmount(), tankNaquadah.getCapacity(), dhdTile.getReactorState(), dhdTile.isLinked())), (EntityPlayerMP) listener);
				}
			}

			tankLastAmount = tankNaquadah.getFluidAmount();
			lastReactorState = dhdTile.getReactorState();
			lastLinked = dhdTile.isLinked();
		}
	}
}
