package com.mrjake.aunis.util;

import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.world.IBlockAccess;

public interface IBlock extends ITextureConsumer {

    void setRenderType(int id);

    ICustomRenderer getCustomRenderer();

    ModelSpec getModelSpec(IBlockState state);

    Class getDefaultItemClass();
}
