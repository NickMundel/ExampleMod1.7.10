package com.mrjake.aunis;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.mrjake.aunis.block.AunisBlocks;

public class AunisCreativeTab extends CreativeTabs {

	public AunisCreativeTab() {
		super(Aunis.MODID);
	}

    @Override
    public Item getTabIconItem() {
        return Item.getItemFromBlock(AunisBlocks.NAQUADAH_BLOCK);
    }
}
