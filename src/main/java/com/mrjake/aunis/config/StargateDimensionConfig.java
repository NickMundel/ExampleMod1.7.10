package com.mrjake.aunis.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.config.StargateDimensionConfigEntry;
import com.mrjake.aunis.stargate.power.StargateEnergyRequired;
import net.minecraftforge.common.DimensionManager;

public class StargateDimensionConfig {

    private static final Map<String, StargateDimensionConfigEntry> DEFAULTS_MAP = new HashMap<String, StargateDimensionConfigEntry>();

    static {
        DEFAULTS_MAP.put("overworld", new StargateDimensionConfigEntry(0, 0, "netherOv"));
        DEFAULTS_MAP.put("the_nether", new StargateDimensionConfigEntry(3686400, 1600, "netherOv"));
        DEFAULTS_MAP.put("the_end", new StargateDimensionConfigEntry(5529600, 2400, null));
        DEFAULTS_MAP.put("moon.moon", new StargateDimensionConfigEntry(7372800, 3200, null));
        DEFAULTS_MAP.put("planet.mars", new StargateDimensionConfigEntry(11059200, 4800, null));
        DEFAULTS_MAP.put("planet.venus", new StargateDimensionConfigEntry(12288000, 5334, null));
        DEFAULTS_MAP.put("planet.asteroids", new StargateDimensionConfigEntry(14745600, 6400, null));
    }

    private static File dimensionConfigFile;
    private static Map<String, StargateDimensionConfigEntry> dimensionStringMap;
    private static Map<Integer, StargateDimensionConfigEntry> dimensionMap;

    public static StargateEnergyRequired getCost(int fromDimId, int toDimId) {
        StargateDimensionConfigEntry reqFrom = dimensionMap.get(fromDimId);
        StargateDimensionConfigEntry reqTo = dimensionMap.get(toDimId);

        if (reqFrom == null || reqTo == null) {
            Aunis.LOG.error("Tried to get a cost of a non-existing dimension. This is a bug.");
            Aunis.LOG.error("FromId: {}, ToId: {}, FromEntryNull: {}, ToEntryNull: {}", fromDimId, toDimId, reqFrom == null, reqTo == null);
            Aunis.LOG.error("Aunis dimension entries:{}{}", System.lineSeparator(), dimensionMap.entrySet().stream()
                .map(en -> en.getKey() + " | " + en.getValue().toString())
                .collect(Collectors.joining(System.lineSeparator()))
            );
            return new StargateEnergyRequired(0, 0);
        }

        int energyToOpen = Math.abs(reqFrom.energyToOpen - reqTo.energyToOpen);
        int keepAlive = Math.abs(reqFrom.keepAlive - reqTo.keepAlive);

        return new StargateEnergyRequired(energyToOpen, keepAlive);
    }

    public static boolean isGroupEqual(int fromDimId, int toDimId) {
        StargateDimensionConfigEntry reqFrom = dimensionMap.get(fromDimId);
        StargateDimensionConfigEntry reqTo = dimensionMap.get(toDimId);

        if (reqFrom == null || reqTo == null) {
            Aunis.LOG.error("Tried to perform a group check for a non-existing dimension. This is a bug.");
            Aunis.LOG.error("FromId: {}, ToId: {}, FromEntryNull: {}, ToEntryNull: {}", fromDimId, toDimId, reqFrom == null, reqTo == null);
            Aunis.LOG.error("Aunis dimension entries:{}{}", System.lineSeparator(), dimensionMap.entrySet().stream()
                .map(en -> en.getKey() + " | " + en.getValue().toString())
                .collect(Collectors.joining(System.lineSeparator()))
            );
            return false;
        }

        return reqFrom.isGroupEqual(reqTo);
    }

    public static boolean netherOverworld8thSymbol() {
        return !isGroupEqual(0, -1);
    }

    public static void load(File modConfigDir) {
        dimensionMap = null;
        dimensionConfigFile = new File(modConfigDir, "aunis_dimensions.json");

        try {
            Type typeOfHashMap = new TypeToken<Map<String, StargateDimensionConfigEntry>>() { }.getType();
            dimensionStringMap = new GsonBuilder().create().fromJson(new FileReader(dimensionConfigFile), typeOfHashMap);
        }

        catch (FileNotFoundException exception) {
            dimensionStringMap = new HashMap<String, StargateDimensionConfigEntry>();
        }
    }

    public static void update() throws IOException {
        if (dimensionMap == null) {
            dimensionMap = new HashMap<Integer, StargateDimensionConfigEntry>();

            for (String dimName : dimensionStringMap.keySet()) {
                try {
                    int dimId = Integer.parseInt(dimName);
                    if (DimensionManager.isDimensionRegistered(dimId)) {
                        dimensionMap.put(dimId, dimensionStringMap.get(dimName));
                    } else {
                        Aunis.LOG.debug("Dimension not registered: " + dimId);
                    }
                } catch (NumberFormatException ex) {
                    Aunis.LOG.debug("Invalid dimension name: " + dimName);
                }
            }
        }

        int originalSize = dimensionMap.size();

        for (int dimId : DimensionManager.getIDs()) {
            if (!dimensionMap.containsKey(dimId)) {
                String dimName = DimensionManager.getProvider(dimId).getDimensionName();

                if (DEFAULTS_MAP.containsKey(dimName))
                    dimensionMap.put(dimId, DEFAULTS_MAP.get(dimName));
                else
                    dimensionMap.put(dimId, new StargateDimensionConfigEntry(0, 0, null));
            }
        }

        if (originalSize != dimensionMap.size()) {
            FileWriter writer = new FileWriter(dimensionConfigFile);

            dimensionStringMap.clear();
            for (int dimId : dimensionMap.keySet()) {
                String dimName = DimensionManager.getProvider(dimId).getDimensionName();
                dimensionStringMap.put(dimName, dimensionMap.get(dimId));
            }

            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(dimensionStringMap));
            writer.close();
        }
    }
}
