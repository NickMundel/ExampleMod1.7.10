package com.mrjake.aunis.tesr;

import com.mrjake.aunis.renderer.SpecialRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/**
 * Implement this on a {@link TileEntity} to use it with {@link SpecialRenderer}, a universal {@link TileEntitySpecialRenderer}.
 *
 * @author MrJake222
 */
public interface RendererProviderInterface {

	/**
	 * Get the renderer class for this {@link RendererProviderInterface}.
	 *
	 * @return {@link RendererInterface} instance.
	 */
	abstract RendererInterface getRenderer();

}
