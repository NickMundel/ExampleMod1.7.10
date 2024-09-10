package com.mrjake.aunis.util;

import com.google.common.base.Predicate;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.block.Block;

import javax.annotation.Nullable;

public class BlockMatcher implements Predicate<IBlockState>
{
    private final Block block;

    private BlockMatcher(Block blockType)
    {
        this.block = blockType;
    }

    public static BlockMatcher forBlock(Block blockType)
    {
        return new BlockMatcher(blockType);
    }

    public boolean apply(@Nullable IBlockState p_apply_1_)
    {
        return p_apply_1_ != null && p_apply_1_.getBlock() == this.block;
    }
}
