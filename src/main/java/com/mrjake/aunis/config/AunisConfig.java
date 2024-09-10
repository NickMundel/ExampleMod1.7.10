package com.mrjake.aunis.config;

import com.mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import com.mrjake.aunis.util.ItemMetaPair;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AunisConfig {
    public static StargateSizeEnum stargateSize = StargateSizeEnum.SMALL;

    public static StargateConfig stargateConfig = new StargateConfig();

    public static DHDConfig dhdConfig = new DHDConfig();

    public static RingsConfig ringsConfig = new RingsConfig();

    public static PowerConfig powerConfig = new PowerConfig();

    public static DebugConfig debugConfig = new DebugConfig();

    public static MysteriousConfig mysteriousConfig = new MysteriousConfig();

    public static AutoCloseConfig autoCloseConfig = new AutoCloseConfig();

    public static BeamerConfig beamerConfig = new BeamerConfig();

    public static RecipeConfig recipeConfig = new RecipeConfig();

    public static AudioVideoConfig avConfig = new AudioVideoConfig();

    public static WorldGenConfig worldgenConfig = new WorldGenConfig();

    public static class StargateConfig {
        public int stargateOrlinMaxOpenCount = 2;
        public int universeDialerReach = 10;

        public int universeGateNearbyReach = 1024;

        public boolean disableAnimatedEventHorizon = false;

        public float frostyTemperatureThreshold = 0.1f;

        // ---------------------------------------------------------------------------------------
        // Kawoosh blocks

        public String[] kawooshInvincibleBlocks = {};

        private List<IBlockState> cachedInvincibleBlocks = null;

        public boolean canKawooshDestroyBlock(IBlockState state) {
            if (cachedInvincibleBlocks == null) {
                cachedInvincibleBlocks = BlockMetaParser.parseConfig(kawooshInvincibleBlocks);
            }

            return !cachedInvincibleBlocks.contains(state);
        }


        // ---------------------------------------------------------------------------------------
        // Jungle biomes

        public Map<String, String[]> biomeMatches = new HashMap<String, String[]>() {
            {
                put(BiomeOverlayEnum.NORMAL.toString(), new String[] {});
                put(BiomeOverlayEnum.FROST.toString(), new String[] {});
                put(BiomeOverlayEnum.MOSSY.toString(), new String[] {"minecraft:jungle", "minecraft:jungle_hills", "minecraft:jungle_edge", "minecraft:mutated_jungle", "minecraft:mutated_jungle_edge"});
                put(BiomeOverlayEnum.AGED.toString(), new String[] {});
                put(BiomeOverlayEnum.SOOTY.toString(), new String[] {"minecraft:hell"});
            }
        };

        private Map<BiomeGenBase, BiomeOverlayEnum> cachedBiomeMatchesReverse = null;

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

        public Map<BiomeGenBase, BiomeOverlayEnum> getBiomeOverrideBiomes() {
            if (cachedBiomeMatchesReverse == null) {
                genBiomeOverrideBiomeCache();
            }

            return cachedBiomeMatchesReverse;
        }


        // ---------------------------------------------------------------------------------------
        // Biome overlay override blocks

        public Map<String, String[]> biomeOverrideBlocks = new HashMap<String, String[]>() {
            {
                put(BiomeOverlayEnum.NORMAL.toString(), new String[] {"minecraft:stone"});
                put(BiomeOverlayEnum.FROST.toString(), new String[] {"minecraft:ice"});
                put(BiomeOverlayEnum.MOSSY.toString(), new String[] {"minecraft:vine"});
                put(BiomeOverlayEnum.AGED.toString(), new String[] {"minecraft:cobblestone"});
                put(BiomeOverlayEnum.SOOTY.toString(), new String[] {"minecraft:coal_block"});
            }
        };

        private Map<BiomeOverlayEnum, List<ItemMetaPair>> cachedBiomeOverrideBlocks = null;
        private Map<ItemMetaPair, BiomeOverlayEnum> cachedBiomeOverrideBlocksReverse = null;

        private void genBiomeOverrideCache() {
            cachedBiomeOverrideBlocks = new HashMap<>();
            cachedBiomeOverrideBlocksReverse = new HashMap<>();

            for (Map.Entry<String, String[]> entry : biomeOverrideBlocks.entrySet()) {
                List<ItemMetaPair> parsedList = ItemMetaParser.parseConfig(entry.getValue());
                BiomeOverlayEnum biomeOverlay = BiomeOverlayEnum.fromString(entry.getKey());

                cachedBiomeOverrideBlocks.put(biomeOverlay, parsedList);

                for (ItemMetaPair stack : parsedList) {
                    cachedBiomeOverrideBlocksReverse.put(stack, biomeOverlay);
                }
            }
        }

        public Map<BiomeOverlayEnum, List<ItemMetaPair>> getBiomeOverrideBlocks() {
            if (cachedBiomeOverrideBlocks == null) {
                genBiomeOverrideCache();
            }

            return cachedBiomeOverrideBlocks;
        }

        public Map<ItemMetaPair, BiomeOverlayEnum> getBiomeOverrideItemMetaPairs() {
            if (cachedBiomeOverrideBlocksReverse == null) {
                genBiomeOverrideCache();
            }

            return cachedBiomeOverrideBlocksReverse;
        }

    }

    public static class PowerConfig {

        public int stargateEnergyStorage = 71280000;

        public int stargateMaxEnergyTransfer = 26360;

        public int openingBlockToEnergyRatio = 4608;

        public int keepAliveBlockToEnergyRatioPerTick = 2;

        public int instabilitySeconds = 20;

        public double stargateOrlinEnergyMul = 2.0;

        public double stargateUniverseEnergyMul = 1.5;

        public int universeCapacitors = 0;
    }

    public static class RingsConfig {
        public int rangeFlat = 25;

        public int rangeVertical = 256;

        public boolean ignoreObstructionCheck = false;
    }

    public static class DHDConfig {
        public int rangeFlat = 10;

        public int rangeVertical = 5;

        public int fluidCapacity = 60000;

        public int energyPerNaquadah = 10240;

        public int powerGenerationMultiplier = 1;

        public double activationLevel = 0.4;
        public double deactivationLevel = 0.98;
    }

    public static class DebugConfig {
        public boolean checkGateMerge = true;

        public boolean renderBoundingBoxes = false;

        public boolean renderWholeKawooshBoundingBox = false;
    }

    public static class MysteriousConfig {
        public int maxOverworldCoords = 30000;

        public int minOverworldCoords = 15000;

        public double despawnDhdChance = 0.05;

        public int pageCooldown = 40;
    }

    public static class AutoCloseConfig {
        public boolean autocloseEnabled = true;

        public int secondsToAutoclose = 5;
    }

    public static class BeamerConfig {
        public int fluidCapacity = 60000;

        public int energyCapacity = 17820000;

        public int energyTransfer = 26360;

        public int fluidTransfer = 100;

        public int itemTransfer = 4;

        public int reach = 10;

        public boolean enableFluidBeamColorization = true;


        public int signalIntervalTicks = 20;
    }

    public static class RecipeConfig {

    }

    public static class AudioVideoConfig {

        public double glyphTransparency = 0.75;

        public float volume = 1;

        public float pageNarrowing = 0;
    }

    public static class WorldGenConfig {
        public boolean naquadahEnable = true;

        public int naquadahVeinSize = 8;

        public int naquadahMaxVeinInChunk = 16;
    }

    public static void resetCache() {
        stargateConfig.cachedInvincibleBlocks = null;
        stargateConfig.cachedBiomeMatchesReverse = null;
        stargateConfig.cachedBiomeOverrideBlocks = null;
        stargateConfig.cachedBiomeOverrideBlocksReverse = null;
    }
}
