package com.mrjake.aunis.block.stargate;


import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.gui.GuiIdEnum;
import com.mrjake.aunis.stargate.CamoPropertiesHelper;
import com.mrjake.aunis.stargate.EnumMemberVariant;
import com.mrjake.aunis.tileentity.stargate.StargateClassicBaseTile;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.minecraft.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class StargateClassicBaseBlock extends StargateAbstractBaseBlock {

	public StargateClassicBaseBlock(String blockName) {
		super(blockName);
	}

	// --------------------------------------------------------------------------------------
	// Interactions

	@Override
	public void onBlockPlaced(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		StargateClassicBaseTile gateTile = (StargateClassicBaseTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
		EnumFacing facing = EnumFacing.byHorizontalIndex(MathHelper.floor((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();

		if (!world.isRemote) {
			state = state.withProperty(AunisProps.FACING_HORIZONTAL, facing)
					.withProperty(AunisProps.RENDER_BLOCK, true);

            BaseUtils.setWorldBlockState(world, pos, state);

			gateTile.updateFacing(facing, true);
			gateTile.updateMergeState(gateTile.getMergeHelper().checkBlocks(world, pos, facing), facing);
		}
	}

	@Override
	protected void showGateInfo(EntityPlayer player, World world, BlockPos pos) {
		StargateClassicBaseTile tile = (StargateClassicBaseTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
		if(!tile.tryInsertUpgrade(player) && tile.isMerged()) {
			player.openGui(Aunis.instance, GuiIdEnum.GUI_STARGATE.id, world, pos.getX(), pos.getY(), pos.getZ());
		}
	}

	@Override
	protected IBlockState createMemberState(IBlockState memberState, EnumFacing facing, int meta) {
		return memberState.withProperty(AunisProps.RENDER_BLOCK, true)
				.withProperty(AunisProps.FACING_HORIZONTAL, facing)
				.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.byId((meta >> 3) & 0x01));
	}

	// --------------------------------------------------------------------------------------
	// Rendering

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return CamoPropertiesHelper.getStargateBlockBoundingBox(state, access, pos, false);
	}

	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return CamoPropertiesHelper.getStargateBlockBoundingBox(state, access, pos, true);
	}
}
