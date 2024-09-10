package com.mrjake.aunis.util;

import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

import java.util.List;

public class BlockHelpers {

    public static boolean isBlockDirectlyUnderSky(IBlockAccess world, BlockPos pos) {
        while (pos.getY() < 255) {
            pos = pos.up();

            IBlockState state = BaseUtils.getWorldBlockState(world, pos);
            Block block = state.getBlock();

            if (!world.isAirBlock(pos.getX(), pos.getY(), pos.getZ()) && block != Blocks.leaves && block != Blocks.leaves2)
                return false;
        }

        return true;
    }

    /**
     * Returns {@link BlockPos} with largest Y-coord value.
     *
     * @param list List of positions.
     * @return largest Y-coord {@link BlockPos}. {@code null} if list empty.
     */
    public static BlockPos getHighest(List<BlockPos> list) {
        int maxy = -1;
        BlockPos top = null;

        for (BlockPos pos : list) {
            if (pos.getY() > maxy) {
                maxy = pos.getY();
                top = pos;
            }
        }

        return top;
    }
}
