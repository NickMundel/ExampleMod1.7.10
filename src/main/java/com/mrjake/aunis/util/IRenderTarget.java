package com.mrjake.aunis.util;

import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.Vec3d;

public interface IRenderTarget {

    boolean isRenderingBreakEffects();

    void setTexture(ITexture texture);

    void setNormal(Vec3d n);

    void beginTriangle();

    void addVertex(Vec3d p, double u, double v);

    void addProjectedVertex(Vec3d p, EnumFacing face);

    void endFace();
}

