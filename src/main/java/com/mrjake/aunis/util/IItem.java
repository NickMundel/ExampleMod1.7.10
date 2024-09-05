package com.mrjake.aunis.util;

import net.minecraft.item.ItemStack;

public interface IItem extends ITextureConsumer {

    ModelSpec getModelSpec(ItemStack stack);

    int getNumSubtypes();
}
