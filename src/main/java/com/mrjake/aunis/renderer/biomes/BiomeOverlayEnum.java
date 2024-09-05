package com.mrjake.aunis.renderer.biomes;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.config.AunisConfig;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.TextFormatting;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.EnumSet;

public enum BiomeOverlayEnum {
	NORMAL("", TextFormatting.GRAY),
	FROST("_frost", TextFormatting.DARK_AQUA),
	MOSSY("_mossy", TextFormatting.DARK_GREEN),
	AGED("_aged", TextFormatting.GRAY),
	SOOTY("_sooty", TextFormatting.DARK_GRAY);

	public String suffix;
	private TextFormatting color;
	private String unlocalizedName;

	BiomeOverlayEnum(String suffix, TextFormatting color) {
		this.suffix = suffix;
		this.color = color;
		this.unlocalizedName = "gui.stargate.biome_overlay." + name().toLowerCase();
	}

	public String getLocalizedColorizedName() {
		return color + Aunis.proxy.localize(unlocalizedName);
	}

	/**
	 * Called every 1-2 seconds from {@link TileEntity} to update it's
	 * frosted/moss state.
	 *
	 * @param world
	 * @param topmostBlock Topmost block of the structure (Stargates should pass top chevron/ring)
	 * @param supportedOverlays will only return enums which are in this Set
	 * @return
	 */
	public static BiomeOverlayEnum updateBiomeOverlay(World world, BlockPos topmostBlock, EnumSet<BiomeOverlayEnum> supportedOverlays) {
		BiomeOverlayEnum ret = getBiomeOverlay(world, topmostBlock);

		if (supportedOverlays.contains(ret))
			return ret;

		return NORMAL;
	}

	private static BiomeOverlayEnum getBiomeOverlay(World world, BlockPos topmostBlock) {
		BiomeGenBase biome = world.getBiomeGenForCoords(topmostBlock.getX(), topmostBlock.getZ());

		// If not Nether and block not under sky
		if (world.provider.dimensionId != -1 && !world.canBlockSeeTheSky(topmostBlock.getX(), topmostBlock.getY(), topmostBlock.getZ()))
			return NORMAL;

		if (biome.getFloatTemperature(topmostBlock.getX(), topmostBlock.getY(), topmostBlock.getZ()) <= AunisConfig.stargateConfig.frostyTemperatureThreshold)
			return FROST;

		BiomeOverlayEnum overlay = AunisConfig.stargateConfig.getBiomeOverrideBiomes().get(biome);

		if (overlay != null)
			return overlay;

		return NORMAL;
	}

	public static BiomeOverlayEnum fromString(String name) {
		for (BiomeOverlayEnum biomeOverlay : values()) {
			if (biomeOverlay.toString().equals(name)) {
				return biomeOverlay;
			}
		}

		return null;
	}
}
