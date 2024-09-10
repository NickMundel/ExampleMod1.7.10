package com.mrjake.aunis.state;

import com.mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import io.netty.buffer.ByteBuf;

public class StargateBiomeOverrideState extends State {
	public StargateBiomeOverrideState() {}

	public BiomeOverlayEnum biomeOverride;

	public StargateBiomeOverrideState(BiomeOverlayEnum biomeOverride) {
		this.biomeOverride = biomeOverride;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (biomeOverride != null) {
			buf.writeBoolean(true);
			buf.writeInt(biomeOverride.ordinal());
		}

		else {
			buf.writeBoolean(false);
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		if (buf.readBoolean()) {
			biomeOverride = BiomeOverlayEnum.values()[buf.readInt()];
		}
	}

}
