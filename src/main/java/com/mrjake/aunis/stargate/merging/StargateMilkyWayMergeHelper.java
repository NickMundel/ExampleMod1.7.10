package com.mrjake.aunis.stargate.merging;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.block.AunisBlocks;
import com.mrjake.aunis.block.stargate.StargateMilkyWayBaseBlock;
import com.mrjake.aunis.block.stargate.StargateMilkyWayMemberBlock;
import com.mrjake.aunis.config.AunisConfig;
import com.mrjake.aunis.config.StargateSizeEnum;
import com.mrjake.aunis.stargate.EnumMemberVariant;
import com.mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import com.mrjake.aunis.util.AunisAxisAlignedBB;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.BlockMatcher;
import com.mrjake.aunis.util.FacingToRotation;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mrjake.aunis.config.StargateSizeEnum.SMALL;

public class StargateMilkyWayMergeHelper extends StargateClassicMergeHelper {

	public static final StargateMilkyWayMergeHelper INSTANCE = new StargateMilkyWayMergeHelper();

	/**
	 * Bounding box used for {@link StargateMilkyWayBaseTile} search.
	 * Searches 3 blocks to the left/right and 7 blocks down.
	 */
	private static final AunisAxisAlignedBB BASE_SEARCH_BOX_SMALL = new AunisAxisAlignedBB(-3, -7, 0, 3, 0, 0);
	private static final AunisAxisAlignedBB BASE_SEARCH_BOX_LARGE = new AunisAxisAlignedBB(-5, -9, 0, 5, 0, 0);

	public static final BlockMatcher BASE_MATCHER = BlockMatcher.forBlock(AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK);
	public static final BlockMatcher MEMBER_MATCHER = BlockMatcher.forBlock(AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK);

	private static final List<BlockPos> RING_BLOCKS_SMALL = Arrays.asList(
			new BlockPos(1, 7, 0),
			new BlockPos(3, 5, 0),
			new BlockPos(3, 3, 0),
			new BlockPos(2, 1, 0),
			new BlockPos(-2, 1, 0),
			new BlockPos(-3, 3, 0),
			new BlockPos(-3, 5, 0),
			new BlockPos(-1, 7, 0));

	private static final List<BlockPos> CHEVRON_BLOCKS_SMALL = Arrays.asList(
			new BlockPos(2, 6, 0),
			new BlockPos(3, 4, 0),
			new BlockPos(3, 2, 0),
			new BlockPos(-3, 2, 0),
			new BlockPos(-3, 4, 0),
			new BlockPos(-2, 6, 0),
			new BlockPos(1, 0, 0),
			new BlockPos(-1, 0, 0),
			new BlockPos(0, 7, 0));

	private static final List<BlockPos> RING_BLOCKS_LARGE = Arrays.asList(
			new BlockPos(-1, 0, 0),
			new BlockPos(-3, 1, 0),
			new BlockPos(-4, 3, 0),
			new BlockPos(-5, 4, 0),
			new BlockPos(-4, 6, 0),
			new BlockPos(-4, 7, 0),
			new BlockPos(-2, 9, 0),
			new BlockPos(-1, 9, 0),
			new BlockPos(1, 9, 0),
			new BlockPos(2, 9, 0),
			new BlockPos(4, 7, 0),
			new BlockPos(4, 6, 0),
			new BlockPos(5, 4, 0),
			new BlockPos(4, 3, 0),
			new BlockPos(3, 1, 0),
			new BlockPos(1, 0, 0));

	private static final List<BlockPos> CHEVRON_BLOCKS_LARGE = Arrays.asList(
			new BlockPos(3, 8, 0),
			new BlockPos(5, 5, 0),
			new BlockPos(4, 2, 0),
			new BlockPos(-4, 2, 0),
			new BlockPos(-5, 5, 0),
			new BlockPos(-3, 8, 0),
			new BlockPos(2, 0, 0),
			new BlockPos(-2, 0, 0),
			new BlockPos(0, 9, 0));

