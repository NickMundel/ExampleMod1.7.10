package com.mrjake.aunis.block;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.tileentity.DHDTile;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import com.mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import com.mrjake.aunis.util.*;
import com.mrjake.aunis.util.blocks.BaseBlock;
import com.mrjake.aunis.util.minecraft.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static com.mrjake.aunis.util.minecraft.EnumFacing.HORIZONTALS;

public class DHDBlock extends BaseBlock {

	public static final String BLOCK_NAME = "dhd_block";

	public DHDBlock() {
		super(Material.iron);
		//setUnlocalizedName(Aunis.ModID + "." + BLOCK_NAME);

        setStepSound(soundTypeMetal);
		setCreativeTab(Aunis.aunisCreativeTab);

		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.ROTATION_HORIZONTAL, 0)
				.withProperty(AunisProps.SNOWY, false));

		setLightOpacity(0);

		setHardness(3.0f);
		setResistance(20.0f);
		setHarvestLevel("pickaxe", 3);
	}

	// ------------------------------------------------------------------------
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AunisProps.ROTATION_HORIZONTAL, AunisProps.SNOWY);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state
				.getValue(AunisProps.ROTATION_HORIZONTAL);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(AunisProps.ROTATION_HORIZONTAL, meta);
	}

	public final static BlockMatcher SNOW_MATCHER = BlockMatcher.forBlock(Blocks.SNOW_LAYER);

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.withProperty(AunisProps.SNOWY, isSnowAroundBlock(world, pos));
	}

	public static boolean isSnowAroundBlock(IBlockAccess world, BlockPos inPos) {

		// Check if 4 adjacent blocks are snow layers
		for (EnumFacing facing : HORIZONTALS) {
			BlockPos pos = inPos.offset(facing);
			if (!SNOW_MATCHER.apply(world.getBlockState(pos))) {
				return false;
			}
		}

		return true;
	}

	// ------------------------------------------------------------------------
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		// Server side
		if ( !world.isRemote ) {
			int facing = MathHelper.floor( (double)((placer.rotationYaw) * 16.0F / 360.0F) + 0.5D ) & 0x0F;
			world.setBlockState(pos, state.withProperty(AunisProps.ROTATION_HORIZONTAL, facing), 3);

			DHDTile dhdTile = (DHDTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
			BlockPos closestGate = LinkingHelper.findClosestUnlinked(world, pos, LinkingHelper.getDhdRange(), AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK);

			if (closestGate != null) {
				StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) world.getTileEntity(closestGate.getX(), closestGate.getY(), closestGate.getZ());

				dhdTile.setLinkedGate(closestGate);
				gateTile.setLinkedDHD(pos);
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing facing, float hitX, float hitY, float hitZ) {
		EnumFacing dhdFacingOpposite = HORIZONTALS[Math.round(state.getValue(AunisProps.ROTATION_HORIZONTAL)/4.0f)];
		boolean backActivation = (facing == dhdFacingOpposite);

		if (!world.isRemote) {
			// Server

			if (!player.isSneaking() && backActivation) {
				// Not sneaking and activating from the back
				// Try: fluid interaction, upgrade insertion, gui opening

				if (!FluidUtil.interactWithFluidHandler(player, world, pos, null)) {
					DHDTile tile = (DHDTile) world.getTileEntity(pos);
					if(!tile.tryInsertUpgrade(player)) {
						player.openGui(Aunis.instance, GuiIdEnum.GUI_DHD.id, world, pos.getX(), pos.getY(), pos.getZ());
					}
				}
			}
		}

		// Only activate when not sneaking and activating from the back
		return !player.isSneaking() && backActivation;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		DHDTile dhdTile = (DHDTile) world.getTileEntity(pos);

		if (!world.isRemote) {
			StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) dhdTile.getLinkedGate(world);

			if (gateTile != null)
				gateTile.setLinkedDHD(null);

			ItemHandlerHelper.dropInventoryItems(world, pos, dhdTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
		}

		super.breakBlock(world, pos, state);
	}

	private int getPower(IBlockAccess world, BlockPos pos) {
		DHDTile dhdTile = (DHDTile) world.getTileEntity(pos);
		if (!dhdTile.isLinked())
			return 0;

		StargateAbstractBaseTile gateTile = dhdTile.getLinkedGate(world);

		if (gateTile == null) {
			Aunis.LOG.error("gateTile was null while getting power for DHD. Did you use Mysterious Page? ;)");
			return 0;
		}

		return gateTile.getDialedAddress().size() > 0 ? 15 : 0;
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return getPower(world, pos);
	}

	@Override
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return getPower(world, pos);
	}

	// ------------------------------------------------------------------------
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new DHDTile();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		int rotation = (int) (state.getValue(AunisProps.ROTATION_HORIZONTAL)*22.5f);

		if (rotation % 90 == 0)
			return new AunisAxisAlignedBB(-0.5, 0, -0.25, 0.5, 1, 0.25).rotate(rotation).offset(0.5, 0, 0.5);
		else
			return new AunisAxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
    	int rotation = (int) (state.getValue(AunisProps.ROTATION_HORIZONTAL)*22.5f);

		if (rotation % 90 == 0)
			return new AunisAxisAlignedBB(-0.5, 0, -0.25, 0.5, 1, 0.25).rotate(rotation).offset(0.5, 0, 0.5);
		else
			return new AunisAxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75);
    }
}
