package com.mrjake.aunis.packet.stargate;

import com.mrjake.aunis.packet.PositionedPacket;
import com.mrjake.aunis.stargate.EnumStargateState;
import com.mrjake.aunis.stargate.StargateClosedReasonEnum;
import com.mrjake.aunis.stargate.StargateOpenResult;
import com.mrjake.aunis.stargate.network.SymbolMilkyWayEnum;
import com.mrjake.aunis.tileentity.DHDTile;
import com.mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import com.mrjake.aunis.util.minecraft.BlockPos;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.WorldServer;

public class DHDButtonClickedToServer extends PositionedPacket {
	public DHDButtonClickedToServer() {}

	public SymbolMilkyWayEnum symbol;

	public DHDButtonClickedToServer(BlockPos pos, SymbolMilkyWayEnum symbol) {
		super(pos);
		this.symbol = symbol;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);

		buf.writeInt(symbol.id);
	}

	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);

		symbol = SymbolMilkyWayEnum.valueOf(buf.readInt());
	}


	public static class DHDButtonClickedServerHandler implements IMessageHandler<DHDButtonClickedToServer, IMessage> {

		@Override
		public IMessage onMessage(DHDButtonClickedToServer message, MessageContext ctx) {
            NetHandlerPlayServer temp = ctx.getServerHandler();
            EntityPlayerMP player = temp.playerEntity;
            WorldServer world = player.getServerForPlayer();

				DHDTile dhdTile = (DHDTile) world.getTileEntity(message.pos.getX(), message.pos.getY(), message.pos.getZ());

				if (dhdTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0).isEmpty()) {
					player.addChatMessage(new ChatComponentTranslation("tile.aunis.dhd_block.no_crystal_warn"));
					return;
				}

				if (!dhdTile.isLinked()) {
					player.addChatMessage(new ChatComponentTranslation("tile.aunis.dhd_block.not_linked_warn"));
					return;
				}

				StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) dhdTile.getLinkedGate(world);
				EnumStargateState gateState = gateTile.getStargateState();

				if (gateState.engaged() && message.symbol.brb()) {
					// Gate is open, BRB was press, possible closure attempt

					if (gateState.initiating())
						gateTile.attemptClose(StargateClosedReasonEnum.REQUESTED);
					else
						player.addChatMessage(new ChatComponentTranslation("tile.aunis.dhd_block.incoming_wormhole_warn"));
				}

				else if (gateState.idle()) {
					// Gate is idle, some glyph was pressed

					if (message.symbol.brb()) {
						// BRB pressed on idling gate, attempt to open

						StargateOpenResult openResult = gateTile.attemptOpenAndFail();

						if (openResult == StargateOpenResult.NOT_ENOUGH_POWER) {
							player.addChatMessage(new ChatComponentTranslation("tile.aunis.stargatebase_block.not_enough_power"));
						}
					}

					else if (gateTile.canAddSymbol(message.symbol)) {
						// Not BRB, some other glyph pressed on idling gate, we can add this symbol now

						gateTile.addSymbolToAddressDHD(message.symbol);
					}
				}

			return null;
		}
	}
}
