package com.mrjake.aunis.block;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.AunisCreativeTab;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.IBlock;
import com.mrjake.aunis.util.blocks.BaseBlock;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class AunisBlocks {

    public static String assetKey = Aunis.MODID.toLowerCase();
    public static String blockDomain = assetKey;

    public static List<Block> registeredBlocks = new ArrayList<Block>();

    public static final Block NAQUADAH_BLOCK = newBlock("naquadah_block", NaquadahBlock.class);

    private static Block[] blocks = {
        NAQUADAH_BLOCK,
    };

    public static <BLOCK extends Block> BLOCK newBlock(String name, Class<BLOCK> cls) {
        return newBlock(name, cls, null);
    }

    public static <BLOCK extends Block> BLOCK newBlock(String name, Class<BLOCK> cls, Class itemClass) {
        BLOCK block;
        try {
            Constructor<BLOCK> ctor = cls.getConstructor();
            block = ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return addBlock(block, name, itemClass);
    }

    public static <BLOCK extends Block> BLOCK newBlockMaterial(String name, Class<BLOCK> cls, Material material) {
        return newBlockMaterial(name, cls, material, null);
    }

    public static <BLOCK extends Block> BLOCK newBlockMaterial(String name, Class<BLOCK> cls, Material material, Class itemClass) {
        BLOCK block;
        try {
            Constructor<BLOCK> ctor = cls.getConstructor(Material.class);
            block = ctor.newInstance(material);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return addBlock(block, name, itemClass);
    }

    public <BLOCK extends Block> BLOCK addBlock(BLOCK block, String name) {
        return addBlock(block, name, null);
    }

    public static <BLOCK extends Block> BLOCK addBlock(BLOCK block, String name, Class itemClass) {
        String qualName = blockDomain + ":" + name;
        block.setBlockName(qualName);
        block.setBlockTextureName(assetKey + ":" + name);
        itemClass = getItemClassForBlock(block, itemClass);
        Aunis.LOG.trace(String.format("BaseMod.addBlock: ItemClass %s", itemClass));
        GameRegistry.registerBlock(block, itemClass, name);
        Aunis.LOG.trace(String.format("BaseMod.addBlock: Setting creativeTab to %s", Aunis.aunisCreativeTab));
        block.setCreativeTab(Aunis.aunisCreativeTab);
        registeredBlocks.add(block);
        return block;
    }

    protected static Class getItemClassForBlock(Block block, Class suppliedClass) {
        Class baseClass = defaultItemClassForBlock(block);
        if (suppliedClass == null) return baseClass;

        if (!baseClass.isAssignableFrom(suppliedClass)) {
            throw new RuntimeException(
                String.format(
                    "Block item class %s for %s does not extend %s\n",
                    suppliedClass.getName(),
                    block.getUnlocalizedName(),
                    baseClass.getName()));
        }

        return suppliedClass;

    }

    protected static Class defaultItemClassForBlock(Block block) {
        if (block instanceof IBlock) return ((IBlock) block).getDefaultItemClass();
        else return ItemBlock.class;
    }
}

