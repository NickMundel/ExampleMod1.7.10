package com.mrjake.aunis.stargate;

import com.mrjake.aunis.config.AunisConfig;
import com.mrjake.aunis.stargate.network.StargatePos;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import com.mrjake.aunis.util.INBTSerializable;
import com.mrjake.aunis.util.minecraft.AxisAlignedBB;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.Vec3i;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public class AutoCloseManager implements INBTSerializable<NBTTagCompound> {

	private StargateAbstractBaseTile gateTile;

	private int secondsPassed;
	private int playersPassed;

	public AutoCloseManager(StargateAbstractBaseTile gateTile) {
		this.gateTile = gateTile;
	}

	public void reset() {
		secondsPassed = 0;
		playersPassed = 0;
	}

	public void playerPassing() {
		playersPassed++;
	}

	/**
	 * AutoClose update function (on server) (engaged) (receiving gate).
	 * Scan for load status of the source gate every 20 ticks (1 second).
	 * @param {@link StargatePos} of the initiating gate.
	 * @return {@code True} if the gate should be closed, false otherwise.
	 */
	public boolean shouldClose(StargatePos sourceStargatePos) {
		if (gateTile.getWorldObj().getTotalWorldTime() % 20 == 0) {
			World sourceWorld = sourceStargatePos.getWorldObj();
			BlockPos sourcePos = sourceStargatePos.gatePos;

            //TODO: ACTUALLY CHECK IF LOADED
			boolean sourceLoaded = sourceWorld.blockExists(sourcePos.getX(), sourcePos.getY(), sourcePos.getZ());

			if (playersPassed > 0) {
				if (sourceLoaded) {
                    int playerCount = 0;
					AxisAlignedBB scanBox = new AxisAlignedBB(sourcePos.add(new Vec3i(-10, -5, -10)), sourcePos.add(new Vec3i(10, 5, 10)));
                    List<Entity> players = sourceWorld.getEntitiesWithinAABB(Entity.class, scanBox.convertToMC());

                    for (Entity entity : players) {
                        if (entity instanceof EntityFishHook) continue;
                        if (!entity.isDead && entity.ridingEntity == null) {
                           playerCount++;
                        }
                    }

					if (playerCount == 0)
						secondsPassed++;
					else
						secondsPassed = 0;
				}

				else {
					secondsPassed++;
				}
			}

			if (secondsPassed >= AunisConfig.autoCloseConfig.secondsToAutoclose) {
				return true;
			}
		}

		return false;
	}


	// ------------------------------------------------------------------------
	// NBT

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();

		compound.setInteger("secondsPassed", secondsPassed);
		compound.setInteger("playersPassed", playersPassed);

		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		if (compound == null)
			return;

		secondsPassed = compound.getInteger("secondsPassed");
		playersPassed = compound.getInteger("playersPassed");
	}
}
