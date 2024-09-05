package com.mrjake.aunis.proxy;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public interface IProxy {
	public void preInit(FMLPreInitializationEvent event);
	public void init(FMLInitializationEvent event);
	public void postInit(FMLPostInitializationEvent event);

	public String localize(String unlocalized, Object... args);

	public EntityPlayer getPlayerInMessageHandler(MessageContext ctx);
	public void setTileEntityItemStackRenderer(Item item);
	public EntityPlayer getPlayerClientSide();
	public void addScheduledTaskClientSide(Runnable runnable);

    //TODO:FIX
	//public void orlinRendererSpawnParticles(World world, StargateAbstractRendererState rendererState);
	//public void playPositionedSoundClientSide(BlockPos pos, SoundPositionedEnum soundEnum, boolean play);

	public void openGui(GuiScreen gui);
}
