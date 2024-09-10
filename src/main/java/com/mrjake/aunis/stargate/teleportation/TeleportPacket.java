package com.mrjake.aunis.stargate.teleportation;

import com.mrjake.aunis.sound.AunisSoundHelper;
import com.mrjake.aunis.sound.SoundEventEnum;
import com.mrjake.aunis.stargate.network.StargatePos;
import com.mrjake.aunis.util.minecraft.BlockPos;
import net.minecraft.entity.Entity;

import javax.vecmath.Vector2f;


/**
 * Class used for teleporting entities with saving their motion
 *
 * @author MrJake222
 */
public class TeleportPacket {
	private BlockPos sourceGatePos;
	private StargatePos targetGatePos;

	private Entity entity;

	private float rotation;
	private Vector2f motionVector;

	public TeleportPacket(Entity entity, BlockPos source, StargatePos target, float rotation) {
		this.entity = entity;
		this.sourceGatePos = source;
		this.targetGatePos = target;

		this.rotation = rotation;
	}

	public StargatePos getTargetGatePos() {
		return targetGatePos;
	}

	public Entity getEntity() {
		return entity;
	}

	public void teleport() {
		TeleportHelper.teleportEntity(entity, sourceGatePos, targetGatePos, rotation, motionVector);

		AunisSoundHelper.playSoundEvent(targetGatePos.getWorldObj(), targetGatePos.getTileEntity().getGateCenterPos(), SoundEventEnum.WORMHOLE_GO);
	}

	public TeleportPacket setMotion(Vector2f motion) {
		this.motionVector = motion;

		return this;
	}
}
