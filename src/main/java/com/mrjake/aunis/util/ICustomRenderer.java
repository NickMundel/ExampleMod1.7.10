package com.mrjake.aunis.util;

import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public interface ICustomRenderer {

    void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target,
                     EnumWorldBlockLayer layer, EnumFacing t);

    void renderItemStack(ItemStack stack, IRenderTarget target, EnumFacing t);
}
