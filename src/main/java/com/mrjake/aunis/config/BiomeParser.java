package com.mrjake.aunis.config;

import net.minecraft.world.biome.BiomeGenBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BiomeParser {

	/**
	 * Parses array of configured biomes.
	 *
	 * @param config Array of single lines containing biome definitions.
	 * @return List of {@link BiomeGenBase}s or empty list.
	 */
	@Nonnull
	static List<BiomeGenBase> parseConfig(String[] config) {
		List<BiomeGenBase> list = new ArrayList<>();

		for (String line : config) {
			BiomeGenBase biome = getBiomeFromString(line);

			if(biome != null) {
				list.add(biome);
			}
		}

		return list;
	}

	/**
	 * Parses single line of the config.
	 *
	 * @param line Consists of modid:biomename
	 * @return {@link IBlockState} when valid biome, {@code null} otherwise.
	 */
	@Nullable
    static BiomeGenBase getBiomeFromString(String line) {
        String[] parts = line.trim().split(":", 2);
        return BiomeGenBase.getBiome(1);
    }
}
