package com.mrjake.aunis.tileentity.stargate;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.api.event.*;
import com.mrjake.aunis.block.AunisBlocks;
import com.mrjake.aunis.block.DHDBlock;
import com.mrjake.aunis.chunkloader.ChunkManager;
import com.mrjake.aunis.config.AunisConfig;
import com.mrjake.aunis.config.StargateDimensionConfig;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.packet.StateUpdatePacketToClient;
import com.mrjake.aunis.packet.StateUpdateRequestToServer;
import com.mrjake.aunis.particle.ParticleWhiteSmoke;
import com.mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import com.mrjake.aunis.renderer.stargate.StargateAbstractRendererState;
import com.mrjake.aunis.sound.*;
import com.mrjake.aunis.stargate.*;
import com.mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import com.mrjake.aunis.stargate.network.*;
import com.mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import com.mrjake.aunis.stargate.power.StargateEnergyRequired;
import com.mrjake.aunis.stargate.teleportation.EventHorizon;
import com.mrjake.aunis.state.*;
import com.mrjake.aunis.tileentity.util.PreparableInterface;
import com.mrjake.aunis.tileentity.util.ScheduledTask;
import com.mrjake.aunis.tileentity.util.ScheduledTaskExecutorInterface;
import com.mrjake.aunis.util.*;
import com.mrjake.aunis.util.minecraft.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import com.mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import java.util.*;

public abstract class StargateAbstractBaseTile extends BaseTileEntity implements StateProviderInterface, ITickable, ScheduledTaskExecutorInterface, PreparableInterface {

	// ------------------------------------------------------------------------
	// Stargate state

	protected EnumStargateState stargateState = EnumStargateState.IDLE;

	public final EnumStargateState getStargateState() {
		return stargateState;
	}

	private boolean isInitiating;

	protected void engageGate() {
		stargateState = isInitiating ? EnumStargateState.ENGAGED_INITIATING : EnumStargateState.ENGAGED;
		eventHorizon.reset();

		AunisSoundHelper.playPositionedSound(worldObj, getGateCenterPos(), SoundPositionedEnum.WORMHOLE_LOOP, true);

		new StargateOpenedEvent(this, targetGatePos.getTileEntity(), isInitiating).post();

		////sendSignal(null, "stargate_wormhole_stabilized", new Object[] { isInitiating });

		markDirty();
	}

	protected void disconnectGate() {
		stargateState = EnumStargateState.IDLE;
		getAutoCloseManager().reset();

        //TODO: Fix orlin
		//if (!(this instanceof StargateOrlinBaseTile))
		//	dialedAddress.clear();

		new StargateClosedEvent(this).post();

		ChunkManager.unforceChunk(worldObj, new ChunkPos(pos));
		////sendSignal(null, "stargate_wormhole_closed_fully", new Object[] { isInitiating });

		markDirty();
	}

	protected void failGate() {
		stargateState = EnumStargateState.IDLE;

		//if (!(this instanceof StargateOrlinBaseTile))
		//	dialedAddress.clear();

		markDirty();
	}

	public void onBlockBroken() {
		for (StargateAddress address : gateAddressMap.values())
			network.removeStargate(address);
	}

	protected void onGateBroken() {
		worldObj.setBlockToAir(getGateCenterPos().getX(), getGateCenterPos().getY(), getGateCenterPos().getZ());

		if (stargateState.initiating()) {
			attemptClose(StargateClosedReasonEnum.CONNECTION_LOST);
		}

		else if (stargateState.engaged()) {
			targetGatePos.getTileEntity().attemptClose(StargateClosedReasonEnum.CONNECTION_LOST);
		}

		dialedAddress.clear();
		targetGatePos = null;
		scheduledTasks.clear();
		stargateState = EnumStargateState.IDLE;
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, 0, false);

		ChunkManager.unforceChunk(worldObj, new ChunkPos(pos));
		AunisSoundHelper.playPositionedSound(worldObj, getGateCenterPos(), SoundPositionedEnum.WORMHOLE_LOOP, false);

