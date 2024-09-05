package com.mrjake.aunis.util;

import com.mrjake.aunis.util.minecraft.BlockPos;

public class ModelSpec {

    public String modelName;
    public String[] textureNames;
    public BlockPos origin;

    public ModelSpec(String model, String... textures) {
        this(model, BlockPos.ORIGIN, textures);
    }

    public ModelSpec(String model, BlockPos origin, String... textures) {
        modelName = model;
        textureNames = textures;
        this.origin = origin;
    }
}
