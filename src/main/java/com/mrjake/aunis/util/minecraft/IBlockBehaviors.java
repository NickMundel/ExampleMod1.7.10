package com.mrjake.aunis.util.minecraft;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public interface IBlockBehaviors
{
    boolean onBlockEventReceived(World worldIn, BlockPos pos, int id, int param);

    void neighborChanged(World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos);
}
