package com.mrjake.aunis.block;

import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.util.EnumBlockRenderType;
import com.mrjake.aunis.util.blocks.BaseBlock;
import com.mrjake.aunis.util.blockstates.BlockState;
import com.mrjake.aunis.util.minecraft.AxisAlignedBB;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class InvisibleBlock extends BaseBlock {

	public InvisibleBlock() {
		super(Material.air);

		//setUnlocalizedName(Aunis.ModID + "." + blockName);

		setDefaultState(blockState.getBaseState().withProperty(AunisProps.HAS_COLLISIONS, true));

		setLightLevel(1.0f);
		setLightOpacity(0);
	}

	// ------------------------------------------------------------------------
	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, AunisProps.HAS_COLLISIONS);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(AunisProps.HAS_COLLISIONS) ? 0x01 : 0x00;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(AunisProps.HAS_COLLISIONS, (meta & 0x01) == 1);
	}

	// ------------------------------------------------------------------------
	@Override
	public int getRenderType() {
		return 0;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}


	// ------------------------------------------------------------------------

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(AunisProps.HAS_COLLISIONS))
        	return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        else
        	return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    	if (state.getValue(AunisProps.HAS_COLLISIONS))
        	return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        else
        	return null;
    }
}
