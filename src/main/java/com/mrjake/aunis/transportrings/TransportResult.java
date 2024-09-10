package com.mrjake.aunis.transportrings;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

import javax.annotation.Nullable;

public enum TransportResult {
	OK(null),
	BUSY(new ChatComponentTranslation("tile.aunis.transportrings_block.busy")),
	BUSY_TARGET(new ChatComponentTranslation("tile.aunis.transportrings_block.busy_target")),
	OBSTRUCTED(new ChatComponentTranslation("tile.aunis.transportrings_block.obstructed")),
	OBSTRUCTED_TARGET(new ChatComponentTranslation("tile.aunis.transportrings_block.obstructed_target")),
	NO_SUCH_ADDRESS(new ChatComponentTranslation("tile.aunis.transportrings_block.non_existing_address"));

	@Nullable
	public ChatComponentTranslation textComponent;

	private TransportResult(ChatComponentTranslation textComponent) {
		this.textComponent = textComponent;

	}

	public boolean ok() {
		return this == OK;
	}

	public void sendMessageIfFailed(EntityPlayer player) {
		if (!ok()) {
			player.addChatMessage(textComponent);
		}
	}
}
