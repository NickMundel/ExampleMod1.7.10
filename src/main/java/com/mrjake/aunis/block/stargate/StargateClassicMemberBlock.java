package com.mrjake.aunis.block.stargate;

import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.block.AunisBlocks;
import com.mrjake.aunis.block.DHDBlock;
import com.mrjake.aunis.stargate.CamoPropertiesHelper;
import com.mrjake.aunis.stargate.EnumMemberVariant;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import com.mrjake.aunis.tileentity.stargate.StargateClassicMemberTile;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.IUnlistedProperty;
import com.mrjake.aunis.util.minecraft.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IRegistry;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import static com.mrjake.aunis.util.minecraft.EnumFacing.HORIZONTALS;

public abstract class StargateClassicMemberBlock extends StargateAbstractMemberBlock {

	public StargateClassicMemberBlock(String blockName) {
		super(blockName);

		setDefaultState(blockState.getBaseState()
				.withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.NORTH)
				.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.RING)
				.withProperty(AunisProps.RENDER_BLOCK, true));
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		// Optifine shit
		if (BaseUtils.getWorldBlockState(world, pos).getBlock() != this)
			return 0;

		StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());

		if (memberTile != null)
			return memberTile.isLitUp(state) ? 7 : 0;

		else
			return 0;
	}

	// ------------------------------------------------------------------------
	@Override
	public void getSubBlocks(CreativeTabs creativeTabs, NonNullList<ItemStack> items) {
		for (EnumMemberVariant variant : EnumMemberVariant.values()) {
			items.add(new ItemStack(this, 1, getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, variant))));
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		EnumMemberVariant variant = state.getValue(AunisProps.MEMBER_VARIANT);
//		Aunis.info("state: " + state + ", meta:"+getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, variant)));

		return new ItemStack(this, 1, getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, variant)));
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		EnumMemberVariant variant = state.getValue(AunisProps.MEMBER_VARIANT);

		drops.add(new ItemStack(this, 1, getMetaFromState(getDefaultState().withProperty(AunisProps.MEMBER_VARIANT, variant))));
	}


	// ------------------------------------------------------------------------
	@SuppressWarnings("rawtypes")
	private static final IProperty[] LISTED_PROPS = new IProperty[] { AunisProps.RENDER_BLOCK, AunisProps.FACING_HORIZONTAL, AunisProps.MEMBER_VARIANT };

	@SuppressWarnings("rawtypes")
	private static final IUnlistedProperty[] UNLISTED_PROPS = new IUnlistedProperty[] { AunisProps.CAMO_BLOCKSTATE };

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, LISTED_PROPS, UNLISTED_PROPS);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 	(state.getValue(AunisProps.MEMBER_VARIANT).id << 3) |
				(state.getValue(AunisProps.RENDER_BLOCK) ? 0x04 : 0) |
				 state.getValue(AunisProps.FACING_HORIZONTAL).getHorizontalIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.byId((meta >> 3) & 0x01))
				.withProperty(AunisProps.RENDER_BLOCK, (meta & 0x04) != 0)
				.withProperty(AunisProps.FACING_HORIZONTAL, HORIZONTALS[MathHelper.abs(meta & 0x03 % HORIZONTALS.length)]);
	}


	// ------------------------------------------------------------------------
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		// Optifine shit

		if (BaseUtils.getWorldBlockState(world, pos).getBlock() != this)
			return state;

		StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos);

		if (memberTile != null) {
			IBlockState camoBlockState = memberTile.getCamoState();

			if (camoBlockState != null) {
				return state.withProperty(AunisProps.CAMO_BLOCKSTATE, camoBlockState);
			}
		}

		return state;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerCustomModel(IRegistry<String, Object> registry) {
        for (IBlockState state : getActualState().getValidStates()) {
            String variant = "";

            for (IProperty prop : state.getPropertyKeys()) {
                Object value = state.getValue(prop);
                variant += prop.getName() + "=" + value.toString() + ",";
            }

            variant = variant.substring(0, variant.length() - 1);

            String modelKey = getUnlocalizedName() + "_" + variant;

            registry.putObject(modelKey, new StargateClassicMemberBlockRenderer(this, getRenderId()));
        }
    }

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos);

		if (!world.isRemote && memberTile != null) {
			// Server and tile entity exists

			if (memberTile.isMerged() && memberTile.getCamoState() == null || DHDBlock.SNOW_MATCHER.apply(memberTile.getCamoState())) {
				// Merged and camo is empty or it's snow
				boolean snowAround = DHDBlock.isSnowAroundBlock(world, pos);

				// Set camo to snow or null
				memberTile.setCamoState(snowAround ? Blocks.SNOW_LAYER.getDefaultState() : null);
				world.setBlockState(pos, state.withProperty(AunisProps.RENDER_BLOCK, snowAround));
			}
		}
	}


	// ------------------------------------------------------------------------
	@SuppressWarnings("deprecation")
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItemStack = player.getHeldItem();
		Item heldItem = heldItemStack.getItem();
		Block heldBlock = Block.getBlockFromItem(heldItemStack.getItem());

		StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos);
