package com.mrjake.aunis.packet;

import com.mrjake.aunis.tileentity.util.RedstoneModeEnum;
import com.mrjake.aunis.util.minecraft.BlockPos;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;

public class ChangeRedstoneModeToServer extends PositionedPacket {
	public ChangeRedstoneModeToServer() {}

	public RedstoneModeEnum mode;

	public ChangeRedstoneModeToServer(BlockPos pos, RedstoneModeEnum mode) {
		super(pos);
		this.mode = mode;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(mode.getKey());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		mode = RedstoneModeEnum.valueOf(buf.readInt());
	}


	public static class ChangeRedstoneModeServerHandler implements IMessageHandler<ChangeRedstoneModeToServer, IMessage> {

		@Override
		public IMessage onMessage(ChangeRedstoneModeToServer message, MessageContext ctx) {
            NetHandlerPlayServer temp = ctx.getServerHandler();
            WorldServer world = temp.playerEntity.getServerForPlayer();

                //TODO: BEAMER
				//BeamerTile beamerTile = (BeamerTile) world.getTileEntity(message.pos);
				//beamerTile.setRedstoneMode(message.mode);

			return null;
		}

	}
}
