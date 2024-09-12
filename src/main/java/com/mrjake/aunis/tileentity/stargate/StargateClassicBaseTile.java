package com.mrjake.aunis.tileentity.stargate;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.config.AunisConfig;
import com.mrjake.aunis.gui.container.StargateContainerGuiState;
import com.mrjake.aunis.gui.container.StargateContainerGuiUpdate;
import com.mrjake.aunis.item.AunisItems;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.packet.StateUpdatePacketToClient;
import com.mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import com.mrjake.aunis.renderer.stargate.StargateClassicRendererState;
import com.mrjake.aunis.sound.StargateSoundEventEnum;
import com.mrjake.aunis.sound.StargateSoundPositionedEnum;
import com.mrjake.aunis.stargate.EnumScheduledTask;
import com.mrjake.aunis.stargate.EnumSpinDirection;
import com.mrjake.aunis.stargate.StargateClassicSpinHelper;
import com.mrjake.aunis.stargate.StargateClosedReasonEnum;
import com.mrjake.aunis.stargate.network.StargatePos;
import com.mrjake.aunis.stargate.network.SymbolInterface;
import com.mrjake.aunis.stargate.network.SymbolTypeEnum;
import com.mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import com.mrjake.aunis.stargate.power.StargateClassicEnergyStorage;
import com.mrjake.aunis.state.*;
import com.mrjake.aunis.tileentity.util.IUpgradable;
import com.mrjake.aunis.tileentity.util.ScheduledTask;
import com.mrjake.aunis.util.*;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.renderer.stargate.StargateClassicRendererState.StargateClassicRendererStateBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

import com.mrjake.aunis.state.StargateRendererActionState.EnumGateAction;

/**
 * This class wraps common behavior for the fully-functional Stargates i.e.
 * all of them (right now) except Orlin's.
 *
 * @author MrJake222
 *
 */
public abstract class StargateClassicBaseTile extends StargateAbstractBaseTile implements IUpgradable {

	// ------------------------------------------------------------------------
	// Stargate state

	protected boolean isFinalActive;

	@Override
	protected void engageGate() {
		super.engageGate();

		for (BlockPos beamerPos : linkedBeamers) {
			//((BeamerTile) worldObj.getTileEntity(beamerPos)).gateEngaged(targetGatePos);
		}
	}

	@Override
	public void closeGate(StargateClosedReasonEnum reason) {
		super.closeGate(reason);

		for (BlockPos beamerPos : linkedBeamers) {
			//((BeamerTile) worldObj.getTileEntity(beamerPos)).gateClosed();
		}
	}

	@Override
	protected void disconnectGate() {
		super.disconnectGate();

		isFinalActive = false;

		updateChevronLight(0, false);
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, dialedAddress.size(), isFinalActive);
	}

	@Override
	protected void failGate() {
		super.failGate();

		isFinalActive = false;

		updateChevronLight(0, false);
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, dialedAddress.size(), isFinalActive);
	}

	@Override
	public void openGate(StargatePos targetGatePos, boolean isInitiating) {
		super.openGate(targetGatePos, isInitiating);

		this.isFinalActive = true;
	}

	@Override
	public void incomingWormhole(int dialedAddressSize) {
		super.incomingWormhole(dialedAddressSize);

		isFinalActive = true;
		updateChevronLight(dialedAddressSize, isFinalActive);

		playSoundEvent(StargateSoundEventEnum.INCOMING);
		sendRenderingUpdate(EnumGateAction.LIGHT_UP_CHEVRONS, dialedAddressSize, true);
	}

	@Override
	public void onGateBroken() {
		super.onGateBroken();
		updateChevronLight(0, false);
		isSpinning = false;
		currentRingSymbol = getSymbolType().getTopSymbol();
		AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, StateTypeEnum.SPIN_STATE, new StargateSpinState(currentRingSymbol, spinDirection, true)), targetPoint);

		playPositionedSound(StargateSoundPositionedEnum.GATE_RING_ROLL, false);
		ItemHandlerHelper.dropInventoryItems(worldObj, pos, itemStackHandler);

