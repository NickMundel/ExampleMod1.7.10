package com.mrjake.aunis.stargate.merging;


import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.block.stargate.StargateMilkyWayBaseBlock;
import com.mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;
import com.mrjake.aunis.stargate.EnumMemberVariant;
import com.mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import com.mrjake.aunis.tileentity.stargate.StargateMilkyWayMemberTile;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.FacingToRotation;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class StargateClassicMergeHelper extends StargateAbstractMergeHelper {

	protected boolean checkMemberBlock(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing, EnumMemberVariant variant) {
		IBlockState state = BaseUtils.getWorldBlockState(blockAccess, pos);

		return matchMember(state) &&
				state.getValue(AunisProps.FACING_HORIZONTAL) == facing &&
				state.getValue(AunisProps.MEMBER_VARIANT) == variant;
	}

	protected void updateMemberMergeStatus(World world, BlockPos checkPos, BlockPos basePos, EnumFacing baseFacing, boolean shouldBeMerged) {
		checkPos = checkPos.rotate(FacingToRotation.get(baseFacing)).add(basePos);

		IBlockState state = BaseUtils.getWorldBlockState(world, checkPos);

		if (matchMember(state)) {
			StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(checkPos.getX(), checkPos.getY(), checkPos.getZ());

			if ((shouldBeMerged && !memberTile.isMerged()) || (memberTile.isMerged() && memberTile.getBasePos().equals(basePos))) {

				ItemStack camoStack = memberTile.getCamoItemStack();
				if (camoStack != null) {
                    EntityItem entityItem = new EntityItem(world, checkPos.getX(), checkPos.getY(), checkPos.getZ(), camoStack);
                    world.spawnEntityInWorld(entityItem);
				}

				if (memberTile.getCamoState() != null) {
					memberTile.setCamoState(null);
				}

				// This also sets merge status
				memberTile.setBasePos(shouldBeMerged ? basePos : null);

                BaseUtils.setWorldBlockState(world, checkPos, state.withProperty(AunisProps.RENDER_BLOCK, !shouldBeMerged));
			}
		}
	}

	/**
	 * Updates the {@link StargateMilkyWayBaseBlock} position of the
	 * {@link StargateMilkyWayMemberTile}.
	 *
	 * @param blockAccess Usually {@link World}.
	 * @param pos Position of the currently updated {@link StargateMilkyWayMemberBlock}.
	 * @param basePos Position of {@link StargateMilkyWayBaseBlock} the tiles should be linked to.
	 * @param baseFacing Facing of {@link StargateMilkyWayBaseBlock}.
	 */
	private void updateMemberBasePos(IBlockAccess blockAccess, BlockPos pos, BlockPos basePos, EnumFacing baseFacing) {
		IBlockState state = BaseUtils.getWorldBlockState(blockAccess, pos);

		if (matchMember(state)) {
			StargateClassicMemberTile memberTile = (StargateClassicMemberTile) blockAccess.getTileEntity(pos.getX(), pos.getY(), pos.getZ());

			memberTile.setBasePos(basePos);
		}
	}

	/**
	 * Updates all {@link StargateMilkyWayMemberTile} to contain
	 * correct {@link StargateMilkyWayBaseBlock} position.
	 *
	 * @param blockAccess Usually {@link World}.
	 * @param basePos Position of {@link StargateMilkyWayBaseBlock} the tiles should be linked to.
	 * @param baseFacing Facing of {@link StargateMilkyWayBaseBlock}.
	 */
	public void updateMembersBasePos(IBlockAccess blockAccess, BlockPos basePos, EnumFacing baseFacing) {
		for (BlockPos pos : getRingBlocks())
			updateMemberBasePos(blockAccess, pos.rotate(FacingToRotation.get(baseFacing)).add(basePos), basePos, baseFacing);

		for (BlockPos pos : getChevronBlocks())
			updateMemberBasePos(blockAccess, pos.rotate(FacingToRotation.get(baseFacing)).add(basePos), basePos, baseFacing);
	}
}
