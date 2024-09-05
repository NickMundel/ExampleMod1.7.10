package com.mrjake.aunis.block;

import com.mrjake.aunis.util.blocks.BaseBlock;
import net.minecraft.block.material.Material;

public class NaquadahBlock extends BaseBlock {

    public NaquadahBlock() {
        super(Material.rock);
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
    }

}