//		for (BlockPos beamerPos : linkedBeamers) {
//			BeamerTile beamerTile = (BeamerTile) worldObj.getTileEntity(beamerPos);
//			beamerTile.setLinkedGate(null, null);
//		}

		linkedBeamers.clear();
	}

	@Override
	protected void onGateMerged() {
		super.onGateMerged();

		//BeamerLinkingHelper.findBeamersInFront(worldObj, pos, facing);
		updateBeamers();
	}


	// ------------------------------------------------------------------------
	// Loading and ticking

	@Override
	public void onLoad() {
		super.onLoad();

		if (!worldObj.isRemote) {
			updateBeamers();
			updatePowerTier();
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (!worldObj.isRemote) {
			if (givePageTask != null) {
				if (givePageTask.update(worldObj.getTotalWorldTime())) {
					givePageTask = null;
				}
			}

			if (doPageProgress) {
				if (worldObj.getTotalWorldTime() % 2 == 0) {
					pageProgress++;

					if (pageProgress > 18) {
						pageProgress = 0;
						doPageProgress = false;
					}
				}

				if (itemStackHandler.getStackInSlot(pageSlotId).isEmpty()) {
					lockPage = false;
					doPageProgress = false;
					pageProgress = 0;
					givePageTask = null;
				}
			}

			else {
				if (lockPage && itemStackHandler.getStackInSlot(pageSlotId).isEmpty()) {
					lockPage = false;
				}

				if (!lockPage) {
					for (int i=7; i<10; i++) {
						if (!itemStackHandler.getStackInSlot(i).isEmpty()) {
							doPageProgress = true;
							lockPage = true;
							pageSlotId = i;
							givePageTask = new ScheduledTask(EnumScheduledTask.STARGATE_GIVE_PAGE, 36);
							givePageTask.setTaskCreated(worldObj.getTotalWorldTime());
							givePageTask.setExecutor(this);

							break;
						}
					}
				}
			}
		}

		else {
			// Client

			// Each 2s check for the biome overlay
			if (worldObj.getTotalWorldTime() % 40 == 0 && rendererStateClient != null && getRendererStateClient().biomeOverride == null) {
				rendererStateClient.setBiomeOverlay(BiomeOverlayEnum.updateBiomeOverlay(worldObj, getMergeHelper().getTopBlock().add(pos), getSupportedOverlays()));
			}
		}
	}

	// Server
	private BiomeOverlayEnum determineBiomeOverride() {
		ItemStack stack = itemStackHandler.getStackInSlot(BIOME_OVERRIDE_SLOT);

		if (BaseUtils.isEmpty(stack)) {
			return null;
		}

		BiomeOverlayEnum biomeOverlay = AunisConfig.stargateConfig.getBiomeOverrideItemMetaPairs().get(new ItemMetaPair(stack));

		if (getSupportedOverlays().contains(biomeOverlay)) {
			return biomeOverlay;
		}

		return null;
	}

	@Override
	protected boolean shouldAutoclose() {
		boolean beamerActive = false;

		//for (BlockPos beamerPos : linkedBeamers) {
			//BeamerTile beamerTile = (BeamerTile) worldObj.getTileEntity(beamerPos);
			//beamerActive = beamerTile.isActive();

			//if (beamerActive)
			//	break;
		//}

		return !beamerActive && super.shouldAutoclose();
	}

	// ------------------------------------------------------------------------
	// NBT

	@Override
	protected void setWorldCreate(World worldObj) {
		setWorld(worldObj);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setTag("itemHandler", itemStackHandler.serializeNBT());
		compound.setBoolean("isFinalActive", isFinalActive);

		compound.setBoolean("isSpinning", isSpinning);
		compound.setLong("spinStartTime", spinStartTime);
		compound.setInteger("currentRingSymbol", currentRingSymbol.getId());
		compound.setInteger("targetRingSymbol", targetRingSymbol.getId());
		compound.setInteger("spinDirection", spinDirection.id);

		NBTTagList linkedBeamersTagList = new NBTTagList();
		for (BlockPos vect : linkedBeamers)
			linkedBeamersTagList.appendTag(new NBTTagLong(vect.toLong()));
		compound.setTag("linkedBeamers", linkedBeamersTagList);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		itemStackHandler.deserializeNBT(compound.getCompoundTag("itemHandler"));

		if (compound.getBoolean("hasUpgrade")) {
			itemStackHandler.setStackInSlot(0, new ItemStack(AunisItems.CRYSTAL_GLYPH_STARGATE));
		}

		isFinalActive = compound.getBoolean("isFinalActive");

		isSpinning = compound.getBoolean("isSpinning");
		spinStartTime = compound.getLong("spinStartTime");
		currentRingSymbol = getSymbolType().valueOfSymbol(compound.getInteger("currentRingSymbol"));
		targetRingSymbol = getSymbolType().valueOfSymbol(compound.getInteger("targetRingSymbol"));
		spinDirection = EnumSpinDirection.valueOf(compound.getInteger("spinDirection"));

		for (NBTBase tag : compound.getTagList("linkedBeamers", NBT.TAG_LONG))
			linkedBeamers.add(BlockPos.fromLong(((NBTTagLong) tag).getLong()));

		super.readFromNBT(compound);
	}


	// ------------------------------------------------------------------------
	// Rendering

	protected void updateChevronLight(int lightUp, boolean isFinalActive) {
//		Aunis.info("Updating chevron light to: " + lightUp);

		if (isFinalActive)
			lightUp--;

		for (int i=0; i<9; i++) {
			BlockPos chevPos = getMergeHelper().getChevronBlocks().get(i).rotate(FacingToRotation.get(facing)).add(pos);

			if (getMergeHelper().matchMember(BaseUtils.getWorldBlockState(worldObj, chevPos))) {
				StargateClassicMemberTile memberTile = (StargateClassicMemberTile) worldObj.getTileEntity(chevPos);
				memberTile.setLitUp(i==8 ? isFinalActive : lightUp > i);
			}
		}
	}

	@Override
	protected StargateClassicRendererStateBuilder getRendererStateServer() {
		return new StargateClassicRendererStateBuilder(super.getRendererStateServer())
				.setSymbolType(getSymbolType())
				.setActiveChevrons(dialedAddress.size())
				.setFinalActive(isFinalActive)
				.setCurrentRingSymbol(currentRingSymbol)
				.setSpinDirection(spinDirection)
				.setSpinning(isSpinning)
				.setTargetRingSymbol(targetRingSymbol)
				.setSpinStartTime(spinStartTime)
				.setBiomeOverride(determineBiomeOverride());
	}

	@Override
	public StargateClassicRendererState getRendererStateClient() {
		return (StargateClassicRendererState) super.getRendererStateClient();
	}

	public static final AunisAxisAlignedBB RENDER_BOX = new AunisAxisAlignedBB(-5.5, 0, -0.5, 5.5, 10.5, 0.5);

	@Override
	protected AunisAxisAlignedBB getRenderBoundingBoxRaw() {
		return RENDER_BOX;
	}

	// -----------------------------------------------------------------
	// States

	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case GUI_STATE:
				//return new StargateContainerGuiState(gateAddressMap);
                break;

			case GUI_UPDATE:
				//return new StargateContainerGuiUpdate(energyStorage.getEnergyStoredInternally(), energyTransferedLastTick, energySecondsToClose);
                break;

			default:
				return super.getState(stateType);
		}
	}

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case GUI_STATE:
				//return new StargateContainerGuiState();
                break;

			case GUI_UPDATE:
				//return new StargateContainerGuiUpdate();
                break;

			case SPIN_STATE:
				return new StargateSpinState();

			case BIOME_OVERRIDE_STATE:
				return new StargateBiomeOverrideState();

			default:
				return super.createState(stateType);
		}
	}

	@Override
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_UPDATE:
				StargateRendererActionState gateActionState = (StargateRendererActionState) state;

				switch (gateActionState.action) {
					case CHEVRON_ACTIVATE:
						if (gateActionState.modifyFinal)
							getRendererStateClient().chevronTextureList.activateFinalChevron(worldObj.getTotalWorldTime());
						else
							getRendererStateClient().chevronTextureList.activateNextChevron(worldObj.getTotalWorldTime());

						break;

					case CLEAR_CHEVRONS:
						getRendererStateClient().chevronTextureList.clearChevrons(worldObj.getTotalWorldTime());
						break;

					case LIGHT_UP_CHEVRONS:
						getRendererStateClient().chevronTextureList.lightUpChevrons(worldObj.getTotalWorldTime(), gateActionState.chevronCount);
						break;

					case CHEVRON_ACTIVATE_BOTH:
						getRendererStateClient().chevronTextureList.activateNextChevron(worldObj.getTotalWorldTime());
						getRendererStateClient().chevronTextureList.activateFinalChevron(worldObj.getTotalWorldTime());
						break;

					case CHEVRON_DIM:
						getRendererStateClient().chevronTextureList.deactivateFinalChevron(worldObj.getTotalWorldTime());
						break;

					default:
						break;
				}

				break;

			case GUI_STATE:
				StargateContainerGuiState guiState = (StargateContainerGuiState) state;
				gateAddressMap = guiState.gateAdddressMap;

				break;

			case GUI_UPDATE:
				StargateContainerGuiUpdate guiUpdate = (StargateContainerGuiUpdate) state;
				energyStorage.setEnergyStoredInternally(guiUpdate.energyStored);
				energyTransferedLastTick = guiUpdate.transferedLastTick;
				energySecondsToClose = guiUpdate.secondsToClose;

				break;

			case SPIN_STATE:
				StargateSpinState spinState = (StargateSpinState) state;
				if (spinState.setOnly) {
					getRendererStateClient().spinHelper.isSpinning = false;
					getRendererStateClient().spinHelper.currentSymbol = spinState.targetSymbol;
				}

				else
					getRendererStateClient().spinHelper.initRotation(worldObj.getTotalWorldTime(), spinState.targetSymbol, spinState.direction);

				break;

			case BIOME_OVERRIDE_STATE:
				StargateBiomeOverrideState overrideState = (StargateBiomeOverrideState) state;

				if (rendererStateClient != null) {
					getRendererStateClient().biomeOverride = overrideState.biomeOverride;
				}

				break;

			default:
				break;
		}

		super.setState(stateType, state);
	}


	// -----------------------------------------------------------------
	// Scheduled tasks

	@Override
	public void executeTask(EnumScheduledTask scheduledTask, NBTTagCompound customData) {
		switch (scheduledTask) {
			case STARGATE_SPIN_FINISHED:
				isSpinning = false;
				currentRingSymbol = targetRingSymbol;

				playPositionedSound(StargateSoundPositionedEnum.GATE_RING_ROLL, false);
				playSoundEvent(StargateSoundEventEnum.CHEVRON_SHUT);

				markDirty();
				break;

			case STARGATE_GIVE_PAGE:
				SymbolTypeEnum symbolType = SymbolTypeEnum.valueOf(pageSlotId - 7);
				ItemStack stack = itemStackHandler.getStackInSlot(pageSlotId);

//				if (stack.getItem() == AunisItems.UNIVERSE_DIALER) {
//					NBTTagList saved = stack.getTagCompound().getTagList("saved", NBT.TAG_COMPOUND);
//					NBTTagCompound compound = gateAddressMap.get(symbolType).serializeNBT();
//					compound.setBoolean("hasUpgrade", hasUpgrade(StargateUpgradeEnum.CHEVRON_UPGRADE));
//					saved.appendTag(compound);
//				}
//
//				else {
//					Aunis.LOG.debug("Giving Notebook page of address " + symbolType);
//
//					NBTTagCompound compound = PageNotebookItem.getCompoundFromAddress(
//							gateAddressMap.get(symbolType),
//							hasUpgrade(StargateUpgradeEnum.CHEVRON_UPGRADE),
//							PageNotebookItem.getRegistryPathFromWorld(worldObj, pos));
//
//					stack = new ItemStack(AunisItems.PAGE_NOTEBOOK_ITEM, 1, 1);
//					stack.setTagCompound(compound);
//					itemStackHandler.setStackInSlot(pageSlotId, stack);
//				}

				break;

			default:
				super.executeTask(scheduledTask, customData);
		}
	}


	// ------------------------------------------------------------------------
	// Ring spinning

	protected boolean isSpinning;
	protected long spinStartTime;
	protected SymbolInterface currentRingSymbol = getSymbolType().getTopSymbol();
	protected SymbolInterface targetRingSymbol = getSymbolType().getTopSymbol();
	protected EnumSpinDirection spinDirection = EnumSpinDirection.COUNTER_CLOCKWISE;
	protected Object ringSpinContext;

	public void addSymbolToAddressManual(SymbolInterface targetSymbol, @Nullable Object context) {
		targetRingSymbol = targetSymbol;

		boolean moveOnly = targetRingSymbol == currentRingSymbol;

		if (moveOnly) {
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_SPIN_FINISHED, 0));
		}

		else {
			float distance = spinDirection.getDistance(currentRingSymbol, targetRingSymbol);

			if (distance > 180) {
				spinDirection = spinDirection.opposite();
				distance = spinDirection.getDistance(currentRingSymbol, targetRingSymbol);
			}

			int duration = StargateClassicSpinHelper.getAnimationDuration(distance);

			Aunis.LOG.debug("addSymbolToAddressManual: "
					+ "current:" + currentRingSymbol + ", "
					+ "target:" + targetSymbol + ", "
					+ "direction:" + spinDirection + ", "
					+ "distance:" + distance + ", "
					+ "duration:" + duration + ", "
					+ "moveOnly:" + moveOnly);

			AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, StateTypeEnum.SPIN_STATE, new StargateSpinState(targetRingSymbol, spinDirection, false)), targetPoint);
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_SPIN_FINISHED, duration-5));
			playPositionedSound(StargateSoundPositionedEnum.GATE_RING_ROLL, true);

			isSpinning = true;
			spinStartTime = worldObj.getTotalWorldTime();

			ringSpinContext = context;
			if (context != null)
				//sendSignalf(context, "stargate_spin_start", new Object[] { dialedAddress.size(), stargateWillLock(targetRingSymbol), targetSymbol.getEnglishName() });
		}

		markDirty();
	}

	// -----------------------------------------------------------------------------
	// Page conversion

	private short pageProgress = 0;
	private int pageSlotId;
	private boolean doPageProgress;
	private ScheduledTask givePageTask;
	private boolean lockPage;

	public short getPageProgress() {
		return pageProgress;
	}

	public void setPageProgress(int pageProgress) {
		this.pageProgress = (short) pageProgress;
	}

	// -----------------------------------------------------------------------------
	// Item handler

	public static final int BIOME_OVERRIDE_SLOT = 10;

	private final AunisItemStackHandler itemStackHandler = new AunisItemStackHandler(11) {

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			Item item = stack.getItem();
			boolean isItemCapacitor = (item == Item.getItemFromBlock(AunisBlocks.CAPACITOR_BLOCK));

			switch (slot) {
				case 0:
				case 1:
				case 2:
				case 3:
					return StargateUpgradeEnum.contains(item) && !hasUpgrade(item);

				case 4:
					return isItemCapacitor && getSupportedCapacitors() >= 1;
				case 5:
					return isItemCapacitor && getSupportedCapacitors() >= 2;
				case 6:
					return isItemCapacitor && getSupportedCapacitors() >= 3;

				case 7:
				case 8:
					//return item == AunisItems.PAGE_NOTEBOOK_ITEM;
                    return true;

				case 9:
					//return item == AunisItems.PAGE_NOTEBOOK_ITEM || item == AunisItems.UNIVERSE_DIALER;
                    return true;

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
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);

			switch (slot) {
				case 4:
				case 5:
				case 6:
					updatePowerTier();
					break;

				case BIOME_OVERRIDE_SLOT:
					sendState(StateTypeEnum.BIOME_OVERRIDE_STATE, new StargateBiomeOverrideState(determineBiomeOverride()));
					break;

				default:
					break;
			}

			markDirty();
		}
	};

	public abstract int getSupportedCapacitors();

	public static enum StargateUpgradeEnum implements EnumKeyInterface<Item> {
		MILKYWAY_GLYPHS(AunisItems.CRYSTAL_GLYPH_MILKYWAY),
		PEGASUS_GLYPHS(AunisItems.CRYSTAL_GLYPH_PEGASUS),
		UNIVERSE_GLYPHS(AunisItems.CRYSTAL_GLYPH_UNIVERSE),
		CHEVRON_UPGRADE(AunisItems.CRYSTAL_GLYPH_STARGATE);

		public Item item;

		private StargateUpgradeEnum(Item item) {
			this.item = item;
		}

		@Override
		public Item getKey() {
			return item;
		}

		private static final EnumKeyMap<Item, StargateUpgradeEnum> idMap = new EnumKeyMap<Item, StargateUpgradeEnum>(values());

		public static StargateUpgradeEnum valueOf(Item item) {
			return idMap.valueOf(item);
		}

		public static boolean contains(Item item) {
			return idMap.contains(item);
		}
	}

	@Override
	public Iterator<Integer> getUpgradeSlotsIterator() {
		return IntStream.range(0, 7).iterator();
	}

	// -----------------------------------------------------------------------------
	// Power system

	private final StargateClassicEnergyStorage energyStorage = new StargateClassicEnergyStorage() {

		@Override
		protected void onEnergyChanged() {
			markDirty();
		}
	};

	@Override
	protected StargateAbstractEnergyStorage getEnergyStorage() {
		return energyStorage;
	}

	private int currentPowerTier = 1;

	public int getPowerTier() {
		return currentPowerTier;
	}

	private void updatePowerTier() {
		int powerTier = 1;

		for (int i=4; i<7; i++) {
			if (!itemStackHandler.getStackInSlot(i).isEmpty()) {
				powerTier++;
			}
		}

		if (powerTier != currentPowerTier) {
			currentPowerTier = powerTier;

			energyStorage.clearStorages();

			for (int i=4; i<7; i++) {
				ItemStack stack = itemStackHandler.getStackInSlot(i);

				if (!BaseUtils.isEmpty(stack)) {
					//energyStorage.addStorage(stack.getCapability(CapabilityEnergy.ENERGY, null));
				}
			}

			Aunis.LOG.debug("Updated to power tier: " + powerTier);
		}
	}


	// -----------------------------------------------------------------
	// Beamers

	private final List<BlockPos> linkedBeamers = new ArrayList<>();

	public void addLinkedBeamer(BlockPos pos) {
		if (stargateState.engaged()) {
			//((BeamerTile) worldObj.getTileEntity(pos)).gateEngaged(targetGatePos);
		}

		linkedBeamers.add(pos.toImmutable());
		markDirty();
	}

	public void removeLinkedBeamer(BlockPos pos) {
		linkedBeamers.remove(pos);
		markDirty();
	}

	private void updateBeamers() {
		if (stargateState.engaged()) {
			for (BlockPos beamerPos : linkedBeamers) {
				//((BeamerTile) worldObj.getTileEntity(beamerPos)).gateEngaged(targetGatePos);
			}
		}
	}
}
