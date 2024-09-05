package com.mrjake.aunis.loader.texture;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.loader.FolderLoader;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureLoader {

    public static final String TEXTURES_PATH = "assets/sgcraft/textures/tesr";
    private static final Map<ResourceLocation, Texture> LOADED_TEXTURES = new HashMap<>();

    public static Texture getTexture(ResourceLocation resourceLocation) {
        return LOADED_TEXTURES.get(resourceLocation);
    }

    /**
     * Checks if the texture is loaded. If not, it probably doesn't exist.
     *
     * @return True if the texture exists and it's loaded, False otherwise.
     */
    public static boolean isTextureLoaded(ResourceLocation resourceLocation) {
        return LOADED_TEXTURES.containsKey(resourceLocation);
    }

    public static void reloadTextures(IResourceManager resourceManager) throws IOException {
        for (Texture texture : LOADED_TEXTURES.values())
            texture.deleteTexture();

        List<String> texturePaths = FolderLoader.getAllFiles(TEXTURES_PATH, ".png", ".jpg");

        long start = System.currentTimeMillis();

        for (String texturePath : texturePaths) {
            texturePath = texturePath.replaceFirst("assets/sgcraft/", "");

            if (texturePath.equals("textures/tesr/event_horizon_animated.jpg"))
                continue;

            ResourceLocation resourceLocation = new ResourceLocation(Aunis.MODID, texturePath);
            IResource resource = null;

            try {
                resource = resourceManager.getResource(resourceLocation);
                InputStream inputStream = resource.getInputStream();
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                LOADED_TEXTURES.put(resourceLocation, new Texture(bufferedImage, false));

                if (texturePath.equals("textures/tesr/event_horizon_animated.jpg")) {
                    LOADED_TEXTURES.put(new ResourceLocation(Aunis.MODID, texturePath+"_desaturated"), new Texture(bufferedImage, true));
                }
            }

            catch (IOException e) {
                Aunis.LOG.error("Failed to load texture " + texturePath);
                e.printStackTrace();
            }

            finally {
                if (resource != null) {
                    try {
                        resource.getInputStream().close();
                    } catch (IOException e) {
                        // Fehlerbehandlung, falls erforderlich
                    }
                }
            }
        }

        Aunis.LOG.debug("Loaded "+texturePaths.size()+" textures in "+(System.currentTimeMillis()-start)+" ms");
    }

    public static ResourceLocation getTextureResource(String texture) {
        if (texture.contains(":")) return new ResourceLocation(texture);
        return new ResourceLocation(Aunis.MODID, "textures/tesr/" + texture);
    }
}

