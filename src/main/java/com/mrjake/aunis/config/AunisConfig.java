package com.mrjake.aunis.config;

import com.mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AunisConfig {
    public static StargateSizeEnum stargateSize = StargateSizeEnum.SMALL;


    public static StargateConfig stargateConfig = new StargateConfig();

    public static class StargateConfig {
        public float frostyTemperatureThreshold = 0.1f;

        private Map<BiomeGenBase, BiomeOverlayEnum> cachedBiomeMatchesReverse = null;

        public Map<BiomeGenBase, BiomeOverlayEnum> getBiomeOverrideBiomes() {
            if (cachedBiomeMatchesReverse == null) {
                genBiomeOverrideBiomeCache();
            }

            return cachedBiomeMatchesReverse;
        }

        private void genBiomeOverrideBiomeCache() {
            cachedBiomeMatchesReverse = new HashMap<>();

            for (Map.Entry<String, String[]> entry : biomeMatches.entrySet()) {
                List<BiomeGenBase> parsedList = BiomeParser.parseConfig(entry.getValue());
                BiomeOverlayEnum biomeOverlay = BiomeOverlayEnum.fromString(entry.getKey());

                for (BiomeGenBase biome : parsedList) {
                    cachedBiomeMatchesReverse.put(biome, biomeOverlay);
                }
            }
        }

        public Map<String, String[]> biomeMatches = new HashMap<String, String[]>() {
            {
                put(BiomeOverlayEnum.NORMAL.toString(), new String[] {});
                put(BiomeOverlayEnum.FROST.toString(), new String[] {});
                put(BiomeOverlayEnum.MOSSY.toString(), new String[] {"minecraft:jungle", "minecraft:jungle_hills", "minecraft:jungle_edge", "minecraft:mutated_jungle", "minecraft:mutated_jungle_edge"});
                put(BiomeOverlayEnum.AGED.toString(), new String[] {});
                put(BiomeOverlayEnum.SOOTY.toString(), new String[] {"minecraft:hell"});
            }
        };

    }

}
