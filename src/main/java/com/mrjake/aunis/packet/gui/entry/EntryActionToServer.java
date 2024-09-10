package com.mrjake.aunis.packet.gui.entry;

import com.mrjake.aunis.item.AunisItems;
import com.mrjake.aunis.item.dialer.UniverseDialerMode;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;

import java.nio.charset.StandardCharsets;

public class EntryActionToServer implements IMessage {
	public EntryActionToServer() {}

	private EntryDataTypeEnum dataType;
	private EntryActionEnum action;
	private int index;
	private String name;

	public EntryActionToServer(EntryDataTypeEnum dataType, EntryActionEnum action, int index, String name) {
		this.dataType = dataType;
		this.action = action;
		this.index = index;
		this.name = name;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dataType.ordinal());
		buf.writeInt(action.ordinal());
		buf.writeInt(index);

		buf.writeInt(name.length());
        buf.writeBytes(name.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		dataType = EntryDataTypeEnum.values()[buf.readInt()];
		action = EntryActionEnum.values()[buf.readInt()];
		index = buf.readInt();

		int size = buf.readInt();
        name = buf.readBytes(size).toString(StandardCharsets.UTF_8);
	}


	public static class EntryActionServerHandler implements IMessageHandler<EntryActionToServer, IMessage> {

		@Override
		public IMessage onMessage(EntryActionToServer message, MessageContext ctx) {
            NetHandlerPlayServer temp = ctx.getServerHandler();
            WorldServer world = temp.playerEntity.getServerForPlayer();
            EntityPlayerMP player = temp.playerEntity;

				ItemStack stack = player.getHeldItem();
				NBTTagCompound compound = stack.getTagCompound();

				if (message.dataType.page()) {
					NBTTagList list = compound.getTagList("addressList", NBT.TAG_COMPOUND);

					switch (message.action) {
						case RENAME:
							//NotebookItem.setNameForIndex(list, message.index, message.name);
							break;

						case MOVE_UP:
							tagSwitchPlaces(list, message.index, message.index-1);
							break;

						case MOVE_DOWN:
							tagSwitchPlaces(list, message.index, message.index+1);
							break;

						case REMOVE:
							NBTTagCompound selectedCompound = list.getCompoundTagAt(message.index);
							list.removeTag(message.index);

							if (list.tagCount() == 0)
                                player.inventory.setInventorySlotContents(message.index, null);
							else
								compound.setInteger("selected", Math.min(message.index, list.tagCount()-1));

							//ItemStack pageStack = new ItemStack(AunisItems.PAGE_NOTEBOOK_ITEM, 1, 1);
							//pageStack.setTagCompound(selectedCompound);
                            //player.inventory.addItemStackToInventory(pageStack);

							break;
					}
				}

				else if (message.dataType.universe()) {
					NBTTagList list = compound.getTagList(UniverseDialerMode.MEMORY.tagListName, NBT.TAG_COMPOUND);

					switch (message.action) {
						case RENAME:
							//UniverseDialerItem.setMemoryNameForIndex(list, message.index, message.name);
							break;

						case MOVE_UP:
							tagSwitchPlaces(list, message.index, message.index-1);
							break;

						case MOVE_DOWN:
							tagSwitchPlaces(list, message.index, message.index+1);
							break;

						case REMOVE:
							list.removeTag(message.index);

							UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));
							if (mode == UniverseDialerMode.MEMORY)
								compound.setByte("selected", (byte) Math.min(message.index, list.tagCount()-1));

							break;
					}
				}

				else if (message.dataType.oc()) {
					NBTTagList list = compound.getTagList(UniverseDialerMode.OC.tagListName, NBT.TAG_COMPOUND);

					switch (message.action) {
						case RENAME:
							//UniverseDialerItem.changeOCMessageAtIndex(list, message.index, (ocMessage) -> ocMessage.name = message.name);
							break;

						case MOVE_UP:
							tagSwitchPlaces(list, message.index, message.index-1);
							break;

						case MOVE_DOWN:
							tagSwitchPlaces(list, message.index, message.index+1);
							break;

						case REMOVE:
							list.removeTag(message.index);

							UniverseDialerMode mode = UniverseDialerMode.valueOf(compound.getByte("mode"));
							if (mode == UniverseDialerMode.OC)
								compound.setByte("selected", (byte) Math.min(message.index, list.tagCount()-1));

							break;
					}
				}

			return null;
		}

	}

    private static void tagSwitchPlaces(NBTTagList list, int a, int b) {
        NBTBase tagA = list.removeTag(a);
        NBTBase tagB = list.removeTag(b - 1);
        list.appendTag(tagB);
        list.appendTag(tagA);
    }
}
