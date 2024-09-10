package com.mrjake.aunis.renderer.stargate;

import com.mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import com.mrjake.aunis.stargate.EnumStargateState;
import com.mrjake.aunis.state.State;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import io.netty.buffer.ByteBuf;
import com.mrjake.aunis.renderer.stargate.StargateAbstractRenderer.EnumVortexState;

public class StargateAbstractRendererState extends State {
	public StargateAbstractRendererState() {}

	protected StargateAbstractRendererState(StargateAbstractRendererStateBuilder builder) {
		if (builder.stargateState.engaged()) {
			doEventHorizonRender = true;
			vortexState = EnumVortexState.STILL;
		}
	}

	public StargateAbstractRendererState initClient(BlockPos pos, EnumFacing facing, BiomeOverlayEnum biomeOverlay) {
		this.pos = pos;
		this.facing = facing;

		if (facing.getAxis() == EnumFacing.Axis.X)
			facing = facing.getOpposite();

		this.horizontalRotation = facing.getHorizontalAngle();
		this.biomeOverlay = biomeOverlay;

		return this;
	}

	// Global
	// Not saved
	public BlockPos pos;
	public EnumFacing facing;
	public float horizontalRotation;
	private BiomeOverlayEnum biomeOverlay;

	// Gate
	// Saved
	public boolean doEventHorizonRender = false;
	public EnumVortexState vortexState = EnumVortexState.FORMING;

	// Event horizon
	// Not saved
	public StargateRendererStatic.QuadStrip backStrip;
	public boolean backStripClamp;
	public Float whiteOverlayAlpha;
	public long gateWaitStart = 0;
	public long gateWaitClose = 0;
	public boolean zeroAlphaSet;
	public boolean horizonUnstable = false;
	public int horizonSegments = 0;

	public void openGate(long totalWorldTime) {
		gateWaitStart = totalWorldTime;

		zeroAlphaSet = false;
		backStripClamp = true;
		whiteOverlayAlpha = 1.0f;

		vortexState = EnumVortexState.FORMING;
		doEventHorizonRender = true;
	}

	public void closeGate(long totalWorldTime) {
		gateWaitClose = totalWorldTime;
		vortexState = EnumVortexState.CLOSING;
	}

	public BiomeOverlayEnum getBiomeOverlay() {
		return biomeOverlay;
	}

	public void setBiomeOverlay(BiomeOverlayEnum biomeOverlay) {
		this.biomeOverlay = biomeOverlay;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(doEventHorizonRender);
		buf.writeInt(vortexState.index);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		doEventHorizonRender = buf.readBoolean();
		vortexState = EnumVortexState.valueOf( buf.readInt() );
	}


	// ------------------------------------------------------------------------
	// Builder

	public static StargateAbstractRendererStateBuilder builder() {
		return new StargateAbstractRendererStateBuilder();
	}

	public static class StargateAbstractRendererStateBuilder {

		// Gate
		protected EnumStargateState stargateState;

		public StargateAbstractRendererStateBuilder setStargateState(EnumStargateState stargateState) {
			this.stargateState = stargateState;
			return this;
		}

		public StargateAbstractRendererState build() {
			return new StargateAbstractRendererState(this);
		}
	}
}
