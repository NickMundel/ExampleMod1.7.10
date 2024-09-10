package com.mrjake.aunis.proxy;

import com.mrjake.aunis.sound.SoundPositionedEnum;
import com.mrjake.aunis.util.minecraft.BlockPos;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class ProxyServer implements IProxy {
	public void preInit(FMLPreInitializationEvent event) {

	}

    public void init(FMLInitializationEvent event) {

    }

    public void postInit(FMLPostInitializationEvent event) {

    }

	public String localize(String unlocalized, Object... args) {
		//TODO: Fix translation
        //return I18n.translateToLocalFormatted(unlocalized, args);
        return unlocalized;
	}

	@Override
	public EntityPlayer getPlayerInMessageHandler(MessageContext ctx) {
		return ctx.getServerHandler().playerEntity;
	}

	@Override
	public void setTileEntityItemStackRenderer(Item item) {

	}

	@Override
	public EntityPlayer getPlayerClientSide() {
		return null;
	}

	@Override
	public void addScheduledTaskClientSide(Runnable runnable) {}

    //TODO: Fix
	//@Override
	//public void orlinRendererSpawnParticles(World world, StargateAbstractRendererState rendererState) {}

	@Override
	public void playPositionedSoundClientSide(BlockPos pos, SoundPositionedEnum soundEnum, boolean play) {}

	@Override
	public void openGui(GuiScreen gui) {}
}
