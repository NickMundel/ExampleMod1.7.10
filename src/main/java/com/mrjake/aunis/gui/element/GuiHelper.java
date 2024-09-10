package com.mrjake.aunis.gui.element;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.lwjgl.opengl.GL11;

public class GuiHelper {
	public static void drawTexturedRectScaled(int xLeftCoord, int yBottomCoord, TextureAtlasSprite textureSprite, int maxWidth, int maxHeight, float scaleHeight) {
		maxHeight *= scaleHeight;
		yBottomCoord -= maxHeight;

		drawTexturedRect(xLeftCoord, yBottomCoord, textureSprite, maxWidth, maxHeight, scaleHeight);
	}

	public static void drawTexturedRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int maxWidth, int maxHeight, float scaleHeight) {
		double v = textureSprite.getMaxV() - textureSprite.getMinV();
		v *= (1-scaleHeight);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(xCoord + 0), (double)(yCoord + maxHeight), 0, (double)textureSprite.getMinU(), (double)textureSprite.getMaxV());
        tessellator.addVertexWithUV((double)(xCoord + maxWidth), (double)(yCoord + maxHeight), 0, (double)textureSprite.getMaxU(), (double)textureSprite.getMaxV());
        tessellator.addVertexWithUV((double)(xCoord + maxWidth), (double)(yCoord + 0), 0, (double)textureSprite.getMaxU(), (double)textureSprite.getMinV()+v);
        tessellator.addVertexWithUV((double)(xCoord + 0), (double)(yCoord + 0), 0, (double)textureSprite.getMinU(), (double)textureSprite.getMinV()+v);
        tessellator.draw();
    }

    public static void drawTexturedModalRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn, float zLevel)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(xCoord + 0), (double)(yCoord + heightIn), (double)zLevel, (double)textureSprite.getMinU(), (double)textureSprite.getMaxV());
        tessellator.addVertexWithUV((double)(xCoord + widthIn), (double)(yCoord + heightIn), (double)zLevel, (double)textureSprite.getMaxU(), (double)textureSprite.getMaxV());
        tessellator.addVertexWithUV((double)(xCoord + widthIn), (double)(yCoord + 0), (double)zLevel, (double)textureSprite.getMaxU(), (double)textureSprite.getMinV());
        tessellator.addVertexWithUV((double)(xCoord + 0), (double)(yCoord + 0), (double)zLevel, (double)textureSprite.getMinU(), (double)textureSprite.getMinV());
        tessellator.draw();
    }

	public static void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + height), 0, (double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F));
        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), 0, (double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F));
        tessellator.addVertexWithUV((double)(x + width), (double)(y + 0), 0, (double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F));
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), 0, (double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F));
        tessellator.draw();
    }

	public static boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }

	public static void drawTexturedRectWithShadow(int x, int y, int xOffset, int yOffset, int xSize, int ySize, float color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(color, color, color, 1.0f);
        Gui.func_146110_a(x, y, 0, 0, xSize, ySize, xSize, ySize);

        GL11.glColor4f(color, color, color, 0.2f);
        Gui.func_146110_a(x+xOffset, y+yOffset, 0, 0, xSize, ySize, xSize, ySize);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	}
}