	@Override
	public List<BlockPos> getRingBlocks() {
		switch (AunisConfig.stargateSize) {
		case SMALL:
		case MEDIUM:
			return RING_BLOCKS_SMALL;

		case LARGE:
			return RING_BLOCKS_LARGE;

		default:
			return null;
		}
	}

	@Override
	public List<BlockPos> getChevronBlocks() {
		switch (AunisConfig.stargateSize) {
			case SMALL:
			case MEDIUM:
				return CHEVRON_BLOCKS_SMALL;

			case LARGE:
				return CHEVRON_BLOCKS_LARGE;

			default:
				return null;
		}
	}

	@Override
	@Nullable
	public EnumMemberVariant getMemberVariantFromItemStack(ItemStack stack) {
		if (!(stack.getItem() instanceof ItemBlock))
			return null;

		// No need to use .equals() because blocks are singletons
		if (((ItemBlock) stack.getItem()).field_150939_a != AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK)
			return null;

		int meta = BaseUtils.getMetaFromItemStack(stack);

		if (meta == AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK.RING_META)
			return EnumMemberVariant.RING;

		if (meta == AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK.CHEVRON_META)
			return EnumMemberVariant.CHEVRON;

		return null;
	}

	@Override
	public AunisAxisAlignedBB getBaseSearchBox() {
		switch (AunisConfig.stargateSize) {
			case SMALL:
			case MEDIUM:
				return BASE_SEARCH_BOX_SMALL;

			case LARGE:
				return BASE_SEARCH_BOX_LARGE;

			default:
				return null;
		}
	}

	@Override
	public boolean matchBase(IBlockState state) {
		return BASE_MATCHER.apply(state);
	}

	@Override
	public boolean matchMember(IBlockState state) {
		return MEMBER_MATCHER.apply(state);
	}

	@Override
	public StargateMilkyWayMemberBlock getMemberBlock() {
		return AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK;
	}

	/**
	 * Converts merged Stargate from old pattern (1.5)
	 * to new pattern (1.6).
	 *
	 * @param world {@link World} instance.
	 * @param basePos Position of {@link StargateMilkyWayBaseBlock} the tiles should be linked to.
	 * @param baseFacing Facing of {@link StargateMilkyWayBaseBlock}.
	 * @param currentStargateSize Current Stargate size as read from NBT.
	 * @param targetStargateSize Target Stargate size as defined in config.
	 */
	public void convertToPattern(World world, BlockPos basePos, EnumFacing baseFacing, StargateSizeEnum currentStargateSize, StargateSizeEnum targetStargateSize) {
		Aunis.LOG.debug(basePos + ": Converting Stargate from " + currentStargateSize + " to " + targetStargateSize);
		List<BlockPos> oldPatternBlocks = new ArrayList<BlockPos>();

		switch (currentStargateSize) {
			case SMALL:
			case MEDIUM:
				oldPatternBlocks.addAll(RING_BLOCKS_SMALL);
				oldPatternBlocks.addAll(CHEVRON_BLOCKS_SMALL);
				break;

			case LARGE:
				oldPatternBlocks.addAll(RING_BLOCKS_LARGE);
				oldPatternBlocks.addAll(CHEVRON_BLOCKS_LARGE);
				break;
		}
        BlockPos temp = null;
		for (BlockPos pos : oldPatternBlocks)
            temp = pos.rotate(FacingToRotation.get(baseFacing)).add(basePos);
			world.setBlockToAir(temp.getX(), temp.getY(), temp.getZ());

		IBlockState memberState = AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK.getDefaultState()
				.withProperty(AunisProps.FACING_HORIZONTAL, baseFacing)
				.withProperty(AunisProps.RENDER_BLOCK, false);

		for (BlockPos pos : getRingBlocks())
            BaseUtils.setWorldBlockState(world, pos.rotate(FacingToRotation.get(baseFacing)).add(basePos), memberState.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING));

		for (BlockPos pos : getChevronBlocks())
            BaseUtils.setWorldBlockState(world, pos.rotate(FacingToRotation.get(baseFacing)).add(basePos), memberState.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.CHEVRON));
	}
}
