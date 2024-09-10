package com.mrjake.aunis.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;

public class SetOpenTabToServer implements IMessage {
	public SetOpenTabToServer() {}

	public int openTabId;

	public SetOpenTabToServer(int openTabId) {
		this.openTabId = openTabId;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(openTabId);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		openTabId = buf.readInt();
	}


	public static class SetOpenTabServerHandler implements IMessageHandler<SetOpenTabToServer, IMessage> {

		@Override
		public IMessage onMessage(SetOpenTabToServer message, MessageContext ctx) {
            NetHandlerPlayServer temp = ctx.getServerHandler();
            Container container = temp.playerEntity.openContainer;

            //TODO: GUI
			//if (container instanceof OpenTabHolderInterface) {
			//	((OpenTabHolderInterface) container).setOpenTabId(message.openTabId);
			//}

			return null;
		}

	}
}
