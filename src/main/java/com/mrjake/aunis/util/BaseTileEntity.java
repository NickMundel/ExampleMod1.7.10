package com.mrjake.aunis.util;

import com.mrjake.aunis.util.minecraft.BlockPos;
import net.minecraft.tileentity.TileEntity;

public class BaseTileEntity extends TileEntity {

    public BlockPos pos = getPos();
    public BaseTileEntity() {
        super();
    }

    public BlockPos getPos() {
        return new BlockPos(this.xCoord, this.yCoord, this.zCoord);
    }
}
