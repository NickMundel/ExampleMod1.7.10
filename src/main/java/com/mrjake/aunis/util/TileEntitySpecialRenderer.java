package com.mrjake.aunis.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class TileEntitySpecialRenderer<T extends BaseTileEntity>
{
    protected static final ResourceLocation[] DESTROY_STAGES = new ResourceLocation[] {new ResourceLocation("textures/blocks/destroy_stage_0.png"), new ResourceLocation("textures/blocks/destroy_stage_1.png"), new ResourceLocation("textures/blocks/destroy_stage_2.png"), new ResourceLocation("textures/blocks/destroy_stage_3.png"), new ResourceLocation("textures/blocks/destroy_stage_4.png"), new ResourceLocation("textures/blocks/destroy_stage_5.png"), new ResourceLocation("textures/blocks/destroy_stage_6.png"), new ResourceLocation("textures/blocks/destroy_stage_7.png"), new ResourceLocation("textures/blocks/destroy_stage_8.png"), new ResourceLocation("textures/blocks/destroy_stage_9.png")};
    protected TileEntityRendererDispatcher rendererDispatcher;

    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {}

    protected void setLightmapDisabled(boolean disabled)
    {
        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE1_ARB);

        if (disabled)
        {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
        }
        else
        {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE0_ARB);
    }

    protected void bindTexture(ResourceLocation location)
    {
        TextureManager texturemanager = this.rendererDispatcher.field_147553_e;

        if (texturemanager != null)
        {
            texturemanager.bindTexture(location);
        }
    }

    protected World getWorldObj()
    {
        return this.rendererDispatcher.field_147550_f;
    }

    public void setRendererDispatcher(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        this.rendererDispatcher = rendererDispatcherIn;
    }

    public FontRenderer getFontRenderer()
    {
        return this.rendererDispatcher.getFontRenderer();
    }

    public boolean isGlobalRenderer(T te)
    {
        return false;
    }

    protected void drawNameplate(T te, String str, double x, double y, double z, int maxDistance)
    {
        Entity entity = this.rendererDispatcher.field_147551_g;
        double d0 = BaseUtils.tegetDistanceSq(te, entity.posX, entity.posY, entity.posZ);

        if (d0 <= (double)(maxDistance * maxDistance))
        {
            float f = this.rendererDispatcher.field_147562_h;
            float f1 = this.rendererDispatcher.field_147563_i;
            boolean flag = false;

            FontRenderer fontRendererIn = this.getFontRenderer();
            x = (float)x + 0.5F;
            y = (float)y + 1.5F;
            z = (float)z + 0.5F;
            int verticalShift = 0;
            float viewerYaw = f;
            float viewerPitch = f1;
            boolean isThirdPersonFrontal = false;
            boolean isSneaking = false;

            GL11.glPushMatrix();
            GL11.glTranslatef((float) x, (float) y, (float) z);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-viewerYaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef((float)(isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-0.025F, -0.025F, 0.025F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDepthMask(false);

            if (!isSneaking)
            {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            int i = fontRendererIn.getStringWidth(str) / 2;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
            tessellator.addVertex((double)(-i - 1), (double)(-1 + verticalShift), 0.0D);
            tessellator.addVertex((double)(-i - 1), (double)(8 + verticalShift), 0.0D);
            tessellator.addVertex((double)(i + 1), (double)(8 + verticalShift), 0.0D);
            tessellator.addVertex((double)(i + 1), (double)(-1 + verticalShift), 0.0D);
            tessellator.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            if (!isSneaking)
            {
                fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, 553648127);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }

            GL11.glDepthMask(true);
            fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, isSneaking ? 553648127 : -1);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();

        }
    }
}
