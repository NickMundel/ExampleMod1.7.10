package com.mrjake.aunis.block.stargate;

import com.mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class StargateMilkyWayBaseBlock extends StargateClassicBaseBlock {

	public static final String BLOCK_NAME = "stargate_milkyway_base_block";

	public StargateMilkyWayBaseBlock() {
		super(BLOCK_NAME);
		setResistance(2000.0f);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new StargateMilkyWayBaseTile();
	}
}
