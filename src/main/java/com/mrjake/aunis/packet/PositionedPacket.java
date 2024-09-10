package com.mrjake.aunis.packet;

import com.mrjake.aunis.util.minecraft.BlockPos;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class PositionedPacket implements IMessage {
	public PositionedPacket() {}

	protected BlockPos pos;

	public PositionedPacket(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
	}
}
