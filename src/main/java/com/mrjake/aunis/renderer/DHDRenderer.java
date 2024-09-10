package com.mrjake.aunis.renderer;

import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.loader.ElementEnum;
import com.mrjake.aunis.loader.model.ModelLoader;
import com.mrjake.aunis.stargate.network.SymbolMilkyWayEnum;
import com.mrjake.aunis.tileentity.DHDTile;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.TileEntitySpecialRenderer;
import com.mrjake.aunis.util.minecraft.BlockPos;
import net.minecraft.init.Blocks;
import org.lwjgl.opengl.GL11;


public class DHDRenderer extends TileEntitySpecialRenderer<DHDTile> {

	private static final BlockPos ZERO_BLOCKPOS = new BlockPos(0, 0, 0);

	@Override
	public void render(DHDTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		DHDRendererState rendererState = te.getRendererStateClient();

		if (rendererState != null) {
			GL11.glPushMatrix();
			GL11.glTranslated(x, y, z);

			if (BaseUtils.getWorldBlockState(te.getWorldObj(), te.getPos()).getValue(AunisProps.SNOWY)) {
				//TODO: FIX SNOW RENDER
                //BlockRenderer.render(getWorldObj(), ZERO_BLOCKPOS, Blocks.snow_layer, te.getPos());
			}

			GL11.glTranslated(0.5, 0, 0.5);
            GL11.glRotated(rendererState.horizontalRotation, 0, 1, 0);

			ElementEnum.MILKYWAY_DHD.bindTextureAndRender(rendererState.getBiomeOverlay());

			for (SymbolMilkyWayEnum symbol : SymbolMilkyWayEnum.values()) {
				rendererDispatcher.field_147553_e.bindTexture(rendererState.getButtonTexture(symbol, rendererState.getBiomeOverlay()));
				ModelLoader.getModel(symbol.modelResource).render();
			}


			GL11.glPopMatrix();

			rendererState.iterate(getWorldObj(), partialTicks);
		}
	}
}
