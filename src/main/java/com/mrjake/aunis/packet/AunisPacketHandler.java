package com.mrjake.aunis.packet;

import com.mrjake.aunis.packet.stargate.DHDButtonClickedToServer;
import com.mrjake.aunis.packet.stargate.StargateMotionToClient;
import com.mrjake.aunis.packet.stargate.StargateMotionToServer;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class AunisPacketHandler {
	public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("aunis");

	private static int id = 0;

	public static void registerPackets() {
		INSTANCE.registerMessage(DHDButtonClickedToServer.DHDButtonClickedServerHandler.class, DHDButtonClickedToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(StargateMotionToServer.MotionServerHandler.class, StargateMotionToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(StateUpdateRequestToServer.StateUpdateServerHandler.class, StateUpdateRequestToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(SaveRingsParametersServerHandler.class, SaveRingsParametersToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(TRControllerActivatedServerHandler.class, TRControllerActivatedToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(SetOpenTabToServer.SetOpenTabServerHandler.class, SetOpenTabToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(UniverseDialerActionPacketServerHandler.class, UniverseDialerActionPacketToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(UniverseDialerOCProgramServerHandler.class, UniverseDialerOCProgramToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(BeamerChangeRoleServerHandler.class, BeamerChangeRoleToServer.class, id, Side.SERVER); id++;
		INSTANCE.registerMessage(ChangeRedstoneModeToServer.ChangeRedstoneModeServerHandler.class, ChangeRedstoneModeToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(BeamerChangedLevelsServerHandler.class, BeamerChangedLevelsToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(BeamerChangedInactivityServerHandler.class, BeamerChangedInactivityToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(NotebookActionPacketServerHandler.class, NotebookActionPacketToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(PageNotebookSetNameServerHandler.class, PageNotebookSetNameToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(EntryActionToServer.EntryActionServerHandler.class, EntryActionToServer.class, id, Side.SERVER); id++;
		//INSTANCE.registerMessage(OCActionServerHandler.class, OCActionToServer.class, id, Side.SERVER); id++;


		INSTANCE.registerMessage(StargateMotionToClient.RetrieveMotionClientHandler.class, StargateMotionToClient.class, id, Side.CLIENT); id++;
		//INSTANCE.registerMessage(StartPlayerFadeOutToClientHandler.class, StartPlayerFadeOutToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(StateUpdatePacketToClient.StateUpdateClientHandler.class, StateUpdatePacketToClient.class, id, Side.CLIENT); id++;
		INSTANCE.registerMessage(SoundPositionedPlayToClient.PlayPositionedSoundClientHandler.class, SoundPositionedPlayToClient.class, id, Side.CLIENT); id++;
	}
}
