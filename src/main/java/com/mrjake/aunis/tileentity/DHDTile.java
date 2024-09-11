package com.mrjake.aunis.tileentity;

import cofh.api.energy.IEnergyStorage;
import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.block.AunisBlocks;
import com.mrjake.aunis.config.AunisConfig;
import com.mrjake.aunis.item.AunisItems;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.packet.StateUpdatePacketToClient;
import com.mrjake.aunis.packet.StateUpdateRequestToServer;
import com.mrjake.aunis.renderer.DHDRendererState;
import com.mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import com.mrjake.aunis.sound.AunisSoundHelper;
import com.mrjake.aunis.sound.SoundEventEnum;
import com.mrjake.aunis.stargate.network.StargateAddressDynamic;
import com.mrjake.aunis.stargate.network.SymbolMilkyWayEnum;
import com.mrjake.aunis.stargate.network.SymbolTypeEnum;
import com.mrjake.aunis.state.*;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import com.mrjake.aunis.tileentity.util.IUpgradable;
import com.mrjake.aunis.tileentity.util.ReactorStateEnum;
import com.mrjake.aunis.util.*;
import com.mrjake.aunis.util.minecraft.AxisAlignedBB;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.IBlockState;
import com.mrjake.aunis.util.minecraft.Rotation;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class DHDTile extends BaseTileEntity implements ILinkable, IUpgradable, StateProviderInterface, ITickable {

	// ---------------------------------------------------------------------------------------------------
	// Gate linking

	private BlockPos linkedGate = null;

	public void rotate(Rotation rotation) {
		IBlockState state = BaseUtils.getWorldBlockState(worldObj, pos);

		int rotationOrig = state.getValue(AunisProps.ROTATION_HORIZONTAL);
	    BaseUtils.setWorldBlockState(worldObj, pos, state.withProperty(AunisProps.ROTATION_HORIZONTAL, rotation.rotate(rotationOrig, 16)));
    }

	public void setLinkedGate(BlockPos gate) {
		this.linkedGate = gate;

		markDirty();
	}

	public boolean isLinked() {
		return this.linkedGate != null;
	}

	public StargateAbstractBaseTile getLinkedGate(IBlockAccess worldObj) {
		if (linkedGate == null)
			return null;

		return (StargateAbstractBaseTile) worldObj.getTileEntity(linkedGate.getX(), linkedGate.getY(), linkedGate.getZ());
	}

	@Override
	public boolean canLinkTo() {
		return !isLinked();
	}

	// ---------------------------------------------------------------------------------------------------
	// Renderer state

	private DHDRendererState rendererStateClient;

	public DHDRendererState getRendererStateClient() {
		return rendererStateClient;
	}

	// ---------------------------------------------------------------------------------------------------
	// Loading and ticking

	private TargetPoint targetPoint;
	private ReactorStateEnum reactorState = ReactorStateEnum.STANDBY;

	public ReactorStateEnum getReactorState() {
		return reactorState;
	}

	@Override
	public void onLoad() {
		if (!worldObj.isRemote) {
			targetPoint = new TargetPoint(worldObj.provider.dimensionId, pos.getX(), pos.getY(), pos.getZ(), 512);
			hadControlCrystal = hasControlCrystal();
		}

		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.RENDERER_STATE));
		}
	}

	@Override
	public void tick() {
		if (!worldObj.isRemote) {

			// Has crystal
			if (hasControlCrystal()) {
				if (isLinked()) {
					StargateAbstractBaseTile gateTile = getLinkedGate(worldObj);
					if (gateTile == null) {
						setLinkedGate(null);
						Aunis.LOG.error("Gate didn't unlink properly, forcing...");
						return;
					}

					IEnergyStorage energyStorage = (IEnergyStorage) gateTile.getCapability(CapabilityEnergy.ENERGY, null);

					int amount = 1 * AunisConfig.dhdConfig.powerGenerationMultiplier;

					if (reactorState != ReactorStateEnum.STANDBY) {
						FluidStack simulatedDrain = fluidHandler.drainInternal(amount, false);

						if (simulatedDrain != null && simulatedDrain.amount >= amount)
							reactorState = ReactorStateEnum.ONLINE;
						else
							reactorState = ReactorStateEnum.NO_FUEL;
					}

					if (reactorState == ReactorStateEnum.ONLINE || reactorState == ReactorStateEnum.STANDBY) {
						float percent = energyStorage.getEnergyStored() / (float)energyStorage.getMaxEnergyStored();
//						Aunis.info("state: " + reactorState + ", percent: " + percent);

						if (percent < AunisConfig.dhdConfig.activationLevel)
							reactorState = ReactorStateEnum.ONLINE;

						else if (percent >= AunisConfig.dhdConfig.deactivationLevel)
							reactorState = ReactorStateEnum.STANDBY;
					}

					if (reactorState == ReactorStateEnum.ONLINE) {
						fluidHandler.drainInternal(amount, true);
						energyStorage.receiveEnergy(AunisConfig.dhdConfig.energyPerNaquadah * AunisConfig.dhdConfig.powerGenerationMultiplier, false);
					}
				}

				// Not linked
				else {
					reactorState = ReactorStateEnum.NOT_LINKED;
				}
			}

			// No crystal
			else {
				reactorState = ReactorStateEnum.NO_CRYSTAL;
			}
		}

		else {
			// Client

			// Each 2s check for the sky
			if (worldObj.getTotalWorldTime() % 40 == 0 && rendererStateClient != null && getRendererStateClient().biomeOverride == null) {
				rendererStateClient.setBiomeOverlay(BiomeOverlayEnum.updateBiomeOverlay(worldObj, pos, SUPPORTED_OVERLAYS));
			}
		}
	}

	// Server
	private BiomeOverlayEnum determineBiomeOverride() {
		ItemStack stack = itemStackHandler.getStackInSlot(BIOME_OVERRIDE_SLOT);

		if (stack.isEmpty()) {
			return null;
		}

		BiomeOverlayEnum biomeOverlay = AunisConfig.stargateConfig.getBiomeOverrideItemMetaPairs().get(new ItemMetaPair(stack));

		if (getSupportedOverlays().contains(biomeOverlay)) {
			return biomeOverlay;
		}

		return null;
	}

	public static final EnumSet<BiomeOverlayEnum> SUPPORTED_OVERLAYS = EnumSet.of(
			BiomeOverlayEnum.NORMAL,
			BiomeOverlayEnum.FROST,
			BiomeOverlayEnum.MOSSY,
			BiomeOverlayEnum.SOOTY,
			BiomeOverlayEnum.AGED);

	public static EnumSet<BiomeOverlayEnum> getSupportedOverlays() {
		return SUPPORTED_OVERLAYS;
	}

	private boolean hadControlCrystal;

	public boolean hasControlCrystal() {
		return !itemStackHandler.getStackInSlot(0).isEmpty();
	}

	private void updateCrystal() {
		boolean hasControlCrystal = hasControlCrystal();

		if (hadControlCrystal != hasControlCrystal) {
			if (hasControlCrystal) {
				sendState(StateTypeEnum.RENDERER_STATE, getState(StateTypeEnum.RENDERER_STATE));
			}

			else {
				clearSymbols();
			}

			hadControlCrystal = hasControlCrystal;
		}
	}

	// -----------------------------------------------------------------------------
	// Symbol activation

	public void activateSymbol(SymbolMilkyWayEnum symbol) {
		// By Glen Jolley from his unaccepted PR
		StargateAbstractBaseTile gateTile = getLinkedGate(worldObj);

		// When using OC to dial, don't play sound of the DHD button press
		if (!gateTile.getStargateState().dialingComputer()) {

			if (symbol.brb())
				AunisSoundHelper.playSoundEvent(worldObj, pos, SoundEventEnum.DHD_MILKYWAY_PRESS_BRB);
			else
				AunisSoundHelper.playSoundEvent(worldObj, pos, SoundEventEnum.DHD_MILKYWAY_PRESS);
		}

        //TODO:
        //worldObj.notifyNeighborsOfStateChange(pos, AunisBlocks.DHD_BLOCK, true);

        sendState(StateTypeEnum.DHD_ACTIVATE_BUTTON, new DHDActivateButtonState(symbol));
	}

	public void clearSymbols() {
        //worldObj.notifyNeighborsOfStateChange(pos, AunisBlocks.DHD_BLOCK, true);

        sendState(StateTypeEnum.DHD_ACTIVATE_BUTTON, new DHDActivateButtonState(true));
	}


	// -----------------------------------------------------------------------------
	// States

	protected void sendState(StateTypeEnum type, State state) {
		if (worldObj.isRemote)
			return;

		if (targetPoint != null) {
			AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, type, state), targetPoint);
		}

		else {
            Aunis.LOG.debug("targetPoint was null trying to send " + type + " from " + this.getClass().getCanonicalName());
		}
	}

	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				StargateAddressDynamic address = new StargateAddressDynamic(SymbolTypeEnum.MILKYWAY);

				if (isLinked()) {
					StargateAbstractBaseTile gateTile = getLinkedGate(worldObj);

					address.addAll(gateTile.getDialedAddress());
					boolean brbActive = false;

					switch (gateTile.getStargateState()) {
						case ENGAGED_INITIATING:
							brbActive = true;
							break;

						case ENGAGED:
							address.clear();
							brbActive = true;
							break;

						default:
							break;
					}

					return new DHDRendererState(address, brbActive, determineBiomeOverride());
				}

				return new DHDRendererState(address, false, determineBiomeOverride());

			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return new DHDRendererState();

			case DHD_ACTIVATE_BUTTON:
				return new DHDActivateButtonState();

			case GUI_UPDATE:
				//return new DHDContainerGuiUpdate();
                break;

			case BIOME_OVERRIDE_STATE:
				return new StargateBiomeOverrideState();

			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}

	public boolean isLinkedClient;

	@Override
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_STATE:
				float horizontalRotation = BaseUtils.getWorldBlockState(worldObj, pos).getValue(AunisProps.ROTATION_HORIZONTAL) * -22.5f;
				rendererStateClient = ((DHDRendererState) state).initClient(pos, horizontalRotation, BiomeOverlayEnum.updateBiomeOverlay(worldObj, pos, SUPPORTED_OVERLAYS));

				break;

			case DHD_ACTIVATE_BUTTON:
				DHDActivateButtonState activateState = (DHDActivateButtonState) state;

				if (activateState.clearAll)
					getRendererStateClient().clearSymbols(worldObj.getTotalWorldTime());
				else
					getRendererStateClient().activateSymbol(worldObj.getTotalWorldTime(), activateState.symbol);

				break;

			case GUI_UPDATE:
				//DHDContainerGuiUpdate guiState = (DHDContainerGuiUpdate) state;

				//fluidHandler.setFluid(new FluidStack(AunisFluids.moltenNaquadahRefined, guiState.fluidAmount));
				//fluidHandler.setCapacity(guiState.tankCapacity);
				//reactorState = guiState.reactorState;
				//isLinkedClient = guiState.isLinked;

				break;

			case BIOME_OVERRIDE_STATE:
				StargateBiomeOverrideState overrideState = (StargateBiomeOverrideState) state;

				if (rendererStateClient != null) {
					getRendererStateClient().biomeOverride = overrideState.biomeOverride;
				}

				break;

			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}


	// -----------------------------------------------------------------------------
	// Item handler

	public static final List<Item> SUPPORTED_UPGRADES = Arrays.asList(
			AunisItems.CRYSTAL_GLYPH_DHD);

	public static final int BIOME_OVERRIDE_SLOT = 5;

	private final ItemStackHandler itemStackHandler = new AunisItemStackHandler(6) {

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			Item item = stack.getItem();

			switch (slot) {
				case 0:
					return item == AunisItems.CRYSTAL_CONTROL_DHD;

				case 1:
				case 2:
				case 3:
				case 4:
					return SUPPORTED_UPGRADES.contains(item) && !hasUpgrade(item);

				case BIOME_OVERRIDE_SLOT:
					BiomeOverlayEnum override = AunisConfig.stargateConfig.getBiomeOverrideItemMetaPairs().get(new ItemMetaPair(stack));
					if (override == null)
						return false;

					return getSupportedOverlays().contains(override);

				default:
					return true;
			}
		}

		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			return 1;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			super.setStackInSlot(slot, stack);

			if (!worldObj.isRemote && slot == 0) {
				// Crystal changed
				updateCrystal();
			}
		};

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			ItemStack out = super.extractItem(slot, amount, simulate);

			if (!worldObj.isRemote && slot == 0 && amount > 0 && !simulate) {
				// Removing crystal
				updateCrystal();
			}

			return out;
		}

		@Override
		protected void onContentsChanged(int slot) {
			switch (slot) {
				case BIOME_OVERRIDE_SLOT:
					sendState(StateTypeEnum.BIOME_OVERRIDE_STATE, new StargateBiomeOverrideState(determineBiomeOverride()));
					break;

				default:
					break;
			}

			super.onContentsChanged(slot);
			markDirty();
		}
	};

	// TODO Get rid of EnumKeyInterface
	public static enum DHDUpgradeEnum implements EnumKeyInterface<Item> {
		CHEVRON_UPGRADE(AunisItems.CRYSTAL_GLYPH_DHD);

		public Item item;

		private DHDUpgradeEnum(Item item) {
			this.item = item;
		}

		@Override
		public Item getKey() {
			return item;
		}
	}

	// -----------------------------------------------------------------------------
	// Fluid handler

