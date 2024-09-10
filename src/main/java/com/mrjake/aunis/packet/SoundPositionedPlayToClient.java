package com.mrjake.aunis.packet;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.sound.SoundPositionedEnum;
import com.mrjake.aunis.util.minecraft.BlockPos;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SoundPositionedPlayToClient extends PositionedPacket {
	public SoundPositionedPlayToClient() {}

	public SoundPositionedEnum soundEnum;
	public boolean play;

	public SoundPositionedPlayToClient(BlockPos pos, SoundPositionedEnum soundEnum, boolean play) {
		super(pos);

		this.soundEnum = soundEnum;
		this.play = play;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);

		buf.writeInt(soundEnum.id);
		buf.writeBoolean(play);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);

		soundEnum = SoundPositionedEnum.valueOf(buf.readInt());
		play = buf.readBoolean();
	}


	public static class PlayPositionedSoundClientHandler implements IMessageHandler<SoundPositionedPlayToClient, IMessage> {

		@Override
		public IMessage onMessage(SoundPositionedPlayToClient message, MessageContext ctx) {
			Aunis.proxy.addScheduledTaskClientSide(() -> {
				Aunis.proxy.playPositionedSoundClientSide(message.pos, message.soundEnum, message.play);
			});

			return null;
		}

	}
}