//		StargateMilkyWayBaseTile gateTile = StargateMilkyWayMergeHelper.findBaseTile(world, pos, state.getValue(AunisProps.FACING_HORIZONTAL));

		if (!world.isRemote) {
//			BlockPos vec = pos.subtract(gateTile.getPos());
//			Aunis.info("new BlockPos(" + vec.getX() + ", " + vec.getY() + ", " + vec.getZ() + "),");

			IBlockState camoBlockState = memberTile.getCamoState();

			if (heldItem == Item.getItemFromBlock(AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK) ||
				heldItem == Item.getItemFromBlock(AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK) ||
				!memberTile.isMerged())

				return false;

			if (!(heldItem instanceof ItemBlock) && camoBlockState == null) {
				BlockPos basePos = memberTile.getBasePos();
				player.openGui(Aunis.instance, GuiIdEnum.GUI_STARGATE.id, world, basePos.getX(), basePos.getY(), basePos.getZ());

				return true;
			}

			if (camoBlockState != null) {
				Block camoBlock = camoBlockState.getBlock();

				if (camoBlock.getMetaFromState(camoBlockState) == heldItemStack.getMetadata()) {
					if (camoBlock instanceof BlockSlab && heldBlock instanceof BlockSlab) {
						if (((BlockSlab) camoBlock).isDouble()) {
							return false;
						}
					}

					else {
						if (camoBlock == heldBlock) {
							return false;
						}
					}
				}
			}

			if (camoBlockState != null && !(camoBlockState.getBlock() instanceof BlockSlab && heldBlock instanceof BlockSlab && !((BlockSlab) camoBlockState.getBlock()).isDouble())) {
				Block camoBlock = camoBlockState.getBlock();
				int quantity = 1;
				int meta;

				if (camoBlock instanceof BlockSlab) {
					BlockSlab blockSlab = (BlockSlab) camoBlock;
					meta = blockSlab.getMetaFromState(camoBlockState);

					if (blockSlab.isDouble()) {
						 quantity = 2;

						if (blockSlab == Blocks.double_stone_slab)
							camoBlock = Blocks.stone_slab;

						else if (blockSlab == Blocks.double_wooden_slab)
							camoBlock = Blocks.wooden_slab;

					}
				}

				else {
					meta = camoBlock.getMetaFromState(camoBlockState);
				}

				if (!player.capabilities.isCreativeMode) {
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(camoBlock, quantity, meta));
				}

				SoundType soundtype = camoBlock.getSoundType(camoBlock.getDefaultState(), world, pos, player);
				world.playSound(pos.getX(), pos.getY(), pos.getZ(), soundtype.getBreakSound(), (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F, true);

				memberTile.setCamoState(null);
				camoBlockState = null;
			}

			if (heldItem instanceof ItemBlock) {
				Block block = null;
				int meta;

				if (camoBlockState != null && camoBlockState.getBlock() == heldBlock && camoBlockState.getBlock().getMetaFromState(camoBlockState) == heldItemStack.getMetadata()) {
					BlockSlab blockSlab = (BlockSlab) camoBlockState.getBlock();
					meta = blockSlab.getMetaFromState(camoBlockState);

					if (facing != EnumFacing.UP)
						return false;

                    if (blockSlab == Blocks.double_stone_slab)
                        block = Blocks.stone_slab;

                    else if (blockSlab == Blocks.double_wooden_slab)
                        block = Blocks.wooden_slab;
				}

				else {
					if (camoBlockState != null && !player.capabilities.isCreativeMode) {
						InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(camoBlockState.getBlock(), 1, camoBlockState.getBlock().getMetaFromState(camoBlockState)));
					}

					block = Block.getBlockFromItem(heldItemStack.getItem());
					meta = heldItemStack.getMetadata();
				}

				memberTile.setCamoState(block.getStateFromMeta(meta));

				if (!player.capabilities.isCreativeMode)
					heldItemStack.shrink(1);

				SoundType soundtype = block.getSoundType(block.getDefaultState(), world, pos, player);
				world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

				world.setBlockState(pos, state.withProperty(AunisProps.RENDER_BLOCK, true), 0);
			}

			else {
				world.setBlockState(pos, state.withProperty(AunisProps.RENDER_BLOCK, false), 0);
			}

			return true;
		}

		else {
			return 	heldItem != Item.getItemFromBlock(AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK) &&
					heldItem != Item.getItemFromBlock(AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK) &&
					heldItem != Item.getItemFromBlock(AunisBlocks.STARGATE_UNIVERSE_BASE_BLOCK) &&
					heldItem != Item.getItemFromBlock(AunisBlocks.STARGATE_UNIVERSE_MEMBER_BLOCK);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (!world.isRemote) {
			EnumFacing facing = placer.getHorizontalFacing().getOpposite();

			state = state.withProperty(AunisProps.MEMBER_VARIANT, EnumMemberVariant.byId((stack.getMetadata() >> 3) & 0x01))
					.withProperty(AunisProps.RENDER_BLOCK, true)
					.withProperty(AunisProps.FACING_HORIZONTAL, facing);

			world.setBlockState(pos, state);

			StargateAbstractBaseTile gateTile = getMergeHelper().findBaseTile(world, pos, facing);

			if (gateTile != null && !gateTile.isMerged())
				gateTile.updateMergeState(gateTile.getMergeHelper().checkBlocks(world, gateTile.getPos(), world.getBlockState(gateTile.getPos()).getValue(AunisProps.FACING_HORIZONTAL)), facing);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);

		if(!world.isRemote) {
			StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
			if (memberTile.getCamoItemStack() != null)
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), memberTile.getCamoItemStack());
		}
	}

	// ------------------------------------------------------------------------

	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return CamoPropertiesHelper.getLightOpacity(state, world, pos);
	}

	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.SOLID;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return CamoPropertiesHelper.getStargateBlockBoundingBox(state, access, pos, false);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		return CamoPropertiesHelper.getStargateBlockBoundingBox(state, access, pos, true);
	}
}
