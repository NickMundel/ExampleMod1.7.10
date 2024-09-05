package com.mrjake.aunis.loader.model;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.loader.FolderLoader;
import cpw.mods.fml.common.ProgressManager;
import cpw.mods.fml.common.ProgressManager.ProgressBar;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelLoader {

    public static final String MODELS_PATH = "assets/sgcraft/models/tesr";
    private static final Map<ResourceLocation, OBJModel> LOADED_MODELS = new HashMap<>();

    public static OBJModel getModel(ResourceLocation resourceLocation) {
        return LOADED_MODELS.get(resourceLocation);
    }

    public static void reloadModels() throws IOException {
        LOADED_MODELS.clear();

        List<String> modelPaths = FolderLoader.getAllFiles(MODELS_PATH, ".obj");
        ProgressBar progressBar = ProgressManager.push("Aunis - Loading models", modelPaths.size());

        long start = System.currentTimeMillis();

        for (String modelPath : modelPaths) {
            String modelResourcePath = modelPath.replaceFirst("assets/sgcraft/", "");
            progressBar.step(modelResourcePath);
            LOADED_MODELS.put(new ResourceLocation(Aunis.MODID, modelResourcePath), OBJLoader.loadModel(modelPath));
        }

        Aunis.LOG.debug("Loaded "+modelPaths.size()+" models in "+(System.currentTimeMillis()-start)+" ms");

        ProgressManager.pop(progressBar);
    }

    public static ResourceLocation getModelResource(String model) {
        return new ResourceLocation(Aunis.MODID, "models/tesr/" + model);
    }
}
