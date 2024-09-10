package com.mrjake.aunis.renderer.stargate;

import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.stargate.EnumMemberVariant;
import com.mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import com.mrjake.aunis.stargate.merging.StargateMilkyWayMergeHelper;
import com.mrjake.aunis.util.FacingToRotation;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.world.EnumSkyBlock;

import java.util.HashMap;
import java.util.Map;

public abstract class StargateClassicRenderer<S extends StargateClassicRendererState> extends StargateAbstractRenderer<S> {

	@Override
	protected void applyLightMap(StargateClassicRendererState rendererState, double partialTicks) {
		final int chevronCount = 6;
		int skyLight = 0;
		int blockLight = 0;

		for (int i=0; i<chevronCount; i++) {
			BlockPos blockPos = StargateMilkyWayMergeHelper.INSTANCE.getChevronBlocks().get(i).rotate(FacingToRotation.get(rendererState.facing)).add(rendererState.pos);

			skyLight += getWorldObj().getSkyBlockTypeBrightness(EnumSkyBlock.Sky, blockPos.getX(), blockPos.getY(), blockPos.getZ());
			blockLight += getWorldObj().getSkyBlockTypeBrightness(EnumSkyBlock.Block, blockPos.getX(), blockPos.getY(), blockPos.getZ());
		}

		skyLight /= chevronCount;
		blockLight /= chevronCount;

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight * 16, skyLight * 16);
	}

	@Override
	protected Map<BlockPos, IBlockState> getMemberBlockStates(StargateAbstractMergeHelper mergeHelper, EnumFacing facing) {
		Map<BlockPos, IBlockState> map = new HashMap<BlockPos, IBlockState>();

		for (BlockPos pos : mergeHelper.getRingBlocks())
			map.put(pos, mergeHelper.getMemberBlock().getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING).withProperty(AunisProps.FACING_HORIZONTAL, facing));

		for (BlockPos pos : mergeHelper.getChevronBlocks())
			map.put(pos, mergeHelper.getMemberBlock().getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.CHEVRON).withProperty(AunisProps.FACING_HORIZONTAL, facing));

		return map;
	}


	// ----------------------------------------------------------------------------------------
	// Chevrons

	protected abstract void renderChevron(S rendererState, double partialTicks, ChevronEnum chevron);

	protected void renderChevrons(S rendererState, double partialTicks) {
		for (ChevronEnum chevron : ChevronEnum.values())
			renderChevron(rendererState, partialTicks, chevron);

		rendererState.chevronTextureList.iterate(getWorldObj(), partialTicks);
	}
}
