package com.mrjake.aunis.renderer.activation;


import com.mrjake.aunis.renderer.stargate.ChevronEnum;

public class StargateActivation extends Activation<ChevronEnum> {

	public StargateActivation(ChevronEnum textureKey, long stateChange, boolean dim) {
		super(textureKey, stateChange, dim);
	}

	@Override
	protected float getMaxStage() {
		return 10;
	}

	@Override
	protected float getTickMultiplier() {
		return 3;
	}
}
