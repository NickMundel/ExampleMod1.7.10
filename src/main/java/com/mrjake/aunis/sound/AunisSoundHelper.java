package com.mrjake.aunis.sound;

import com.mrjake.aunis.config.AunisConfig;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.packet.SoundPositionedPlayToClient;
import com.mrjake.aunis.util.minecraft.BlockPos;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.world.World;

public class AunisSoundHelper {

	public static final SoundCategory AUNIS_SOUND_CATEGORY = SoundCategory.BLOCKS;

	public static void playPositionedSound(World world, BlockPos pos, SoundPositionedEnum soundEnum, boolean play) {
		AunisPacketHandler.INSTANCE.sendToAllAround(new SoundPositionedPlayToClient(pos, soundEnum, play), new NetworkRegistry.TargetPoint(world.provider.dimensionId, pos.getX(), pos.getY(), pos.getZ(), 512));
	}

	public static void playSoundEventClientSide(World world, BlockPos pos, SoundEventEnum soundEventEnum) {
		world.playSoundEffect(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, soundEventEnum.name, soundEventEnum.volume * AunisConfig.avConfig.volume, 1.0f);
	}

	public static void playSoundEvent(World world, BlockPos pos, SoundEventEnum soundEventEnum) {
		world.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), soundEventEnum.name, soundEventEnum.volume * AunisConfig.avConfig.volume, 1.0f);
	}

	public static void playSoundToPlayer(EntityPlayerMP player, SoundEventEnum soundEventEnum, BlockPos pos) {
		player.playerNetServerHandler.sendPacket(new S29PacketSoundEffect(soundEventEnum.name, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, soundEventEnum.volume * AunisConfig.avConfig.volume, 1.0f));
	}
}
