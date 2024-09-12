package com.mrjake.aunis.block.stargate;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.IExtendedBlockState;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

import java.util.List;

public class StargateClassicMemberBlockRenderer implements ISimpleBlockRenderingHandler {
    private final Block defaultBlock;
    private final int renderId;

    public StargateClassicMemberBlockRenderer(Block defaultBlock, int renderId) {
        this.defaultBlock = defaultBlock;
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        renderer.renderBlockAsItem(defaultBlock, metadata, 1.0F);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        IBlockState state =BaseUtils.getWorldBlockState(world, new BlockPos(x, y, z));

        IBlockState camoBlockState = ((IExtendedBlockState) state).getValue(AunisProps.CAMO_BLOCKSTATE);

        if (camoBlockState != null && camoBlockState.getBlock() != Blocks.air && camoBlockState.getBlock() != defaultBlock) {
            try {
                renderer.renderBlockByRenderType(camoBlockState.getBlock(), x, y, z);
                return true;
            } catch (IllegalArgumentException e) {
                Aunis.LOG.error("IllegalArgumentException in StargateClassicMemberBlockRenderer: Unsupported block as camo");
            }
        }

        renderer.renderStandardBlock(defaultBlock, x, y, z);
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }
}
