package com.mrjake.aunis.renderer;

import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class BlockRenderer {

	/**
	 * This method renders block using provided {@link IBlockState}.
	 * Call {@code translate(x, y, z)} before this.
	 *
	 * @param world The world in which rendering takes place.
	 * @param relativePos Relative position of the rendered block.
	 * @param state {@link IBlockState}of the block to render.
	 * @param lightPos Position from which the light level will be taken.
	 */
	public static void render(World world, BlockPos relativePos, IBlockState state, BlockPos lightPos) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(relativePos.getX()-lightPos.getX()), (float)(relativePos.getY()-lightPos.getY()), (float)(relativePos.getZ()-lightPos.getZ()));

        GL11.glDisable(GL11.GL_LIGHTING);

// Render block
        RenderBlocks renderBlocks = new RenderBlocks(world);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        renderBlocks.renderBlockAllFaces(state.getBlock(), lightPos.getX(), lightPos.getY(), lightPos.getZ());
        tessellator.draw();

        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_LIGHTING);
	}
}
