package com.mrjake.aunis.stargate.teleportation;

import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.api.event.StargateTeleportEntityEvent;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.packet.stargate.StargateMotionToClient;
import com.mrjake.aunis.sound.AunisSoundHelper;
import com.mrjake.aunis.sound.SoundEventEnum;
import com.mrjake.aunis.stargate.network.StargatePos;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import com.mrjake.aunis.util.AunisAxisAlignedBB;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import javax.vecmath.Vector2f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHorizon {
	private World world;
	private BlockPos pos;
	private BlockPos gateCenter;

	private AunisAxisAlignedBB localBox;
	private AunisAxisAlignedBB globalBox;

	public EventHorizon(World world, BlockPos pos, BlockPos gateCenter, EnumFacing facing, AunisAxisAlignedBB localBox) {
		this.world = world;
		this.pos = pos;
		this.gateCenter = gateCenter;

		this.localBox = localBox.rotate(facing).offset(0.5, 0, 0.5);
		this.globalBox = this.localBox.offset(pos);
	}

	public void reset() {
		scheduledTeleportMap.clear();
	}

	public AunisAxisAlignedBB getLocalBox() {
		return localBox;
	}

	// ------------------------------------------------------------------------
	// Teleporting

	private Map<Integer, TeleportPacket> scheduledTeleportMap = new HashMap<>();

	/**
	 * This map is used not to double the teleport packet on Entity's
	 * passengers.
	 */
	private Map<Integer, Integer> timeoutMap = new HashMap<>();

	public void scheduleTeleportation(StargatePos targetGate) {
		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, globalBox);

//		Aunis.info(globalBox + ": " + entities + ", map: " + scheduledTeleportMap);

//		if (!timeoutMap.isEmpty())
//			Aunis.info("timeoutMap: " + timeoutMap);

		for (int entityId : timeoutMap.keySet())
			timeoutMap.put(entityId, timeoutMap.get(entityId)-1);
		timeoutMap.entrySet().removeIf(entry -> entry.getValue() < 0);

		for (Entity entity : entities) {
			int entityId = entity.getEntityId();

			if (!scheduledTeleportMap.containsKey(entityId) && !timeoutMap.containsKey(entityId) && !entity.isRiding()) {
				EnumFacing sourceFacing = BaseUtils.getWorldBlockState(world, pos).getValue(AunisProps.FACING_HORIZONTAL);
				EnumFacing targetFacing = targetGate.getBlockState().getValue(AunisProps.FACING_HORIZONTAL);

				float rotation = (float) Math.toRadians( EnumFacing.fromAngle(targetFacing.getHorizontalAngle() - sourceFacing.getHorizontalAngle()).getOpposite().getHorizontalAngle() );
				TeleportPacket packet = new TeleportPacket(entity, pos, targetGate, rotation);

				if (entity instanceof EntityPlayerMP) {
					scheduledTeleportMap.put(entityId, packet);
					AunisPacketHandler.INSTANCE.sendTo(new StargateMotionToClient(pos), (EntityPlayerMP) entity);
				}

				else {
					Vector2f motion = new Vector2f( (float)entity.motionX, (float)entity.motionZ );

					if (TeleportHelper.frontSide(sourceFacing, motion)) {
                        Entity ridingEntity = entity.ridingEntity;
                        while (ridingEntity != null) {

                            timeoutMap.put(ridingEntity.getEntityId(), 40);
                            ridingEntity = ridingEntity.ridingEntity;
                        }
						timeoutMap.put(entityId, 40);

						scheduledTeleportMap.put(entityId, packet.setMotion(motion) );
						teleportEntity(entityId);
					}

					/*else {
						// TODO Back side killing
						// Make custom message appear
						// entity.onKillCommand();
					}*/
				}
			}
		}
	}

	public void teleportEntity(int entityId) {
		TeleportPacket packet = scheduledTeleportMap.get(entityId);
		if (!new StargateTeleportEntityEvent((StargateAbstractBaseTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ()), packet.getTargetGatePos().getTileEntity(), packet.getEntity()).post()) {
			// Not cancelled
			packet.teleport();
			AunisSoundHelper.playSoundEvent(world, gateCenter, SoundEventEnum.WORMHOLE_GO);
		};

		scheduledTeleportMap.remove(entityId);
	}

	public void removeEntity(int entityId) {
		scheduledTeleportMap.remove(entityId);
	}

	public void setMotion(int entityId, Vector2f motionVector) {
		scheduledTeleportMap.get(entityId).setMotion(motionVector);
	}
}
