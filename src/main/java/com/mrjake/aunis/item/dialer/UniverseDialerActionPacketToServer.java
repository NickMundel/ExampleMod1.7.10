package com.mrjake.aunis.item.dialer;

import com.mrjake.aunis.item.AunisItems;
import com.mrjake.aunis.util.minecraft.BlockPos;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;

public class UniverseDialerActionPacketToServer implements IMessage {
	public UniverseDialerActionPacketToServer() {}

	private UniverseDialerActionEnum action;
	private boolean next;

	public UniverseDialerActionPacketToServer(UniverseDialerActionEnum action, boolean next) {
		this.action = action;
		this.next = next;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(action.ordinal());
		buf.writeBoolean(next);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		action = UniverseDialerActionEnum.values()[buf.readInt()];
		next = buf.readBoolean();
	}


	public static class UniverseDialerActionPacketServerHandler implements IMessageHandler<UniverseDialerActionPacketToServer, IMessage> {

		@Override
		public IMessage onMessage(UniverseDialerActionPacketToServer message, MessageContext ctx) {
            NetHandlerPlayServer temp = ctx.getServerHandler();
            WorldServer world = temp.playerEntity.getServerForPlayer();
            EntityPlayerMP player = temp.playerEntity;

				ItemStack stack = player.getHeldItem();
                //TODO:FIX
				if (stack.getItem() == AunisItems.BEAMER_CRYSTAL_ITEMS && stack.hasTagCompound()) {
					NBTTagCompound compound = stack.getTagCompound();
					UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));
					byte selected = compound.getByte("selected");

					switch (message.action) {

						case MODE_CHANGE:
							if (message.next) // message.offset < 0
								mode = mode.next();
							else
								mode = mode.prev();

							compound.setByte("mode", mode.id);
							compound.setByte("selected", (byte) 0);

							break;


						case ADDRESS_CHANGE:
							int addressCount = compound.getTagList(mode.tagListName, NBT.TAG_COMPOUND).tagCount();

							if (message.next && selected < addressCount-1) // message.offset < 0
								compound.setByte("selected", (byte) (selected+1));

							if (!message.next && selected > 0)
								compound.setByte("selected", (byte) (selected-1));

							break;


						case ABORT:
							if (compound.hasKey("linkedGate")) {
								BlockPos pos = BlockPos.fromLong(compound.getLong("linkedGate"));
								//StargateUniverseBaseTile gateTile = (StargateUniverseBaseTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());

								//if (gateTile.getStargateSEnumStargateState.DIALING) {
								//	gateTile.abort();
								//	player.addChatMessage(new ChatComponentTranslation("item.aunis.universe_dialer.aborting"));
								//}

								//else {
								//	player.addChatMessage(new ChatComponentTranslation("item.aunis.universe_dialer.not_dialing"));
								//}
							}

							else {
								player.addChatMessage(new ChatComponentTranslation("item.aunis.universe_dialer.not_linked"));
							}

							break;
					}
				}

			return null;
		}

	}
}
