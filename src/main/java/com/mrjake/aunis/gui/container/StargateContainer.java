package com.mrjake.aunis.gui.container;

import com.mrjake.aunis.block.AunisBlocks;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.packet.StateUpdatePacketToClient;
import com.mrjake.aunis.stargate.power.StargateClassicEnergyStorage;
import com.mrjake.aunis.state.StateTypeEnum;
import com.mrjake.aunis.tileentity.stargate.StargateClassicBaseTile;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.minecraft.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class StargateContainer extends Container implements OpenTabHolderInterface {

	public StargateClassicBaseTile gateTile;

	private BlockPos pos;
	private int lastEnergyStored;
	private int energyTransferedLastTick;
	private float lastEnergySecondsToClose;
	private int lastProgress;
	private int openTabId = -1;

	@Override
	public int getOpenTabId() {
		return openTabId;
	}

	@Override
	public void setOpenTabId(int tabId) {
		openTabId = tabId;
	}

	public StargateContainer(IInventory playerInventory, World world, int x, int y, int z) {
		pos = new BlockPos(x, y, z);
		gateTile = (StargateClassicBaseTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
		//IItemHandler itemHandler = gateTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        IInventory itemHandler =  gateTile.

		// Upgrades 2x2 (index 0-3)
		for (int row=0; row<2; row++) {
			for (int col=0; col<2; col++) {
				addSlotToContainer(new Slot(itemHandler, row*2+col, 9+18*col, 18+18*row));
			}
		}

		// Capacitors 1x3 (index 4-6)
		for (int col=0; col<3; col++) {
			final int capacitorIndex = col;

			addSlotToContainer(new Slot(itemHandler, col+4, 115+18*col, 40) {
				public boolean isEnabled() {
					// getHasStack() is a compatibility thing for when players already had their capacitors in the gate.
					return (capacitorIndex+1 <= gateTile.getSupportedCapacitors()) || getHasStack();
				}
			});
		}

		// Page slots (index 7-9)
		for (int i=0; i<3; i++) {
			addSlotToContainer(new Slot(itemHandler, i+7, -22, 89+22*i));
		}

		// Biome overlay slot (index 10)
		addSlotToContainer(new Slot(itemHandler, 10, 0, 0));

		for (Slot slot : ContainerHelper.generatePlayerSlots(playerInventory, 86))
			addSlotToContainer(slot);
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

	@Override
	public void updateProgressBar(int id, int data) {
		gateTile.setPageProgress(data);
	}

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack stack = getSlot(index).getStack();

		// Transfering from Stargate to player's inventory
        if (index < 11) {
        	if (!mergeItemStack(stack, 11, inventorySlots.size(), false)) {
        		return null;
        	}

			putStackInSlot(index, null);
        }

		// Transfering from player's inventory to Stargate
        else {
        	// Capacitors
        	if (stack.getItem() == Item.getItemFromBlock(AunisBlocks.CAPACITOR_BLOCK)) {
        		for (int i=4; i<7; i++) {
        			if (!getSlot(i).getHasStack() && getSlot(i).isItemValid(stack)) {
        				ItemStack stack1 = stack.copy();
                        BaseUtils.setCount(stack1, 1);

	        			putStackInSlot(i, stack1);
                        BaseUtils.shrink(stack, 1);

	        			return stack;
	        		}
        		}
        	}

        	else if (StargateClassicBaseTile.StargateUpgradeEnum.contains(stack.getItem()) && !gateTile.hasUpgrade(stack.getItem())) {
        		for (int i=0; i<4; i++) {
        			if (!getSlot(i).getHasStack()) {
        				ItemStack stack1 = stack.copy();
                        BaseUtils.setCount(stack1, 1);

        				putStackInSlot(i, stack1);
                        BaseUtils.shrink(stack, 1);

        				return null;
        			}
        		}
        	}

        	else if (openTabId >= 0 && openTabId <= 2 && getSlot(7+openTabId).isItemValid(stack)) {
    			if (!getSlot(7+openTabId).getHasStack()) {
    				ItemStack stack1 = stack.copy();
    				BaseUtils.setCount(stack1, 1);

    				putStackInSlot(7+openTabId, stack1);
    				BaseUtils.shrink(stack, 1);

    				return null;
    			}
        	}

        	// Biome override blocks
        	else if (openTabId == 3 && getSlot(10).isItemValid(stack)) {
        		if (!getSlot(10).getHasStack()) {
        			ItemStack stack1 = stack.copy();
    				BaseUtils.setCount(stack1, 1);

    				putStackInSlot(10, stack1);
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

		StargateClassicEnergyStorage energyStorage = (StargateClassicEnergyStorage) gateTile.getCapability(CapabilityEnergy.ENERGY, null);

		if (lastEnergyStored != energyStorage.getEnergyStoredInternally() || lastEnergySecondsToClose != gateTile.getEnergySecondsToClose() || energyTransferedLastTick != gateTile.getEnergyTransferedLastTick()) {
			for (IContainerListener listener : listeners) {
				if (listener instanceof EntityPlayerMP) {
					AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, StateTypeEnum.GUI_UPDATE, gateTile.getState(StateTypeEnum.GUI_UPDATE)), (EntityPlayerMP) listener);
				}
			}

			lastEnergyStored = energyStorage.getEnergyStoredInternally();
			energyTransferedLastTick = gateTile.getEnergyTransferedLastTick();
			lastEnergySecondsToClose = gateTile.getEnergySecondsToClose();
		}

		if (lastProgress != gateTile.getPageProgress()) {
			for (IContainerListener listener : listeners) {
				listener.sendWindowProperty(this, 0, gateTile.getPageProgress());
			}

			lastProgress = gateTile.getPageProgress();
		}
	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);

		if (listener instanceof EntityPlayerMP)
			AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(pos, StateTypeEnum.GUI_STATE, gateTile.getState(StateTypeEnum.GUI_STATE)), (EntityPlayerMP) listener);
	}
}