		markDirty();
	}

	protected void onGateMerged() {}

	public boolean canAcceptConnectionFrom(StargatePos targetGatePos) {
		return isMerged && stargateState.idle();
	}

	protected void sendRenderingUpdate(StargateRendererActionState.EnumGateAction gateAction, int chevronCount, boolean modifyFinal) {
		sendState(StateTypeEnum.RENDERER_UPDATE, new StargateRendererActionState(gateAction, chevronCount, modifyFinal));
	}

	// TODO Convert to using sendState
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

	/**
	 * Instance of the {@link EventHorizon} for teleporting entities.
	 */
	protected EventHorizon eventHorizon;

	public AunisAxisAlignedBB getEventHorizonLocalBox() {
		return eventHorizon.getLocalBox();
	}

	/**
	 * Get the bounding box of the horizon.
	 * @param server Calling side.
	 * @return Horizon bounding box.
	 */
	protected abstract AunisAxisAlignedBB getHorizonTeleportBox(boolean server);

	private AutoCloseManager autoCloseManager;

	private AutoCloseManager getAutoCloseManager() {
		if (autoCloseManager == null)
			autoCloseManager = new AutoCloseManager(this);

		return autoCloseManager;
	}

	public void setMotionOfPassingEntity(int entityId, Vector2f motionVector) {
		eventHorizon.setMotion(entityId, motionVector);
	}

	/**
	 * Called to immediately teleport the entity (after entity has received motion from the client)
	 */
	public void teleportEntity(int entityId) {
		eventHorizon.teleportEntity(entityId);
	}

	/**
	 * Called when entity tries to come through the gate on the back side
	 */
	public void removeEntity(int entityId) {
		eventHorizon.removeEntity(entityId);
	}


	// ------------------------------------------------------------------------
	// Stargate connection

	/**
	 * Wrapper for {@link this#attemptOpenDialed()} which calls {@link this#dialingFailed(StargateOpenResult)}
	 * when the checks fail.
	 *
	 * @return {@link StargateOpenResult} returned by {@link this#attemptOpenDialed()}.
	 */
	public StargateOpenResult attemptOpenAndFail() {
		ResultTargetValid resultTarget = attemptOpenDialed();

		if (!resultTarget.result.ok()) {
			dialingFailed(resultTarget.result);

			// TODO Find a test case for resultTarget.targetVaild
//			if (resultTarget.targetVaild) {
//				// We can call dialing failed on the target gate
//				network.getStargate(dialedAddress).getTileEntity().dialingFailed(StargateOpenResult.CALLER_HUNG_UP);
//			}
		}

		return resultTarget.result;
	}

	/**
	 * Attempts to open the connection to gate pointed by {@link StargateAbstractBaseTile#dialedAddress}.
	 * This performs all the checks.
	 */
	protected ResultTargetValid attemptOpenDialed() {

		boolean targetValid = false;
		StargateOpenResult result = checkAddressAndEnergy(dialedAddress);

		if (result.ok()) {
			targetValid = true;

			StargatePos targetGatePos = network.getStargate(dialedAddress);
			StargateAbstractBaseTile targetTile = targetGatePos.getTileEntity();

			if(new StargateOpeningEvent(this, targetGatePos.getTileEntity(), isInitiating).post()) {
				// Gate open cancelled by event
				return new ResultTargetValid(StargateOpenResult.ABORTED_BY_EVENT, targetValid);
			}

			if (!targetTile.canAcceptConnectionFrom(gatePosMap.get(getSymbolType())))
				return new ResultTargetValid(StargateOpenResult.ADDRESS_MALFORMED, targetValid);

			openGate(targetGatePos, true);
			targetTile.openGate(gatePosMap.get(targetGatePos.symbolType), false);
			targetTile.dialedAddress.clear();
			targetTile.dialedAddress.addAll(gateAddressMap.get(targetGatePos.symbolType).subList(0, dialedAddress.size()-1));
			targetTile.dialedAddress.addOrigin();
		}

		return new ResultTargetValid(result, targetValid);
	}

	/**
	 * Checks if the address can be dialed.
	 * @param address Address to be checked.
	 * @return {@code True} if the address parameter is valid and the dialed gate can be reached, {@code false} otherwise.
	 */
	protected StargateOpenResult checkAddress(StargateAddressDynamic address) {
		if (!address.validate())
			return StargateOpenResult.ADDRESS_MALFORMED;

		if (!canDialAddress(address))
			return StargateOpenResult.ADDRESS_MALFORMED;

		StargateAbstractBaseTile targetTile = network.getStargate(address).getTileEntity();

		if (!targetTile.canAcceptConnectionFrom(gatePosMap.get(getSymbolType())))
			return StargateOpenResult.ADDRESS_MALFORMED;

		return StargateOpenResult.OK;
	}

	/**
	 * Checks if the address can be dialed and if the gate has power to do so.
	 * @param address Address to be checked.
	 * @return {@code True} if the address parameter is valid and the dialed gate can be reached, {@code false} otherwise.
	 */
	protected StargateOpenResult checkAddressAndEnergy(StargateAddressDynamic address) {
		StargateOpenResult result = checkAddress(address);

		if (!result.ok())
			return result;

		StargatePos targetGatePos = network.getStargate(address);

		if (!hasEnergyToDial(targetGatePos))
			return StargateOpenResult.NOT_ENOUGH_POWER;

		return StargateOpenResult.OK;
	}

	/**
	 * Checks if given address points to
	 * a valid target gate (and not to itself).
	 * @param address Address to check,
	 * @return {@code True} if the gate can be reached, {@code false} otherwise.
	 */
	protected boolean canDialAddress(StargateAddressDynamic address) {
		StargatePos targetGatePos = network.getStargate(address);

		if (targetGatePos == null)
			return false;

		if (targetGatePos.equals(gatePosMap.get(getSymbolType())))
			return false;

		boolean localDial = worldObj.provider.dimensionId == targetGatePos.dimensionID ||
				StargateDimensionConfig.isGroupEqual(worldObj.provider.dimensionId, DimensionManager.getProviderType(targetGatePos.dimensionID));

		// TODO Optimize this, prevent dimension from loading only to check the SymbolType...
		if (address.size() < getSymbolType().getMinimalSymbolCountTo(targetGatePos.getTileEntity().getSymbolType(), localDial))
			return false;

		int additional = address.size() - 7;

		if (additional > 0) {
			if (!address.getAdditional().subList(0, additional).equals(targetGatePos.additionalSymbols.subList(0, additional)))
				return false;
		}

		return true;
	}

	public void attemptClose(StargateClosedReasonEnum reason) {
		if((new StargateClosingEvent(this, targetGatePos.getTileEntity(), isInitiating, reason).post() || new StargateClosingEvent(targetGatePos.getTileEntity(), this, !isInitiating, reason).post()) && reason.equals(StargateClosedReasonEnum.REQUESTED))
			return;

		if (targetGatePos != null)
			targetGatePos.getTileEntity().closeGate(reason);

		closeGate(reason);
	}

	private static class ResultTargetValid {
		public final StargateOpenResult result;
		public final boolean targetVaild;

		public ResultTargetValid(StargateOpenResult result, boolean targetVaild) {
			this.result = result;
			this.targetVaild = targetVaild;
		}
	}

	// ------------------------------------------------------------------------
	// Stargate Network

	public abstract SymbolTypeEnum getSymbolType();

	/**
	 * Contains instance of {@link StargateAddress} which holds address of this gate.
	 */
	protected Map<SymbolTypeEnum, StargateAddress> gateAddressMap = new HashMap<>(3);
	protected Map<SymbolTypeEnum, StargatePos> gatePosMap = new HashMap<>(3);
	protected StargateAddressDynamic dialedAddress = new StargateAddressDynamic(getSymbolType());
	protected StargatePos targetGatePos;

	@Nullable
	public StargateAddress getStargateAddress(SymbolTypeEnum symbolType) {
		if (gateAddressMap == null)
			return null;

		return gateAddressMap.get(symbolType);
	}

	public void setGateAddress(SymbolTypeEnum symbolType, StargateAddress stargateAddress) {
		network.removeStargate(gateAddressMap.get(symbolType));

		StargatePos gatePos = new StargatePos(worldObj.provider.dimensionId, pos, stargateAddress);
		gateAddressMap.put(symbolType, stargateAddress);
		gatePosMap.put(symbolType, gatePos);
		network.addStargate(stargateAddress, gatePos);

		markDirty();
	}

	public StargateAddressDynamic getDialedAddress() {
		return dialedAddress;
	}

	protected int getMaxChevrons() {
		return 7;
	}

	protected boolean stargateWillLock(SymbolInterface symbol) {
		if (dialedAddress.size() == getMaxChevrons())
			return true;

		if (dialedAddress.size() >= 7 && symbol.origin())
			return true;

		return false;
	}

