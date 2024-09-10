package com.mrjake.aunis.fluid;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class MoltenFluid extends Fluid {

    public MoltenFluid(String fluidName) {
        super(fluidName);
        setDensity(6000);
        setViscosity(6000);
        setTemperature(2600);
        setLuminosity(4);
    }

}
