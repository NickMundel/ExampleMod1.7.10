package com.mrjake.aunis;

import com.mrjake.aunis.proxy.IProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Aunis.MODID, version = Tags.VERSION, name = "Aunis", acceptedMinecraftVersions = "[1.7.10]")
public class Aunis {

    public static final String MODID = "aunis";
    public static final Logger LOG = LogManager.getLogger(MODID);

    public static final String CLIENT = "com.mrjake.aunis.proxy.ProxyClient";
    public static final String SERVER = "com.mrjake.aunis.proxy.ProxyServer";

    @SidedProxy(clientSide = Aunis.CLIENT, serverSide = Aunis.SERVER)
    public static IProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        //proxy.serverStarting(event);
    }
}