//	public void setGateAddress(StargateAddress gateAddress) {
//		if (network.isStargateInNetwork(gateAddress))
//			Aunis.LOG.error("Stargate with given address already exists");
//
//		if (network.isStargateInNetwork(gateAddress))
//			network.removeStargate(this.gateAddress);
//
//		StargateNetwork.get(worldObj).addStargate(gateAddress, new StargatePos(worldObj.provider.dimensionId, pos, gateAddress));
//
//		this.gateAddress = gateAddress;
//		markDirty();
//	}

	/**
	 * Checks whether the symbol can be added to the address.
	 *
	 * @param symbol Symbol to be added.
	 * @return
	 */
	public boolean canAddSymbol(SymbolInterface symbol){
		return canAddSymbolInternal(symbol) && !(new StargateChevronEngagedEvent(this, symbol, stargateWillLock(symbol)).post());
	}

	protected boolean canAddSymbolInternal(SymbolInterface symbol) {
		if (dialedAddress.contains(symbol))
			return false;

		if (dialedAddress.size() == getMaxChevrons())
			return false;

		return true;
	}

	/**
	 * Adds symbol to address. Called from GateRenderingUpdatePacketToServer.
	 *
	 * @param symbol Currently added symbol.
	 */
	protected void addSymbolToAddress(SymbolInterface symbol) {
		if (!canAddSymbol(symbol))
			throw new IllegalStateException("Cannot add that symbol");

		dialedAddress.addSymbol(symbol);

		if (stargateWillLock(symbol) && checkAddressAndEnergy(dialedAddress).ok()) {
			int size = dialedAddress.size();
			if (size == 6) size++;

			network.getStargate(dialedAddress).getTileEntity().incomingWormhole(size);
		}
	}

	/**
	 * Called on receiving gate. Sets renderer's state
	 *
	 * @param incomingAddress - Initializing gate's address
	 * @param dialedAddressSize - How many symbols are there pressed on the DHD
	 */
	public void incomingWormhole(int dialedAddressSize) {
		dialedAddress.clear();

		//sendSignal(null, "stargate_incoming_wormhole", new Object[] { dialedAddressSize });
	}

	protected int getOpenSoundDelay() {
		return EnumScheduledTask.STARGATE_OPEN_SOUND.waitTicks;
	}

	/**
	 * Called from {@link this#attemptOpenDialed()}. The address is valid here.
	 * It opens the gate unconditionally. Called only internally.
	 *
	 * @param targetGatePos Valid {@link StargatePos} pointing to the other Gate.
	 * @param isInitiating True if gate is initializing the connection, false otherwise.
	 */
	protected void openGate(StargatePos targetGatePos, boolean isInitiating) {
		this.isInitiating = isInitiating;
		this.targetGatePos = targetGatePos;
		this.stargateState = EnumStargateState.UNSTABLE;

		sendRenderingUpdate(StargateRendererActionState.EnumGateAction.OPEN_GATE, 0, false);

		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_OPEN_SOUND, getOpenSoundDelay()));
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_LIGHT_BLOCK, EnumScheduledTask.STARGATE_OPEN_SOUND.waitTicks + 19 + getTicksPerHorizonSegment(true)));
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_WIDEN, EnumScheduledTask.STARGATE_OPEN_SOUND.waitTicks + 23 + getTicksPerHorizonSegment(true))); // 1.3s of the sound to the kill
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_ENGAGE));

		if (isInitiating) {
			StargateEnergyRequired energyRequired = getEnergyRequiredToDial(targetGatePos);
			getEnergyStorage().extractEnergy(energyRequired.energyToOpen, false);
			keepAliveEnergyPerTick = energyRequired.keepAlive;
		}

		ChunkManager.forceChunk(worldObj, new ChunkPos(pos));

		//sendSignal(null, "stargate_open", new Object[] { isInitiating });

		markDirty();
	}

	/**
	 * Called either on pressing BRB on open gate or close command from a computer.
	 */
	protected void closeGate(StargateClosedReasonEnum reason) {
		stargateState = EnumStargateState.UNSTABLE;
		energySecondsToClose = 0;

		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CLOSE, 62));

		playSoundEvent(StargateSoundEventEnum.CLOSE);
		sendRenderingUpdate(EnumGateAction.CLOSE_GATE, 0, false);
		//sendSignal(null, "stargate_close", new Object[] { reason.toString().toLowerCase() });
		AunisSoundHelper.playPositionedSound(worldObj, getGateCenterPos(), SoundPositionedEnum.WORMHOLE_LOOP, false);

		if (isInitiating) {
			horizonFlashTask = null;
			isCurrentlyUnstable = false;
			updateFlashState(false);
		}

		targetGatePos = null;

		markDirty();
	}

	/**
	 * Called on the failed dialing.
	 */
	protected void dialingFailed(StargateOpenResult reason) {
		//sendSignal(null, "stargate_failed", new Object[] { reason.toString().toLowerCase() });
		horizonFlashTask = null;

		new StargateDialFailEvent(this, reason).post();

		addFailedTaskAndPlaySound();
		stargateState = EnumStargateState.FAILING;

		markDirty();
	}

	protected void addFailedTaskAndPlaySound() {
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL, 53));
		playSoundEvent(StargateSoundEventEnum.DIAL_FAILED);
	}

	/**
	 * Checks if {@link this#targetGatePos} points at a valid
	 * Stargate base block. If no, close the connection.
	 *
	 * @return True if the connecion is valid.
	 */
	protected boolean verifyConnection() {
		if (targetGatePos == null || !(targetGatePos.getTileEntity() instanceof StargateAbstractBaseTile)) {
			closeGate(StargateClosedReasonEnum.CONNECTION_LOST);
			return false;
		}

		return true;
	}


	// ------------------------------------------------------------------------
	// Sounds

	@Nullable
	protected abstract SoundPositionedEnum getPositionedSound(StargateSoundPositionedEnum soundEnum);

	@Nullable
	protected abstract SoundEventEnum getSoundEvent(StargateSoundEventEnum soundEnum);

	public void playPositionedSound(StargateSoundPositionedEnum soundEnum, boolean play) {
		SoundPositionedEnum positionedSound = getPositionedSound(soundEnum);

		if (positionedSound == null)
			throw new IllegalArgumentException("Tried to play " + soundEnum + " on " + getClass().getCanonicalName() + " which apparently doesn't support it.");

		if (worldObj.isRemote)
			Aunis.proxy.playPositionedSoundClientSide(getGateCenterPos(), positionedSound, play);
		else
			AunisSoundHelper.playPositionedSound(worldObj, getGateCenterPos(), positionedSound, play);
	}

	public void playSoundEvent(StargateSoundEventEnum soundEnum) {
		SoundEventEnum soundEvent = getSoundEvent(soundEnum);

		if (soundEvent == null)
			throw new IllegalArgumentException("Tried to play " + soundEnum + " on " + getClass().getCanonicalName() + " which apparently doesn't support it.");

		if (worldObj.isRemote)
			AunisSoundHelper.playSoundEventClientSide(worldObj, getGateCenterPos(), soundEvent);
		else
			AunisSoundHelper.playSoundEvent(worldObj, getGateCenterPos(), soundEvent);
	}

	// ------------------------------------------------------------------------
	// Ticking and loading

	public abstract BlockPos getGateCenterPos();

	protected TargetPoint targetPoint;
	protected EnumFacing facing = EnumFacing.NORTH;
	protected StargateNetwork network;

	public EnumFacing getFacing() {
		return facing;
	}

	@Override
	public void onLoad() {
		if (!worldObj.isRemote) {
			updateFacing(BaseUtils.getWorldBlockState(worldObj, pos).getValue(AunisProps.FACING_HORIZONTAL), true);
			network = StargateNetwork.get(worldObj);

			targetPoint = new TargetPoint(worldObj.provider.dimensionId, pos.getX(), pos.getY(), pos.getZ(), 512);
			Random random = new Random(pos.hashCode() * 31 + worldObj.provider.dimensionId);

			for (SymbolTypeEnum symbolType : SymbolTypeEnum.values()) {

				StargatePos stargatePos;

				if (gateAddressMap.get(symbolType) == null) {
					StargateAddress address = new StargateAddress(symbolType);
					address.generate(random);

					stargatePos = new StargatePos(worldObj.provider.dimensionId, pos, address);
					network.addStargate(address, stargatePos);
					gateAddressMap.put(symbolType, address);
//					Aunis.info(address.toString());
				}

				else {
					stargatePos = new StargatePos(worldObj.provider.dimensionId, pos, gateAddressMap.get(symbolType));
				}

				gatePosMap.put(symbolType, stargatePos);
			}

			if (stargateState.engaged()) {
				verifyConnection();
			}
		}

		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.RENDERER_STATE));
		}
	}

	private boolean addedToNetwork;

	@Override
	public void update() {
		// Scheduled tasks
		ScheduledTask.iterate(scheduledTasks, worldObj.getTotalWorldTime());

		if (!worldObj.isRemote) {

			// This cannot be done in onLoad because it makes
			// Stargates invisible to the network sometimes
			if (!addedToNetwork) {
				addedToNetwork = true;
				//Aunis.ocWrapper.joinWirelessNetwork(this);
				//Aunis.ocWrapper.joinOrCreateNetwork(this);
				// Aunis.info(pos + ": Stargate joined OC network");
			}

			if (stargateState.engaged() && targetGatePos == null) {
				Aunis.LOG.error("A stargateState indicates the Gate should be open, but targetGatePos is null. This is a bug. Closing gate...");
				attemptClose(StargateClosedReasonEnum.CONNECTION_LOST);
			}

			// Event horizon teleportation
			if (stargateState.initiating()) {
				eventHorizon.scheduleTeleportation(targetGatePos);
			}

			// Autoclose
			if (worldObj.getTotalWorldTime() % 20 == 0 && stargateState == EnumStargateState.ENGAGED && AunisConfig.autoCloseConfig.autocloseEnabled && shouldAutoclose()) {
				targetGatePos.getTileEntity().attemptClose(StargateClosedReasonEnum.AUTOCLOSE);
			}

			if (horizonFlashTask != null && horizonFlashTask.isActive()) {
				horizonFlashTask.update(worldObj.getTotalWorldTime());
			}

			// Event horizon killing
			if (horizonKilling) {
				List<EntityLivingBase> entities = new ArrayList<EntityLivingBase>();
				List<BlockPos> blocks = new ArrayList<BlockPos>();

				// Get all blocks and entities inside the kawoosh
				for (int i=0; i<horizonSegments; i++) {
					AunisAxisAlignedBB gBox = localKillingBoxes.get(i).offset(pos);

					entities.addAll(worldObj.getEntitiesWithinAABB(EntityLivingBase.class, gBox));

//					Aunis.info(new AxisAlignedBB((int)Math.floor(gBox.minX), (int)Math.floor(gBox.minY+1), (int)Math.floor(gBox.minZ), (int)Math.ceil(gBox.maxX-1), (int)Math.ceil(gBox.maxY-1), (int)Math.ceil(gBox.maxZ-1)).toString());
					for (BlockPos bPos : BlockPos.getAllInBox((int)Math.floor(gBox.minX), (int)Math.floor(gBox.minY), (int)Math.floor(gBox.minZ), (int)Math.ceil(gBox.maxX)-1, (int)Math.ceil(gBox.maxY)-1, (int)Math.ceil(gBox.maxZ)-1))
						blocks.add(bPos);
				}

				// Get all entities inside the gate
				for (AunisAxisAlignedBB lBox : localInnerEntityBoxes)
					entities.addAll(worldObj.getEntitiesWithinAABB(EntityLivingBase.class, lBox.offset(pos)));

				// Get all blocks inside the gate
				for (AunisAxisAlignedBB lBox : localInnerBlockBoxes) {
					AunisAxisAlignedBB gBox = lBox.offset(pos);

					for (BlockPos bPos : BlockPos.getAllInBox((int)gBox.minX, (int)gBox.minY, (int)gBox.minZ, (int)gBox.maxX-1, (int)gBox.maxY-1, (int)gBox.maxZ-1)) {
						// If not snow layer
						if (!DHDBlock.SNOW_MATCHER.apply(BaseUtils.getWorldBlockState(worldObj, bPos))) {
							blocks.add(bPos);
						}
					}
				}

				// Kill them
				for (EntityLivingBase entity : entities) {
					entity.attackEntityFrom(AunisDamageSources.DAMAGE_EVENT_HORIZON, 20);
					AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, StateTypeEnum.STARGATE_VAPORIZE_BLOCK_PARTICLES, new StargateVaporizeBlockParticlesRequest(entity.getPosition())), targetPoint);
				}

				// Vaporize them
				for (BlockPos dPos : blocks) {
					if (!dPos.equals(getGateCenterPos())) {
						IBlockState state = BaseUtils.getWorldBlockState(worldObj, dPos);

						if (!worldObj.isAirBlock(dPos.getX(), dPos.getY(), dPos.getZ()) && BaseUtils.getWorldBlock(worldObj, dPos).getBlockHardness(worldObj, dPos.getX(), dPos.getY(), dPos.getZ()) >= 0.0f && AunisConfig.stargateConfig.canKawooshDestroyBlock(state)) {
							worldObj.setBlockToAir(dPos.getX(), dPos.getY(), dPos.getZ());
							AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, StateTypeEnum.STARGATE_VAPORIZE_BLOCK_PARTICLES, new StargateVaporizeBlockParticlesRequest(dPos)), targetPoint);
						}
					}
				}
			}


			/*
			 * Draw power (engaged)
			 *
			 * If initiating
			 * 	True: Extract energy each tick
			 * 	False: Update the source gate about consumed energy each second
			 */
			if (stargateState.initiating()) {
				int energyStored = getEnergyStorage().getEnergyStored();
				energySecondsToClose = energyStored/(float)keepAliveEnergyPerTick / 20f;

				if (energySecondsToClose >= 1) {

					/*
					 * If energy can sustain connection for less than AunisConfig.powerConfig.instabilitySeconds seconds
					 * Start flickering
					 *
					 * 2020-04-25: changed the below to check if the gate is being sufficiently externally powered and, if so,
					 * do not start flickering even if the internal power isn't enough.
					 */

					// Horizon becomes unstable
					if (horizonFlashTask == null && energySecondsToClose < AunisConfig.powerConfig.instabilitySeconds && energyTransferedLastTick < 0) {
						resetFlashingSequence();

						setHorizonFlashTask(new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int) (Math.random() * 40) + 5));
					}

					// Horizon becomes stable
					if (horizonFlashTask != null && (energySecondsToClose > AunisConfig.powerConfig.instabilitySeconds || energyTransferedLastTick >= 0)) {
						horizonFlashTask = null;
						isCurrentlyUnstable = false;

						updateFlashState(false);
					}

					getEnergyStorage().extractEnergy(keepAliveEnergyPerTick, false);

					markDirty();
