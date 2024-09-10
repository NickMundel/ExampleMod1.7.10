package com.mrjake.aunis.block.stargate;

import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractMemberTile;
import com.mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.BlockFaceShape;
import com.mrjake.aunis.util.EnumBlockRenderType;
import com.mrjake.aunis.util.blocks.BaseBlock;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class StargateAbstractMemberBlock extends BaseBlock {

    public StargateAbstractMemberBlock(String blockName) {
        super(Material.iron);

        //setUnlocalizedName(Aunis.ModID + "." + blockName);

        setHardness(3.0f);
        setHarvestLevel("pickaxe", 3);
    }

    protected abstract StargateAbstractMergeHelper getMergeHelper();


    // --------------------------------------------------------------------------------------
    // Interactions

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        StargateAbstractMemberTile memberTile = (StargateAbstractMemberTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
        StargateAbstractBaseTile gateTile = memberTile.getBaseTile(world);

        if (gateTile != null) {
            gateTile.updateMergeState(false, BaseUtils.getWorldBlockState(world, gateTile.getPos()).getValue(AunisProps.FACING_HORIZONTAL));
        }
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te) {
        super.harvestBlock(world, player, pos, state, te);
        world.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());
    }

    // --------------------------------------------------------------------------------------
    // TileEntity


    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public abstract TileEntity createTileEntity(World world, IBlockState state);


    // --------------------------------------------------------------------------------------
    // Rendering

    @Override
    public int getRenderType(IBlockState state) {
        if (state.getValue(AunisProps.RENDER_BLOCK))
            return EnumBlockRenderType.MODEL.ordinal();
        else
            return EnumBlockRenderType.ENTITYBLOCK_ANIMATED.ordinal();
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing facing) {
    	if (state.getValue(AunisProps.RENDER_BLOCK)) {
    		// Rendering some block
    		StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
			if (memberTile != null && memberTile.getCamoState() != null) {
				return BlockFaceShape.SOLID;
			}
    	}

    	return BlockFaceShape.UNDEFINED;
    }
}
