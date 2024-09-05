package com.mrjake.aunis.proxy;

import com.mrjake.aunis.loader.ReloadListener;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;


public class ProxyClient implements IProxy {
	public void preInit(FMLPreInitializationEvent event) {
		registerRenderers();
		registerFluidRenderers();

		//InputHandlerClient.registerKeybindings();
	}

	public void init(FMLInitializationEvent event) {
		//Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new PageMysteriousItemColor(), AunisItems.PAGE_MYSTERIOUS_ITEM);
    	//Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new PageNotebookItemColor(), AunisItems.PAGE_NOTEBOOK_ITEM);

    	//Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new StargateClassicMemberBlockColor(),
    	//		AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK,
    	//		AunisBlocks.STARGATE_UNIVERSE_MEMBER_BLOCK);
    }

    public void postInit(FMLPostInitializationEvent event) {
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ReloadListener());
    }

	public String localize(String unlocalized, Object... args) {
		return I18n.format(unlocalized, args);
	}

	private void registerRenderers() {
		//OBJLoader.INSTANCE.addDomain("aunis");

		//SpecialRenderer specialRenderer = new SpecialRenderer();

		//ClientRegistry.bindTileEntitySpecialRenderer(StargateMilkyWayBaseTile.class, new StargateMilkyWayRenderer());
		//ClientRegistry.bindTileEntitySpecialRenderer(StargateUniverseBaseTile.class, new StargateUniverseRenderer());
		//ClientRegistry.bindTileEntitySpecialRenderer(StargateOrlinBaseTile.class, new StargateOrlinRenderer());
		//ClientRegistry.bindTileEntitySpecialRenderer(DHDTile.class, new DHDRenderer());

		//ClientRegistry.bindTileEntitySpecialRenderer(TransportRingsTile.class, specialRenderer);
		//ClientRegistry.bindTileEntitySpecialRenderer(TRControllerTile.class, specialRenderer);

		//ClientRegistry.bindTileEntitySpecialRenderer(BeamerTile.class, new BeamerRenderer());
	}


    private void registerFluidRenderers() {
		//for (AunisBlockFluid blockFluid : AunisFluids.blockFluidMap.values()) {
		//	ModelLoader.setCustomStateMapper(blockFluid, new StateMap.Builder().ignore(AunisBlockFluid.LEVEL).build());
		//}
	}

	@Override
	public EntityPlayer getPlayerInMessageHandler(MessageContext ctx) {
		return Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public void setTileEntityItemStackRenderer(Item item) {
		//item.setTileEntityItemStackRenderer(((CustomModelItemInterface) item).createTEISR());
	}

	@Override
	public EntityPlayer getPlayerClientSide() {
		return Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public void addScheduledTaskClientSide(Runnable runnable) {
		Minecraft.getMinecraft().func_152344_a(runnable);
	}

	//@Override
	//public void orlinRendererSpawnParticles(World world, StargateAbstractRendererState rendererState) {
	//	StargateOrlinRenderer.spawnParticles(world, rendererState);
	//}

	//@Override
	//public void playPositionedSoundClientSide(BlockPos pos, SoundPositionedEnum soundEnum, boolean play) {
	//	AunisSoundHelperClient.playPositionedSoundClientSide(pos, soundEnum, play);
	//}

	@Override
	public void openGui(GuiScreen gui) {
		Minecraft.getMinecraft().displayGuiScreen(gui);
	}
}