//					Aunis.info("Stargate energy: " + energyStorage.getEnergyStored() + " / " + energyStorage.getMaxEnergyStored() + "\t\tAlive for: " + (float)(energyStorage.getEnergyStored())/keepAliveCostPerTick/20);
				}

				else {
					attemptClose(StargateClosedReasonEnum.OUT_OF_POWER);
				}
			}

			energyTransferedLastTick = getEnergyStorage().getEnergyStored() - energyStoredLastTick;
			energyStoredLastTick = getEnergyStorage().getEnergyStored();
		}
	}

	public abstract EnumSet<BiomeOverlayEnum> getSupportedOverlays();

	/**
	 * Method for closing the gate using Autoclose mechanism.
	 * @return {@code True} if the gate should be closed, false otherwise.
	 */
	protected boolean shouldAutoclose() {
		return getAutoCloseManager().shouldClose(targetGatePos);
	}

	@Override
	public void onChunkUnload() {
		//if (node != null)
		//	node.remove();

		//Aunis.ocWrapper.leaveWirelessNetwork(this);
	}

	@Override
	public void invalidate() {
		//if (node != null)
		//	node.remove();

		//Aunis.ocWrapper.leaveWirelessNetwork(this);

		super.invalidate();
	}

	@Override
	public void rotate(Rotation rotation) {
		IBlockState state = BaseUtils.getWorldBlockState(worldObj, pos);

		EnumFacing facing = state.getValue(AunisProps.FACING_HORIZONTAL);
        BaseUtils.setWorldBlockState(worldObj, pos, state.withProperty(AunisProps.FACING_HORIZONTAL, rotation.rotate(facing)));
	}

	// ------------------------------------------------------------------------
	// Killing and block vaporizing

	/**
	 * Gets full {@link AxisAlignedBB} of the killing area.
	 * @param server Calling side.
	 * @return Approximate kawoosh size.
	 */
	protected abstract AunisAxisAlignedBB getHorizonKillingBox(boolean server);

	/**
	 * How many segments should the exclusion zone have.
	 * @param server Calling side.
	 * @return Count of subsegments of the killing box.
	 */
	protected abstract int getHorizonSegmentCount(boolean server);

	/**
	 * The event horizon in the gate also should kill
	 * and vaporize everything
	 * @param server Calling side.
	 * @return List of {@link AxisAlignedBB} for the inner gate area.
	 */
	protected abstract List<AunisAxisAlignedBB> getGateVaporizingBoxes(boolean server);

	/**
	 * How many ticks should the {@link StargateAbstractBaseTile} wait to perform
	 * next update to the size of the killing box.
	 * @param server Calling side
	 */
	protected int getTicksPerHorizonSegment(boolean server) {
		return 12 / getHorizonSegmentCount(server);
	}

	/**
	 * Contains all the subboxes to be activated with the kawoosh.
	 * On the server needs to be offsetted by the {@link TileEntity#getPos()}
	 */
	protected List<AunisAxisAlignedBB> localKillingBoxes;

	public List<AunisAxisAlignedBB> getLocalKillingBoxes() {
		return localKillingBoxes;
	}

	/**
	 * Contains all boxes of the inner part of the gate.
	 * Full blocks. Used for destroying blocks.
	 * On the server needs to be offsetted by the {@link TileEntity#getPos()}
	 */
	protected List<AunisAxisAlignedBB> localInnerBlockBoxes;

	public List<AunisAxisAlignedBB> getLocalInnerBlockBoxes() {
		return localInnerBlockBoxes;
	}

	/**
	 * Contains all boxes of the inner part of the gate.
	 * Not full blocks. Used for entity killing.
	 * On the server needs to be offsetted by the {@link TileEntity#getPos()}
	 */
	protected List<AunisAxisAlignedBB> localInnerEntityBoxes;

	public List<AunisAxisAlignedBB> getLocalInnerEntityBoxes() {
		return localInnerEntityBoxes;
	}

	private boolean horizonKilling = false;
	private int horizonSegments = 0;

	// ------------------------------------------------------------------------
	// Rendering

	private AxisAlignedBB renderBoundingBox = TileEntity.INFINITE_EXTENT_AABB;

	public AunisAxisAlignedBB getRenderBoundingBoxForDisplay() {
		return getRenderBoundingBoxRaw().rotate((int) facing.getHorizontalAngle()).offset(0.5, 0, 0.5);
	}

	protected StargateAbstractRendererState.StargateAbstractRendererStateBuilder getRendererStateServer() {
		return StargateAbstractRendererState.builder()
				.setStargateState(stargateState);
	}

	StargateAbstractRendererState rendererStateClient;
	protected abstract StargateAbstractRendererState createRendererStateClient();

	public StargateAbstractRendererState getRendererStateClient() {
		return rendererStateClient;
	}

	protected void setRendererStateClient(StargateAbstractRendererState rendererState) {
		this.rendererStateClient = rendererState;

		AunisSoundHelper.playPositionedSound(worldObj, getGateCenterPos(), SoundPositionedEnum.WORMHOLE_LOOP, rendererState.doEventHorizonRender);
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_LIGHTING_UPDATE_CLIENT, 10));
	}

	protected abstract AunisAxisAlignedBB getRenderBoundingBoxRaw();

	public void updateFacing(EnumFacing facing, boolean server) {
		this.facing = facing;
		this.eventHorizon = new EventHorizon(worldObj, pos, getGateCenterPos(), facing, getHorizonTeleportBox(server));
		this.renderBoundingBox = getRenderBoundingBoxRaw().rotate((int) facing.getHorizontalAngle()).offset(0.5, 0, 0.5).offset(pos);

		AunisAxisAlignedBB kBox = getHorizonKillingBox(server);
		double width = kBox.maxZ - kBox.minZ;
		width /= getHorizonSegmentCount(server);

		localKillingBoxes = new ArrayList<AunisAxisAlignedBB>(getHorizonSegmentCount(server));
		for (int i=0; i<getHorizonSegmentCount(server); i++) {
			AunisAxisAlignedBB box = new AunisAxisAlignedBB(kBox.minX, kBox.minY, kBox.minZ + width*i, kBox.maxX, kBox.maxY, kBox.minZ + width*(i+1));
			box = box.rotate(facing).offset(0.5, 0, 0.5);

			localKillingBoxes.add(box);
		}

		localInnerBlockBoxes = new ArrayList<AunisAxisAlignedBB>(3);
		localInnerEntityBoxes = new ArrayList<AunisAxisAlignedBB>(3);
		for (AunisAxisAlignedBB lBox : getGateVaporizingBoxes(server)) {
			localInnerBlockBoxes.add(lBox.rotate(facing).offset(0.5, 0, 0.5));
			localInnerEntityBoxes.add(lBox.grow(0, 0, -0.25).rotate(facing).offset(0.5, 0, 0.5));
		}
	}

	@Override
	public boolean shouldRefresh(World worldObj, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return renderBoundingBox;
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536;
	}


	// ------------------------------------------------------------------------
	// Merging

	private boolean isMerged;

	public final boolean isMerged() {
		return isMerged;
	}

	/**
	 * @return Appropriate merge helper
	 */
	public abstract StargateAbstractMergeHelper getMergeHelper();

	/**
	 * Checks gate's merge state
	 *
	 * @param shouldBeMerged - True if gate's multiblock structure is valid
	 * @param facing Facing of the base block.
	 */
	public final void updateMergeState(boolean shouldBeMerged, EnumFacing facing) {
		if (!shouldBeMerged) {
			if (isMerged)
				onGateBroken();

			if (stargateState.engaged()) {
				targetGatePos.getTileEntity().closeGate(StargateClosedReasonEnum.CONNECTION_LOST);
			}
		}

		else {
			onGateMerged();
		}

		this.isMerged = shouldBeMerged;
		IBlockState actualState = BaseUtils.getWorldBlockState(worldObj, pos);


		// When the block is destroyed, there will be air in this place and we cannot set it's block state
		if (getMergeHelper().matchBase(actualState)) {
            BaseUtils.setWorldBlockState(worldObj, pos, actualState.withProperty(AunisProps.RENDER_BLOCK, !shouldBeMerged));
		}

		getMergeHelper().updateMembersMergeStatus(worldObj, pos, facing, shouldBeMerged);

		markDirty();
	}

	// ------------------------------------------------------------------------
	// AutoClose

	public final void entityPassing(Entity entity, boolean inbound) {
		boolean isPlayer = entity instanceof EntityPlayerMP;

		if (isPlayer) {
			getAutoCloseManager().playerPassing();
			markDirty();
		}

		////sendSignal(null, "stargate_traveler", new Object[] {inbound, isPlayer, entity.getClass().getSimpleName()});
	}


	// -----------------------------------------------------------------
	// Horizon flashing
	private ScheduledTask horizonFlashTask;

	private void setHorizonFlashTask(ScheduledTask horizonFlashTask) {
		horizonFlashTask.setExecutor(this);
		horizonFlashTask.setTaskCreated(worldObj.getTotalWorldTime());

		this.horizonFlashTask = horizonFlashTask;
		markDirty();
	}

	private int flashIndex = 0;
	private boolean isCurrentlyUnstable = false;

	private void resetFlashingSequence() {
		flashIndex = 0;
		isCurrentlyUnstable = false;
	}

	private void updateFlashState(boolean flash) {
		AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, StateTypeEnum.FLASH_STATE, new StargateFlashState(isCurrentlyUnstable)), targetPoint);

		if (targetGatePos != null) {
			BlockPos tPos = targetGatePos.gatePos;
			AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(tPos, StateTypeEnum.FLASH_STATE, new StargateFlashState(isCurrentlyUnstable)), new TargetPoint(targetGatePos.dimensionID, tPos.getX(), tPos.getY(), tPos.getZ(), 512));
		}
	}


	// ------------------------------------------------------------------------
	// States

	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return getRendererStateServer().build();

			default:
				return null;
		}
	}

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return createRendererStateClient();

			case RENDERER_UPDATE:
				return new StargateRendererActionState();

			case STARGATE_VAPORIZE_BLOCK_PARTICLES:
				return new StargateVaporizeBlockParticlesRequest();

			case FLASH_STATE:
				return new StargateFlashState();

			default:
				return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_STATE:

				EnumFacing facing = BaseUtils.getWorldBlockState(worldObj, pos).getValue(AunisProps.FACING_HORIZONTAL);

				setRendererStateClient(((StargateAbstractRendererState) state).initClient(pos, facing, BiomeOverlayEnum.updateBiomeOverlay(worldObj, pos, getSupportedOverlays())));

				updateFacing(facing, false);

				break;

			case RENDERER_UPDATE:
				switch (((StargateRendererActionState) state).action) {
					case OPEN_GATE:
						getRendererStateClient().horizonSegments = 0;
						getRendererStateClient().openGate(worldObj.getTotalWorldTime());
						break;

					case CLOSE_GATE:
						getRendererStateClient().closeGate(worldObj.getTotalWorldTime());
						break;

					case STARGATE_HORIZON_WIDEN:
						getRendererStateClient().horizonSegments++;
						break;

					case STARGATE_HORIZON_SHRINK:
						getRendererStateClient().horizonSegments--;
						break;

					default:
						break;
				}

				break;

			case STARGATE_VAPORIZE_BLOCK_PARTICLES:
				BlockPos b = ((StargateVaporizeBlockParticlesRequest) state).block;

				for (int i=0; i<20; i++) {
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleWhiteSmoke(worldObj, b.getX() + (Math.random()-0.5), b.getY(), b.getZ() + (Math.random()-0.5), 0, 0, false));
				}

				break;

			case FLASH_STATE:
				if (getRendererStateClient() != null)
					getRendererStateClient().horizonUnstable = ((StargateFlashState) state).flash;

				break;

			default:
				break;
		}
	}

	// ------------------------------------------------------------------------
	// Scheduled tasks

	/**
	 * List of scheduled tasks to be performed on {@link ITickable#update()}.
	 */
	private List<ScheduledTask> scheduledTasks = new ArrayList<>();

	@Override
	public void addTask(ScheduledTask scheduledTask) {
		scheduledTask.setExecutor(this);
		scheduledTask.setTaskCreated(worldObj.getTotalWorldTime());

		scheduledTasks.add(scheduledTask);
		markDirty();
	}

	@Override
	public void executeTask(EnumScheduledTask scheduledTask, NBTTagCompound customData) {
		switch (scheduledTask) {
			case STARGATE_OPEN_SOUND:
				playSoundEvent(StargateSoundEventEnum.OPEN);
				break;

			case STARGATE_HORIZON_LIGHT_BLOCK:
                BaseUtils.setWorldBlockState(worldObj, getGateCenterPos(), AunisBlocks.INVISIBLE_BLOCK.getDefaultState().withProperty(AunisProps.HAS_COLLISIONS, false));
				break;

			case STARGATE_HORIZON_WIDEN:
				if (!horizonKilling)
					horizonKilling = true;

				horizonSegments++;
				AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, StargateRendererActionState.STARGATE_HORIZON_WIDEN_ACTION), targetPoint);

				if (horizonSegments < getHorizonSegmentCount(true))
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_WIDEN, getTicksPerHorizonSegment(true)));
				else
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_SHRINK, getTicksPerHorizonSegment(true) + 12));

				break;

			case STARGATE_HORIZON_SHRINK:
				horizonSegments--;
				AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, StargateRendererActionState.STARGATE_HORIZON_SHRINK_ACTION), targetPoint);

				if (horizonSegments > 0)
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_SHRINK, getTicksPerHorizonSegment(true) + 1));
				else
					horizonKilling = false;

				markDirty();

				break;

			case STARGATE_CLOSE:
				worldObj.setBlockToAir(getGateCenterPos().getX(), getGateCenterPos().getY(), getGateCenterPos().getZ());
				disconnectGate();
				break;

			case STARGATE_FAIL:
				failGate();
				break;

			case STARGATE_ENGAGE:
				// Gate destroyed mid-process
				if (verifyConnection()) {
					engageGate();
				}

				break;

			case STARGATE_LIGHTING_UPDATE_CLIENT:
                worldObj.func_147451_t(pos.getX(), pos.getY(), pos.getZ());
                worldObj.updateLightByType(EnumSkyBlock.Block, pos.getX(), pos.getY(), pos.getZ());

				break;

			case HORIZON_FLASH:
				isCurrentlyUnstable ^= true;

				if (isCurrentlyUnstable) {
					flashIndex++;

					if (flashIndex == 1 && targetGatePos != null) {
						AunisSoundHelper.playSoundEvent(worldObj, getGateCenterPos(), SoundEventEnum.WORMHOLE_FLICKER);
						AunisSoundHelper.playSoundEvent(targetGatePos.getWorldObj(), targetGatePos.getTileEntity().getGateCenterPos(), SoundEventEnum.WORMHOLE_FLICKER);
					}

					// Schedule change into stable state
					setHorizonFlashTask(new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int)(Math.random() * 3) + 3));
				}

				else {
					if (flashIndex == 1)
						// Schedule second flash
						setHorizonFlashTask(new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int)(Math.random() * 4) + 1));

					else {
						// Schedule next flash sequence
						float mul = energySecondsToClose / (float) AunisConfig.powerConfig.instabilitySeconds;
						int min = (int) (15 * mul);
						int off = (int) (20 * mul);
						setHorizonFlashTask(new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, min + (int)(Math.random() * off)));

						resetFlashingSequence();
					}
				}

				updateFlashState(isCurrentlyUnstable);

				markDirty();
				break;

			default:
				break;
		}
	}


	// -----------------------------------------------------------------
	// Power system

	private int keepAliveEnergyPerTick = 0;
	private int energyStoredLastTick = 0;
	protected int energyTransferedLastTick = 0;
	protected float energySecondsToClose = 0;

	public int getEnergyTransferedLastTick() {
		return energyTransferedLastTick;
	}

	public float getEnergySecondsToClose() {
		return energySecondsToClose;
	}

	protected abstract StargateAbstractEnergyStorage getEnergyStorage();

	protected StargateEnergyRequired getEnergyRequiredToDial(StargatePos targetGatePos) {
		BlockPos sPos = pos;
		BlockPos tPos = targetGatePos.gatePos;

        int sourceDim = worldObj.provider.dimensionId;
		int targetDim = targetGatePos.getWorldObj().provider.dimensionId;

		if (sourceDim == DimensionType.OVERWORLD && targetDim == DimensionType.NETHER)
			tPos = new BlockPos(tPos.getX()*8, tPos.getY(), tPos.getZ()*8);
		else if (sourceDim == DimensionType.NETHER && targetDim == DimensionType.OVERWORLD)
			sPos = new BlockPos(sPos.getX()*8, sPos.getY(), sPos.getZ()*8);

		double distance = (int) sPos.getDistance(tPos.getX(), tPos.getY(), tPos.getZ());

		if (distance < 5000)
			distance *= 0.8;
		else
			distance = 5000 * Math.log10(distance) / Math.log10(5000);

		StargateEnergyRequired energyRequired = new StargateEnergyRequired(AunisConfig.powerConfig.openingBlockToEnergyRatio, AunisConfig.powerConfig.keepAliveBlockToEnergyRatioPerTick);
		energyRequired = energyRequired.mul(distance).add(StargateDimensionConfig.getCost(worldObj.provider.dimensionId, targetDim));

		Aunis.LOG.info(String.format("Energy required to dial [distance=%,d, from=%s, to=%s] = %,d / keepAlive: %,d/t, stored=%,d",
				Math.round(distance),
				sourceDim,
				targetDim,
				energyRequired.energyToOpen,
				energyRequired.keepAlive,
				getEnergyStorage().getEnergyStored()));

		return energyRequired;
	}

	/**
	 * Checks is gate has sufficient power to dial across specified distance and dimension
	 * It also sets energy draw for (possibly) outgoing wormhole
	 *
	 * @param distance - distance in blocks to target gate
	 * @param targetWorld - target worldObj, used for multiplier
	 */
	public boolean hasEnergyToDial(StargatePos targetGatePos) {
		StargateEnergyRequired energyRequired = getEnergyRequiredToDial(targetGatePos);

		if (getEnergyStorage().getEnergyStored() >= energyRequired.energyToOpen) {
			return true;
		}

		return false;
	}


	// ------------------------------------------------------------------------
	// NBT
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		for (StargateAddress stargateAddress : gateAddressMap.values()) {
			compound.setTag("address_" + stargateAddress.getSymbolType(), stargateAddress.serializeNBT());
		}

		compound.setTag("dialedAddress", dialedAddress.serializeNBT());

		if (targetGatePos != null)
			compound.setTag("targetGatePos", targetGatePos.serializeNBT());

		compound.setBoolean("isMerged", isMerged);
		compound.setTag("autoCloseManager", getAutoCloseManager().serializeNBT());

		compound.setInteger("keepAliveCostPerTick", keepAliveEnergyPerTick);

		if (stargateState != null)
			compound.setInteger("stargateState", stargateState.id);

		compound.setTag("scheduledTasks", ScheduledTask.serializeList(scheduledTasks));

		compound.setTag("energyStorage", getEnergyStorage().serializeNBT());

		//if (node != null) {
		//	NBTTagCompound nodeCompound = new NBTTagCompound();
		//	node.save(nodeCompound);
		//
		//	compound.setTag("node", nodeCompound);
		//}

		compound.setBoolean("horizonKilling", horizonKilling);
		compound.setInteger("horizonSegments", horizonSegments);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		for (SymbolTypeEnum symbolType : SymbolTypeEnum.values()) {
			if (compound.hasKey("address_" + symbolType))
				gateAddressMap.put(symbolType, new StargateAddress(compound.getCompoundTag("address_" + symbolType)));
		}

		dialedAddress.deserializeNBT(compound.getCompoundTag("dialedAddress"));

		if (compound.hasKey("targetGatePos"))
			targetGatePos = new StargatePos(getSymbolType(), compound.getCompoundTag("targetGatePos"));

		isMerged = compound.getBoolean("isMerged");
		getAutoCloseManager().deserializeNBT(compound.getCompoundTag("autoCloseManager"));

		try {
			ScheduledTask.deserializeList(compound.getCompoundTag("scheduledTasks"), scheduledTasks, this);
		}

		catch (NullPointerException | IndexOutOfBoundsException | ClassCastException e) {
			Aunis.LOG.warn("Exception at reading NBT");
			Aunis.LOG.warn("If loading worldObj used with previous version and nothing game-breaking doesn't happen, please ignore it");

			e.printStackTrace();
		}

		getEnergyStorage().deserializeNBT(compound.getCompoundTag("energyStorage"));
		this.keepAliveEnergyPerTick = compound.getInteger("keepAliveCostPerTick");

		stargateState = EnumStargateState.valueOf(compound.getInteger("stargateState"));
		if (stargateState == null)
			stargateState = EnumStargateState.IDLE;

		//if (node != null && compound.hasKey("node"))
		//	node.load(compound.getCompoundTag("node"));

		horizonKilling = compound.getBoolean("horizonKilling");
		horizonSegments = compound.getInteger("horizonSegments");

		super.readFromNBT(compound);
	}

	@Override
	public boolean prepare(ICommandSender sender, ICommand command) {
		if (!stargateState.idle()) {
			CommandBase.func_152373_a(sender, command, "Stop any gate activity before preparation.");
			return false;
		}

		gateAddressMap.clear();
		dialedAddress.clear();
		scheduledTasks.clear();

		return true;
	}

	// ------------------------------------------------------------------------
	// OpenComputers

	/**
	 * Tries to find a {@link SymbolInterface} instance from
	 * Integer index or String name of the symbol.
	 * @param nameIndex Name or index.
	 * @return Symbol.
	 * @throws IllegalArgumentException When symbol/index is invalid.
	 */
	public SymbolInterface getSymbolFromNameIndex(Object nameIndex) throws IllegalArgumentException {
		SymbolInterface symbol = null;

		if (nameIndex instanceof Integer)
			symbol = getSymbolType().valueOfSymbol((Integer) nameIndex);

		else if (nameIndex instanceof byte[])
			symbol = getSymbolType().fromEnglishName(new String((byte[]) nameIndex));

		else if (nameIndex instanceof String)
			symbol = getSymbolType().fromEnglishName((String) nameIndex);

		if (symbol == null)
			throw new IllegalArgumentException("bad argument (symbol name/index invalid)");

		return symbol;
	}
}
