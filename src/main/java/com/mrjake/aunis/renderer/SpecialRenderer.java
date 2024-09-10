package com.mrjake.aunis.renderer;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.tesr.RendererProviderInterface;
import com.mrjake.aunis.util.BaseTileEntity;
import com.mrjake.aunis.util.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class SpecialRenderer extends TileEntitySpecialRenderer<BaseTileEntity> {

    public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        try {
            ((RendererProviderInterface) te).getRenderer().render(x, y, z, partialTicks);
        }

        catch (ClassCastException e) {
            Aunis.LOG.warn("RendererProviderInterface is not implemented on " + te.getClass().getName());
        }
    }
}
