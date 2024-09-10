package com.mrjake.aunis.packet.stargate;

import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.packet.PositionedPacket;
import com.mrjake.aunis.stargate.teleportation.TeleportHelper;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import javax.vecmath.Vector2f;

public class StargateMotionToServer extends PositionedPacket {
	public StargateMotionToServer() {}

	private int entityId;
	private float motionX;
	private float motionZ;

	public StargateMotionToServer(int entityId, BlockPos pos, float motionX, float motionZ) {
		super(pos);

		this.entityId = entityId;
		this.motionX = motionX;
		this.motionZ = motionZ;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);

		buf.writeInt(entityId);

		buf.writeFloat(motionX);
		buf.writeFloat(motionZ);

	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);

		entityId = buf.readInt();

		motionX = buf.readFloat();
		motionZ = buf.readFloat();
	}


	public static class MotionServerHandler implements IMessageHandler<StargateMotionToServer, IMessage> {

		@Override
		public IMessage onMessage(StargateMotionToServer message, MessageContext ctx) {
            NetHandlerPlayServer temp = ctx.getServerHandler();
			WorldServer world = temp.playerEntity.getServerForPlayer();

				EnumFacing sourceFacing = BaseUtils.getWorldBlockState(world, message.pos).getValue(AunisProps.FACING_HORIZONTAL);

				StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) world.getTileEntity(message.pos.getX(), message.pos.getY(), message.pos.getZ());

				Vector2f motionVector = new Vector2f(message.motionX, message.motionZ);

				if (TeleportHelper.frontSide(sourceFacing, motionVector)) {
					gateTile.setMotionOfPassingEntity(message.entityId, motionVector);
					gateTile.teleportEntity(message.entityId);
				}

				else {
                    gateTile.removeEntity(message.entityId);
                }

			return null;
		}

	}
}
