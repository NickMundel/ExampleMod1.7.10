package com.mrjake.aunis.tileentity.stargate;

import com.mrjake.aunis.block.AunisBlocks;
import com.mrjake.aunis.config.AunisConfig;
import com.mrjake.aunis.config.StargateSizeEnum;
import com.mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import com.mrjake.aunis.renderer.stargate.StargateAbstractRendererState;
import com.mrjake.aunis.renderer.stargate.StargateMilkyWayRendererState;
import com.mrjake.aunis.sound.SoundEventEnum;
import com.mrjake.aunis.sound.SoundPositionedEnum;
import com.mrjake.aunis.sound.StargateSoundEventEnum;
import com.mrjake.aunis.sound.StargateSoundPositionedEnum;
import com.mrjake.aunis.stargate.EnumScheduledTask;
import com.mrjake.aunis.stargate.EnumStargateState;
import com.mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import com.mrjake.aunis.stargate.merging.StargateMilkyWayMergeHelper;
import com.mrjake.aunis.stargate.network.*;
import com.mrjake.aunis.state.StargateRendererActionState;
import com.mrjake.aunis.state.State;
import com.mrjake.aunis.state.StateTypeEnum;
import com.mrjake.aunis.tileentity.DHDTile;
import com.mrjake.aunis.tileentity.util.ScheduledTask;
import com.mrjake.aunis.util.AunisAxisAlignedBB;
import com.mrjake.aunis.util.ILinkable;
import com.mrjake.aunis.util.LinkingHelper;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.renderer.stargate.StargateMilkyWayRendererState.StargateMilkyWayRendererStateBuilder;
import com.mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import com.mrjake.aunis.config.StargateDimensionConfig;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class StargateMilkyWayBaseTile extends StargateClassicBaseTile implements ILinkable {

	// ------------------------------------------------------------------------
	// Stargate state

	@Override
	protected void disconnectGate() {
		super.disconnectGate();

		if (isLinkedAndDHDOperational())
			getLinkedDHD(worldObj).clearSymbols();
	}

	@Override
	protected void failGate() {
		super.failGate();

		if (isLinkedAndDHDOperational())
			getLinkedDHD(worldObj).clearSymbols();
	}

	@Override
	protected void addFailedTaskAndPlaySound() {
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL, stargateState.dialingComputer() ? 83 : 53));
		playSoundEvent(StargateSoundEventEnum.DIAL_FAILED);
	}


	// ------------------------------------------------------------------------
	// Stargate connection

	@Override
	public void openGate(StargatePos targetGatePos, boolean isInitiating) {
		super.openGate(targetGatePos, isInitiating);

		if (isLinkedAndDHDOperational()) {
			getLinkedDHD(worldObj).activateSymbol(SymbolMilkyWayEnum.BRB);
		}
	}

	// ------------------------------------------------------------------------
	// Stargate Network

	@Override
	public SymbolTypeEnum getSymbolType() {
		return SymbolTypeEnum.MILKYWAY;
	}

	@Override
	protected AunisAxisAlignedBB getHorizonTeleportBox(boolean server) {
		return getStargateSizeConfig(server).teleportBox;
	}

	public void addSymbolToAddressDHD(SymbolMilkyWayEnum symbol) {
		addSymbolToAddress(symbol);
		stargateState = EnumStargateState.DIALING;

		if (stargateWillLock(symbol)) {
			isFinalActive = true;
		}

		//sendSignal(null, "stargate_dhd_chevron_engaged", new Object[] { dialedAddress.size(), isFinalActive, symbol.englishName });
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_ACTIVATE_CHEVRON, 10));

		markDirty();
	}

	@Override
	protected int getMaxChevrons() {
		return isLinkedAndDHDOperational() && stargateState != EnumStargateState.DIALING_COMPUTER && !getLinkedDHD(worldObj).hasUpgrade(DHDTile.DHDUpgradeEnum.CHEVRON_UPGRADE) ? 7 : 9;
	}

	@Override
	public void addSymbolToAddress(SymbolInterface symbol) {
		if (symbol.origin() && dialedAddress.size() >= 6 && dialedAddress.equals(StargateNetwork.EARTH_ADDRESS) && !network.isStargateInNetwork(StargateNetwork.EARTH_ADDRESS)) {
			if (StargateDimensionConfig.netherOverworld8thSymbol()) {
				if (dialedAddress.size() == 7 && dialedAddress.getLast() == SymbolMilkyWayEnum.SERPENSCAPUT) {
					dialedAddress.clear();
					dialedAddress.addAll(network.getLastActivatedOrlins().subList(0, 7));
				}
			}

			else {
				dialedAddress.clear();
				dialedAddress.addAll(network.getLastActivatedOrlins().subList(0, 6));
			}
		}

		super.addSymbolToAddress(symbol);

		if (isLinkedAndDHDOperational()) {
			getLinkedDHD(worldObj).activateSymbol((SymbolMilkyWayEnum) symbol);
		}
	}

	@Override
	public void addSymbolToAddressManual(SymbolInterface targetSymbol, Object context) {
		stargateState = EnumStargateState.DIALING_COMPUTER;

		super.addSymbolToAddressManual(targetSymbol, context);
	}

	public void incomingWormhole(int dialedAddressSize) {
		super.incomingWormhole(dialedAddressSize);

		if (isLinkedAndDHDOperational()) {
			getLinkedDHD(worldObj).clearSymbols();
		}
	}


	// ------------------------------------------------------------------------
	// Merging

	@Override
	public void onGateBroken() {
		super.onGateBroken();

		if (isLinked()) {
			getLinkedDHD(worldObj).clearSymbols();
			getLinkedDHD(worldObj).setLinkedGate(null);
			setLinkedDHD(null);
		}
	}

	@Override
	protected void onGateMerged() {
		super.onGateMerged();
		BlockPos closestDhd = LinkingHelper.findClosestUnlinked(worldObj, pos, LinkingHelper.getDhdRange(), AunisBlocks.DHD_BLOCK);

		if (closestDhd != null) {
			DHDTile dhdTile = (DHDTile) worldObj.getTileEntity(closestDhd.getX(), closestDhd.getY(), closestDhd.getZ());

			dhdTile.setLinkedGate(pos);
			setLinkedDHD(closestDhd);
			markDirty();
		}
	}

	@Override
	public StargateAbstractMergeHelper getMergeHelper() {
		return StargateMilkyWayMergeHelper.INSTANCE;
	}


	// ------------------------------------------------------------------------
	// Linking

	private BlockPos linkedDHD = null;

	@Nullable
	public DHDTile getLinkedDHD(World worldObj) {
		if (linkedDHD == null)
			return null;

		return (DHDTile) worldObj.getTileEntity(linkedDHD.getX(), linkedDHD.getY(), linkedDHD.getZ());
	}

	public boolean isLinked() {
		return linkedDHD != null;
	}

	public boolean isLinkedAndDHDOperational() {
		if (!isLinked())
			return false;

		DHDTile dhdTile = getLinkedDHD(worldObj);
		if (!dhdTile.hasControlCrystal())
			return false;

		return true;
	}

	public void setLinkedDHD(BlockPos dhdPos) {
		this.linkedDHD = dhdPos;

		markDirty();
	}

	@Override
	public boolean canLinkTo() {
		return isMerged() && !isLinked();
	}

	// ------------------------------------------------------------------------
	// NBT

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		if (isLinked())
			compound.setLong("linkedDHD", linkedDHD.toLong());

		compound.setInteger("stargateSize", stargateSize.id);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("linkedDHD"))
			this.linkedDHD = BlockPos.fromLong( compound.getLong("linkedDHD") );

		if (compound.hasKey("patternVersion"))
			stargateSize = StargateSizeEnum.SMALL;
		else {
			if (compound.hasKey("stargateSize"))
				stargateSize = StargateSizeEnum.fromId(compound.getInteger("stargateSize"));
			else
				stargateSize = StargateSizeEnum.LARGE;
		}

		super.readFromNBT(compound);
	}

	@Override
	public boolean prepare(ICommandSender sender, ICommand command) {
		setLinkedDHD(null);

		return super.prepare(sender, command);
	}


	// ------------------------------------------------------------------------
	// Sounds

	@Override
	protected SoundPositionedEnum getPositionedSound(StargateSoundPositionedEnum soundEnum) {
		switch (soundEnum) {
			case GATE_RING_ROLL: return SoundPositionedEnum.MILKYWAY_RING_ROLL;
		}

		return null;
	}

	@Override
	protected SoundEventEnum getSoundEvent(StargateSoundEventEnum soundEnum) {
		switch (soundEnum) {
			case OPEN: return SoundEventEnum.GATE_MILKYWAY_OPEN;
			case CLOSE: return SoundEventEnum.GATE_MILKYWAY_CLOSE;
			case DIAL_FAILED: return stargateState.dialingComputer() ? SoundEventEnum.GATE_MILKYWAY_DIAL_FAILED_COMPUTER : SoundEventEnum.GATE_MILKYWAY_DIAL_FAILED;
			case INCOMING: return SoundEventEnum.GATE_MILKYWAY_INCOMING;
			case CHEVRON_OPEN: return SoundEventEnum.GATE_MILKYWAY_CHEVRON_OPEN;
			case CHEVRON_SHUT: return SoundEventEnum.GATE_MILKYWAY_CHEVRON_SHUT;
		}

		return null;
	}


	// ------------------------------------------------------------------------
	// Ticking and loading

	@Override
	public BlockPos getGateCenterPos() {
		return pos.offset(EnumFacing.UP, 4);
	}

	private boolean firstTick = true;

	@Override
	public void tick() {
		super.tick();

		if (!worldObj.isRemote) {
			if (firstTick) {
				firstTick = false;

				// Doing this in onLoad causes ConcurrentModificationException
				if (stargateSize != AunisConfig.stargateSize && isMerged()) {
					StargateMilkyWayMergeHelper.INSTANCE.convertToPattern(worldObj, pos, facing, stargateSize, AunisConfig.stargateSize);
					updateMergeState(StargateMilkyWayMergeHelper.INSTANCE.checkBlocks(worldObj, pos, facing), facing);

					stargateSize = AunisConfig.stargateSize;
					markDirty();
				}
			}
		}
	}

	public static final EnumSet<BiomeOverlayEnum> SUPPORTED_OVERLAYS = EnumSet.of(
			BiomeOverlayEnum.NORMAL,
			BiomeOverlayEnum.FROST,
			BiomeOverlayEnum.MOSSY,
			BiomeOverlayEnum.AGED,
			BiomeOverlayEnum.SOOTY);

	@Override
	public EnumSet<BiomeOverlayEnum> getSupportedOverlays() {
		return SUPPORTED_OVERLAYS;
	}

	// ------------------------------------------------------------------------
	// Killing and block vaporizing

	@Override
	protected AunisAxisAlignedBB getHorizonKillingBox(boolean server) {
		return getStargateSizeConfig(server).killingBox;
	}

	@Override
	protected int getHorizonSegmentCount(boolean server) {
		return getStargateSizeConfig(server).horizonSegmentCount;
	}

	@Override
	protected  List<AunisAxisAlignedBB> getGateVaporizingBoxes(boolean server) {
		return getStargateSizeConfig(server).gateVaporizingBoxes;
	}


	// ------------------------------------------------------------------------
	// Rendering

	private StargateSizeEnum stargateSize = AunisConfig.stargateSize;

	/**
	 * Returns stargate state either from config or from client's state.
	 * THIS IS NOT A GETTER OF stargateSize.
	 *
	 * @param server Is the code running on server
	 * @return Stargate's size
	 */
	private StargateSizeEnum getStargateSizeConfig(boolean server) {
		return server ? AunisConfig.stargateSize : getRendererStateClient().stargateSize;
	}

	@Override
	protected StargateMilkyWayRendererStateBuilder getRendererStateServer() {
		return new StargateMilkyWayRendererStateBuilder(super.getRendererStateServer())
				.setStargateSize(stargateSize);
	}

	@Override
	protected StargateAbstractRendererState createRendererStateClient() {
		return new StargateMilkyWayRendererState();
	}

	@Override
	public StargateMilkyWayRendererState getRendererStateClient() {
		return (StargateMilkyWayRendererState) super.getRendererStateClient();
	}


	// -----------------------------------------------------------------
	// States

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {


			default:
				return super.createState(stateType);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_UPDATE:
				StargateRendererActionState gateActionState = (StargateRendererActionState) state;

				switch (gateActionState.action) {
					case CHEVRON_OPEN:
						getRendererStateClient().openChevron(worldObj.getTotalWorldTime());
						break;

					case CHEVRON_CLOSE:
						getRendererStateClient().closeChevron(worldObj.getTotalWorldTime());
						break;

					default:
						break;
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
			case STARGATE_ACTIVATE_CHEVRON:
				stargateState = EnumStargateState.IDLE;
				markDirty();

				playSoundEvent(StargateSoundEventEnum.CHEVRON_OPEN);
				sendRenderingUpdate(EnumGateAction.CHEVRON_ACTIVATE, -1, isFinalActive);
				updateChevronLight(dialedAddress.size(), isFinalActive);
	//			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, new StargateRendererActionState(EnumGateAction.CHEVRON_ACTIVATE, -1, customData.getBoolean("final"))), targetPoint);
				break;

			case STARGATE_SPIN_FINISHED:
				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_OPEN, 11));

				break;

			case STARGATE_CHEVRON_OPEN:
				playSoundEvent(StargateSoundEventEnum.CHEVRON_OPEN);
				sendRenderingUpdate(EnumGateAction.CHEVRON_OPEN, 0, false);

				if (canAddSymbol(targetRingSymbol)) {
					addSymbolToAddress(targetRingSymbol);

					if (stargateWillLock(targetRingSymbol)) {
						if (checkAddressAndEnergy(dialedAddress).ok()) {
							addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_OPEN_SECOND, 8));
						}

						else
							addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_FAIL, 60));
					}

					else
						addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_OPEN_SECOND, 8));
				}

				else
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_FAIL, 60));

				break;

			case STARGATE_CHEVRON_OPEN_SECOND:
				playSoundEvent(StargateSoundEventEnum.CHEVRON_OPEN);
				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_LIGHT_UP, 4));

				break;

			case STARGATE_CHEVRON_LIGHT_UP:
				if (stargateWillLock(targetRingSymbol))
					sendRenderingUpdate(EnumGateAction.CHEVRON_ACTIVATE, 0, true);
				else
					sendRenderingUpdate(EnumGateAction.CHEVRON_ACTIVATE_BOTH, 0, false);

				updateChevronLight(dialedAddress.size(), isFinalActive);

				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_CLOSE, 14));

				break;

			case STARGATE_CHEVRON_CLOSE:
				playSoundEvent(StargateSoundEventEnum.CHEVRON_SHUT);
				sendRenderingUpdate(EnumGateAction.CHEVRON_CLOSE, 0, false);

				if (stargateWillLock(targetRingSymbol)) {
					stargateState = EnumStargateState.IDLE;
					//sendSignal(ringSpinContext, "stargate_spin_chevron_engaged", new Object[] { dialedAddress.size(), true, targetRingSymbol.getEnglishName() });
				} else
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_DIM, 6));

				break;

			case STARGATE_CHEVRON_DIM:
				sendRenderingUpdate(EnumGateAction.CHEVRON_DIM, 0, false);
				stargateState = EnumStargateState.IDLE;

				//sendSignal(ringSpinContext, "stargate_spin_chevron_engaged", new Object[] { dialedAddress.size(), false, targetRingSymbol.getEnglishName() });

				break;

			case STARGATE_CHEVRON_FAIL:
				sendRenderingUpdate(EnumGateAction.CHEVRON_CLOSE, 0, false);
				dialingFailed(checkAddressAndEnergy(dialedAddress));

				break;

			default:
				break;
		}

		super.executeTask(scheduledTask, customData);
	}

	@Override
	public int getSupportedCapacitors() {
		return 3;
	}
}
