package com.mrjake.aunis.property;


import com.mrjake.aunis.util.IUnlistedProperty;
import com.mrjake.aunis.util.minecraft.IBlockState;

public class PropertyUnlistedCamoBlockState implements IUnlistedProperty<IBlockState> {

	@Override
	public String getName() {
		return "camoblockstate";
	}

	@Override
	public boolean isValid(IBlockState value) {
		return true;
	}

	@Override
	public Class<IBlockState> getType() {
		return IBlockState.class;
	}

	@Override
	public String valueToString(IBlockState value) {
		return value.toString();
	}

}
