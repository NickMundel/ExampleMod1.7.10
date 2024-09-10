package com.mrjake.aunis.packet.stargate;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.util.minecraft.BlockPos;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class StargateMotionToClient implements IMessage {
	public StargateMotionToClient() {}

	private BlockPos gatePos;

	public StargateMotionToClient(BlockPos gatePos) {
		this.gatePos = gatePos;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( gatePos.toLong() );
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		gatePos = BlockPos.fromLong( buf.readLong() );
	}


	public static class RetrieveMotionClientHandler implements IMessageHandler<StargateMotionToClient, IMessage> {

		@Override
		public IMessage onMessage(StargateMotionToClient message, MessageContext ctx) {

			Aunis.proxy.addScheduledTaskClientSide(() -> {
				EntityPlayer player = Aunis.proxy.getPlayerClientSide();

				AunisPacketHandler.INSTANCE.sendToServer( new StargateMotionToServer(player.getEntityId(), message.gatePos, (float)player.motionX, (float)player.motionZ) );
			});

			return null;
		}

	}



}
