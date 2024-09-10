package com.mrjake.aunis.fluid;

import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class AunisBlockFluid extends BlockFluidClassic {

	public AunisBlockFluid(Fluid fluid, String name) {
		super(fluid, Material.lava);
        setBlockName(fluid.getUnlocalizedName());
	}
}
