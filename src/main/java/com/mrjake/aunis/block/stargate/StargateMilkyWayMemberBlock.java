package com.mrjake.aunis.block.stargate;

import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.stargate.EnumMemberVariant;
import com.mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import com.mrjake.aunis.stargate.merging.StargateMilkyWayMergeHelper;
import com.mrjake.aunis.tileentity.stargate.StargateMilkyWayMemberTile;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class StargateMilkyWayMemberBlock extends StargateClassicMemberBlock {

	public static final String BLOCK_NAME = "stargate_milkyway_member_block";

	public final int RING_META = getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING));
	public final int CHEVRON_META = getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.CHEVRON));

	public StargateMilkyWayMemberBlock() {
		super(BLOCK_NAME);
		setResistance(2000.0f);
	}

	@Override
	protected StargateAbstractMergeHelper getMergeHelper() {
		return StargateMilkyWayMergeHelper.INSTANCE;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new StargateMilkyWayMemberTile();
	}
}
