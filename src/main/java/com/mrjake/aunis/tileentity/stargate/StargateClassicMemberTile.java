package com.mrjake.aunis.tileentity.stargate;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.packet.StateUpdatePacketToClient;
import com.mrjake.aunis.packet.StateUpdateRequestToServer;
import com.mrjake.aunis.stargate.EnumMemberVariant;
import com.mrjake.aunis.state.*;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import com.mrjake.aunis.util.minecraft.Rotation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.EnumSkyBlock;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * TileEntity for ring blocks and chevron blocks
 *
 * Holds the camouflage block in {@link ItemStackHandler}, providing it into {@link Block#getExtendedState()}
 *
 * @author MrJake
 */
public abstract class StargateClassicMemberTile extends StargateAbstractMemberTile implements StateProviderInterface {

	private TargetPoint targetPoint;

	@Override
	public void onLoad() {
		if (!worldObj.isRemote) {
			targetPoint = new TargetPoint(worldObj.provider.dimensionId, pos.getX(), pos.getY(), pos.getZ(), 512);
		}

		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.CAMO_STATE));
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.LIGHT_STATE));
		}
	}

	@Override
	public void rotate(Rotation rotation) {
		IBlockState state = BaseUtils.getWorldBlockState(worldObj, pos);

		EnumFacing facing = state.getValue(AunisProps.FACING_HORIZONTAL);
        BaseUtils.setWorldBlockState(worldObj, pos, state.withProperty(AunisProps.FACING_HORIZONTAL, rotation.rotate(facing)));
	}

	// ---------------------------------------------------------------------------------

	/**
	 * Is chevron block emitting light
	 */
	private boolean isLitUp;

	public void setLitUp(boolean isLitUp) {
		boolean sync = isLitUp != this.isLitUp;

		this.isLitUp = isLitUp;
		markDirty();

		if (sync) {
			sendState(StateTypeEnum.LIGHT_STATE, getState(StateTypeEnum.LIGHT_STATE));
		}
	}

	public boolean isLitUp(IBlockState state) {
		return state.getValue(AunisProps.MEMBER_VARIANT) == EnumMemberVariant.CHEVRON && isLitUp;
	}


	// ---------------------------------------------------------------------------------
	private IBlockState camoBlockState;

	/**
	 * Should only be called from server. Updates camoBlockState and
	 * syncs the change to clients.
	 *
	 * @param camoBlockState Camouflage block state.
	 */
	public void setCamoState(IBlockState camoBlockState) {
		// Aunis.logger.debug("Setting camo for " + pos + " to " + camoBlockState);

		this.camoBlockState = camoBlockState;
		markDirty();

		if (!worldObj.isRemote) {
			sendState(StateTypeEnum.CAMO_STATE, getState(StateTypeEnum.CAMO_STATE));
		}

		else {
			Aunis.LOG.warn("Tried to set camoBlockState from client. This won't work!");
		}
	}

	public IBlockState getCamoState() {
		return camoBlockState;
	}

	public ItemStack getCamoItemStack() {
		if (camoBlockState != null) {
			Block block = camoBlockState.getBlock();

			if (block == Blocks.snow_layer)
				return null;

			int quantity = 1;
			int meta;

			if (block instanceof BlockSlab && ((BlockSlab) block).isOpaqueCube()) {
				quantity = 2;
				meta = BaseUtils.getMetaFromBlockState(camoBlockState);

				if (block == Blocks.double_stone_slab)
					block = Blocks.stone_slab;

				else if (block == Blocks.double_wooden_slab)
					block = Blocks.wooden_slab;
			}

			else {
				meta = BaseUtils.getMetaFromBlockState(camoBlockState);
			}

			return new ItemStack(block, quantity, meta);
		}

		else {
			return null;
		}
	}

	// ---------------------------------------------------------------------------------
	// NBT

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("isLitUp", isLitUp);

		if (camoBlockState != null) {
			compound.setString("doubleSlabBlock", Block.blockRegistry.getNameForObject(camoBlockState.getBlock()));

			compound.setInteger("doubleSlabMeta", BaseUtils.getMetaFromBlockState(camoBlockState));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		isLitUp = compound.getBoolean("isLitUp");

		if (compound.hasKey("doubleSlabBlock")) {
			Block dblSlabBlock = Block.getBlockFromName(compound.getString("doubleSlabBlock"));
			camoBlockState = BaseUtils.getBlockStateFromMeta(dblSlabBlock, compound.getInteger("doubleSlabMeta"));
		}

		super.readFromNBT(compound);
	}


	// ---------------------------------------------------------------------------------
	// States

	protected void sendState(StateTypeEnum type, State state) {
		if (worldObj.isRemote)
			return;

		if (targetPoint != null) {
			AunisPacketHandler.INSTANCE.sendToAllAround(new StateUpdatePacketToClient(pos, type, state), targetPoint);
		}

		else {
            Aunis.LOG.debug("targetPoint was null trying to send " + type + " from " + this.getClass().getCanonicalName());
		}
	}

	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case CAMO_STATE:
				return new StargateCamoState(camoBlockState);

			case LIGHT_STATE:
				return new StargateLightState(isLitUp);

			default:
				return null;
		}
	}
	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case CAMO_STATE:
				return new StargateCamoState();

			case LIGHT_STATE:
				return new StargateLightState();

			default:
				return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case CAMO_STATE:
				StargateCamoState memberState = (StargateCamoState) state;
				camoBlockState = memberState.getState();

				worldObj.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
				break;

			case LIGHT_STATE:
				isLitUp = ((StargateLightState) state).isLitUp();
				worldObj.func_147451_t(pos.getX(), pos.getY(), pos.getZ());
				worldObj.updateLightByType(EnumSkyBlock.Block, pos.getX(), pos.getY(), pos.getZ());

				break;

			default:
				break;
		}
	}
}
