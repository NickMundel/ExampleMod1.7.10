package com.mrjake.aunis.state;

import com.mrjake.aunis.beamer.BeamerStatusEnum;
import io.netty.buffer.ByteBuf;

public class BeamerRendererUpdate extends State {
	public BeamerRendererUpdate() {}

	public BeamerStatusEnum beamerStatus;

	public BeamerRendererUpdate(BeamerStatusEnum beamerStatus) {
		this.beamerStatus = beamerStatus;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(beamerStatus.getKey());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		beamerStatus = BeamerStatusEnum.valueOf(buf.readInt());
	}

}
