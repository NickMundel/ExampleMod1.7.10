package com.mrjake.aunis.block.stargate;

import com.mrjake.aunis.AunisProps;
import com.mrjake.aunis.stargate.EnumMemberVariant;
import com.mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.BlockFaceShape;
import com.mrjake.aunis.util.EnumBlockRenderType;
import com.mrjake.aunis.util.blocks.BaseBlock;
import com.mrjake.aunis.util.blockstates.BlockState;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static com.mrjake.aunis.util.minecraft.EnumFacing.HORIZONTALS;

public abstract class StargateAbstractBaseBlock extends BaseBlock {

    public StargateAbstractBaseBlock(String blockName) {
        super(Material.iron);

        //setUnlocalizedName(Aunis.ModID + "." + blockName);

        setDefaultState(blockState.getBaseState()
                .withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.NORTH)
                .withProperty(AunisProps.RENDER_BLOCK, true));

        setLightOpacity(0);
        setHardness(3.0f);
        setHarvestLevel("pickaxe", 3);
    }


    // --------------------------------------------------------------------------------------
    // Block states

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, AunisProps.FACING_HORIZONTAL, AunisProps.RENDER_BLOCK);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(AunisProps.RENDER_BLOCK) ? 0x04 : 0) |
                state.getValue(AunisProps.FACING_HORIZONTAL).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(AunisProps.RENDER_BLOCK, (meta & 0x04) != 0)
                .withProperty(AunisProps.FACING_HORIZONTAL, HORIZONTALS[meta & 0x03]);
    }


    // ------------------------------------------------------------------------
    // Block behavior

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!world.isRemote) {
            if(!player.isSneaking() && !tryAutobuild(player, world, pos)) {
                showGateInfo(player, world, pos);
            }
        }

        return !player.isSneaking();
    }

    protected abstract void showGateInfo(EntityPlayer player, World world, BlockPos pos);

    protected boolean tryAutobuild(EntityPlayer player, World world, BlockPos basePos) {
        final StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) world.getTileEntity(basePos.getX(), basePos.getY(), basePos.getZ());
        final EnumFacing facing = gateTile.getFacing();

        StargateAbstractMergeHelper mergeHelper = gateTile.getMergeHelper();
        ItemStack stack = player.getHeldItem();

        if(!gateTile.isMerged()) {

        	// This check ensures that stack represents matching member block.
        	EnumMemberVariant variant = mergeHelper.getMemberVariantFromItemStack(stack);

            if (variant != null) {
                List<BlockPos> posList = mergeHelper.getAbsentBlockPositions(world, basePos, facing, variant);

                if(!posList.isEmpty()) {
                	BlockPos pos = posList.get(0);

                	if (BaseUtils.getWorldBlockState(world, pos).getBlock().isReplaceable(world, pos.getX(), pos.getY(), pos.getZ())) {
                		IBlockState memberState = mergeHelper.getMemberBlock().getDefaultState();
                        BaseUtils.setWorldBlockState(world, pos, createMemberState(memberState, facing, stack.getItem().getMetadata(stack.getItemDamage())));

                		SoundType soundtype = memberState.getBlock().getSoundType(memberState, world, pos, player);
        				world.playSound(pos.getX(),pos.getY(), pos.getZ(), soundtype.getBreakSound(), (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F, true);

                        if(!player.capabilities.isCreativeMode)
                            BaseUtils.shrink(stack, 1);

                        // If it was the last chevron/ring
                        if(posList.size() == 1)
                            gateTile.updateMergeState(gateTile.getMergeHelper().checkBlocks(world, basePos, facing), facing);

                        return true;
                	}
                }
            } // variant == null, wrong block held
        }

        return false;
    }

    protected abstract IBlockState createMemberState(IBlockState memberState, EnumFacing facing, int meta);

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            StargateAbstractBaseTile gateTile = (StargateAbstractBaseTile) world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
            gateTile.updateMergeState(false, state.getValue(AunisProps.FACING_HORIZONTAL));
            gateTile.onBlockBroken();
        }
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te) {
        super.harvestBlock(world, player, pos, state, te);
        world.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());
    }


    // --------------------------------------------------------------------------------------
    // TileEntity

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public abstract TileEntity createTileEntity(World world, IBlockState state);


    // --------------------------------------------------------------------------------------
    // Rendering

    @Override
    public int getRenderType(IBlockState state) {
        if (state.getValue(AunisProps.RENDER_BLOCK))
            return EnumBlockRenderType.MODEL.ordinal();
        else
            return EnumBlockRenderType.ENTITYBLOCK_ANIMATED.ordinal();
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
