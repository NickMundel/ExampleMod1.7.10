package com.mrjake.aunis.gui.container;

import com.mrjake.aunis.state.State;
import io.netty.buffer.ByteBuf;

public class StargateContainerGuiUpdate extends State {
	public StargateContainerGuiUpdate() {}

	public int energyStored;
	public int transferedLastTick;
	public float secondsToClose;

	public StargateContainerGuiUpdate(int energyStored, int transferedLastTick, float secondsToClose) {
		this.energyStored = energyStored;
		this.transferedLastTick = transferedLastTick;
		this.secondsToClose = secondsToClose;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energyStored);
		buf.writeInt(transferedLastTick);
		buf.writeFloat(secondsToClose);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		energyStored = buf.readInt();
		transferedLastTick = buf.readInt();
		secondsToClose = buf.readFloat();
	}
}
