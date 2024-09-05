// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base for 1.7 Version B - Block Utilities
//
// ------------------------------------------------------------------------------------------------

package com.mrjake.aunis.util;

import com.mrjake.aunis.util.blockstates.MetaBlockState;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BaseBlockUtils {

    public static int getMetaFromItemStack(ItemStack stack) {
        return getMetaFromBlockState(getBlockStateFromItemStack(stack));
    }

    public static IBlockState getBlockStateFromItemStack(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        int meta = 0;
        //if (stack.getItem().getHasSubtypes()) meta = stack.getItem().getMetadata(stack.getItemDamage());
        //if (block instanceof BaseBlock) return ((BaseBlock) block).getStateFromMeta(meta);
        //else return new MetaBlockState(block, meta);
        return new MetaBlockState(block, meta);
    }

    public static IBlockState getStateFromMeta(Block block, int meta) {
        //if (block instanceof BaseBlock) return ((BaseBlock) block).getStateFromMeta(meta);
        //else return new MetaBlockState(block, meta);
        return new MetaBlockState(block, meta);
    }

    public static int getMetaFromBlockState(IBlockState state) {
        //if (state instanceof MetaBlockState) return ((MetaBlockState) state).meta;
        //else return ((BaseBlock) state.getBlock()).getMetaFromState(state);
        return ((MetaBlockState) state).meta;
    }

    public static Block getWorldBlock(IBlockAccess world, BlockPos pos) {
        return world.getBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public static IBlockState getWorldBlockState(IBlockAccess world, BlockPos pos) {
        Block block = world.getBlock(pos.getX(), pos.getY(), pos.getZ());
        int meta = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());
        //if (block instanceof BaseBlock) return ((BaseBlock) block).getStateFromMeta(meta);
        //else return new MetaBlockState(block, meta);
        return new MetaBlockState(block, meta);
    }

    public static IBlockState getWorldBlockState(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        //if (block instanceof BaseBlock) return ((BaseBlock) block).getStateFromMeta(meta);
        //else return new MetaBlockState(block, meta);
        return new MetaBlockState(block, meta);
    }

    public static void setWorldBlockState(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        int meta = getMetaFromBlockState(state);
        world.setBlock(pos.getX(), pos.getY(), pos.getZ(), block, meta, 3);
    }

    public static void markWorldBlockForUpdate(World world, BlockPos pos) {
        world.markBlockForUpdate(pos.getX(), pos.getY(), pos.getZ());
    }

    public static void notifyWorldNeighborsOfStateChange(World world, BlockPos pos, Block block) {
        world.notifyBlocksOfNeighborChange(pos.getX(), pos.getY(), pos.getZ(), block);
    }

    public static TileEntity getWorldTileEntity(IBlockAccess world, BlockPos pos) {
        return world.getTileEntity(pos.getX(), pos.getZ(), pos.getZ());
    }

    public static World getTileEntityWorld(TileEntity te) {
        return te.getWorldObj();
    }

    public static BlockPos getTileEntityPos(TileEntity te) {
        return new BlockPos(te.xCoord, te.yCoord, te.zCoord);
    }

    //TODO: FIX
    //public static boolean blockCanRenderInLayer(Block block, EnumWorldBlockLayer layer) {
    //    if (block instanceof BaseBlock) return ((BaseBlock) block).canRenderInLayer(layer);
    //    else switch (layer) {
    //        case SOLID:
    //            return block.canRenderInPass(0);
    //        case TRANSLUCENT:
    //            return block.canRenderInPass(1);
    //        default:
    //            return false;
    //    }
    //}
}