//	private final FluidTank fluidHandler = new FluidTank(new FluidStack(AunisFluids.moltenNaquadahRefined, 0), AunisConfig.dhdConfig.fluidCapacity) {
//
//		@Override
//		public boolean canFillFluidType(FluidStack fluid) {
//			if (fluid == null)
//				return false;
//
//			return fluid.getFluid() == AunisFluids.moltenNaquadahRefined;
//		}
//
//		protected void onContentsChanged() {
//			markDirty();
//		}
//	};




	// ---------------------------------------------------------------------------------------------------
	// NBT

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		if (linkedGate != null)
			compound.setLong("linkedGate", linkedGate.toLong());

		compound.setTag("itemStackHandler", itemStackHandler.serializeNBT());

		NBTTagCompound fluidHandlerCompound = new NBTTagCompound();
		//fluidHandler.writeToNBT(fluidHandlerCompound);
		compound.setTag("fluidHandler", fluidHandlerCompound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);

		if (compound.hasKey("linkedGate")) {
			linkedGate = BlockPos.fromLong(compound.getLong("linkedGate"));
			if (linkedGate.equals(new BlockPos(0, 0, 0))) // 1.8 fix
				linkedGate = null;
		}

		itemStackHandler.deserializeNBT(compound.getCompoundTag("itemStackHandler"));

		if (compound.getBoolean("hasUpgrade") || compound.getBoolean("insertAnimation")) {
			itemStackHandler.setStackInSlot(1, new ItemStack(AunisItems.CRYSTAL_GLYPH_DHD));
		}

		//fluidHandler.readFromNBT(compound.getCompoundTag("fluidHandler"));

		if (compound.hasKey("inventory")) {
			NBTTagCompound inventoryTag = compound.getCompoundTag("inventory");
			NBTTagList tagList = inventoryTag.getTagList("Items", Constants.NBT.TAG_COMPOUND);

			if (tagList.tagCount() > 0) {
				itemStackHandler.setStackInSlot(0, new ItemStack(AunisItems.CRYSTAL_CONTROL_DHD));

				int energy = tagList.getCompoundTagAt(0).getCompoundTag("ForgeCaps").getCompoundTag("Parent").getInteger("energy");
				int fluidAmount = energy / AunisConfig.dhdConfig.energyPerNaquadah;
				//fluidHandler.fillInternal(new FluidStack(AunisFluids.moltenNaquadahRefined, fluidAmount), true);
			}
		}
	}


	// ---------------------------------------------------------------------------------------------------
	// Rendering distance

	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 2, 1));
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536;
	}
}
