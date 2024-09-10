package com.mrjake.aunis.state;

import com.mrjake.aunis.beamer.BeamerModeEnum;
import com.mrjake.aunis.beamer.BeamerRoleEnum;
import com.mrjake.aunis.beamer.BeamerStatusEnum;
import io.netty.buffer.ByteBuf;

public class BeamerRendererState extends State {
	public BeamerRendererState() {}

	public BeamerModeEnum beamerMode;
	public BeamerRoleEnum beamerRole;
	public BeamerStatusEnum beamerStatus;
	public boolean isObstructed;
	public int beamLength;

	public BeamerRendererState(BeamerModeEnum beamerMode, BeamerRoleEnum beamerRole, BeamerStatusEnum beamerStatus, boolean isObstructed, int beamLength) {
		this.beamerMode = beamerMode;
		this.beamerRole = beamerRole;
		this.beamerStatus = beamerStatus;
		this.isObstructed = isObstructed;
		this.beamLength = beamLength;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(beamerMode.getKey());
		buf.writeInt(beamerRole.getKey());
		buf.writeInt(beamerStatus.getKey());
		buf.writeBoolean(isObstructed);
		buf.writeInt(beamLength);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		beamerMode = BeamerModeEnum.valueOf(buf.readInt());
		beamerRole = BeamerRoleEnum.valueOf(buf.readInt());
		beamerStatus = BeamerStatusEnum.valueOf(buf.readInt());
		isObstructed = buf.readBoolean();
		beamLength = buf.readInt();
	}

}
