package com.mrjake.aunis.tileentity.stargate;

import com.mrjake.aunis.util.BaseTileEntity;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class StargateAbstractMemberTile extends BaseTileEntity {

    // ---------------------------------------------------------------------------------
    // Base position

    protected BlockPos basePos;

    public boolean isMerged() {
        return basePos != null;
    }

    @Nullable
    public BlockPos getBasePos() {
        return basePos;
    }

    @Nullable
    public StargateAbstractBaseTile getBaseTile(World world) {
        if (basePos != null)
            return (StargateAbstractBaseTile) world.getTileEntity(basePos.getX(), basePos.getY(), basePos.getZ());

        return null;
    }

    public void setBasePos(BlockPos basePos) {
        this.basePos = basePos;

        markDirty();
    }


    // ---------------------------------------------------------------------------------
    // NBT

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        if (basePos != null)
            compound.setLong("basePos", basePos.toLong());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("basePos"))
            basePos = BlockPos.fromLong(compound.getLong("basePos"));

        super.readFromNBT(compound);
    }

    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
