package com.mrjake.aunis.loader;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.loader.model.ModelLoader;
import com.mrjake.aunis.loader.texture.TextureLoader;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

import java.io.IOException;

public class ReloadListener implements IResourceManagerReloadListener {

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
    	try {
				ModelLoader.reloadModels();
				TextureLoader.reloadTextures(resourceManager);
    	}

    	catch (IOException e) {
    		Aunis.LOG.error("Failed reloading resources");
    		e.printStackTrace();
    	}
	}

}
