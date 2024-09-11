package com.mrjake.aunis.util.blocks;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.util.*;
import com.mrjake.aunis.util.blockstates.BlockState;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.IBlockState;
import com.mrjake.aunis.util.minecraft.IProperty;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Arrays;

import static com.mrjake.aunis.util.BaseUtils.getBlockStateFromMeta;

public class BaseBlock extends BlockContainer implements IBlock {

    protected final BlockState blockState;
    protected IBlockState defaultBlockState;

    protected IProperty[] properties;
    protected Object[][] propertyValues;
    protected int numProperties; // Do not explicitly initialise
    protected int renderID;

    public BaseBlock(Material material) {
        this(material, null);
    }

    public BaseBlock(Material material, String teID) {
        super(material);
        blockState = createBlockState();
        defaultBlockState = blockState.getBaseState();
        opaque = true;
    }

    public boolean isFullCube() {
        return super.renderAsNormalBlock();
    }

    protected final void setDefaultState(IBlockState state)
    {
        this.defaultBlockState = state;
    }

    @Override
    public int getRenderType() {
        return renderID;
    }

    @Override
    public void setRenderType(int id) {
        renderID = id;
    }

    protected BlockState createBlockState() {
        if (false) Aunis.LOG.debug("BaseBlock.createBlockState: Defining properties");
        defineProperties();
        if (false) dumpProperties();
        checkProperties();
        IProperty[] props = Arrays.copyOf(properties, numProperties);
        if (false) Aunis.LOG.debug(
            String.format("BaseBlock.createBlockState: Creating BlockState with %s properties", props.length));
        return new BlockState(this, props);
    }

    private void dumpProperties() {
        Aunis.LOG.debug(String.format("BaseBlock: Properties of %s:", getClass().getName()));
        for (int i = 0; i < numProperties; i++) {
            Aunis.LOG.debug(String.format("%s: %s", i, properties[i]));
            Object[] values = propertyValues[i];
            for (int j = 0; j < values.length; j++) Aunis.LOG.debug(String.format("   %s: %s", j, values[j]));
        }
    }

    protected void checkProperties() {
        int n = 1;
        for (int i = 0; i < numProperties; i++) n *= propertyValues[i].length;
        if (n > 16) throw new IllegalStateException(
            String.format("Block %s has %s combinations of property values (16 allowed)", getClass().getName(), n));
    }

    protected void defineProperties() {
        properties = new IProperty[4];
        propertyValues = new Object[4][];
    }

    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getStateFromMeta(meta);
    }

    public final IBlockState getDefaultState() {
        return this.defaultBlockState;
    }

    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        int m = meta;
        for (int i = numProperties - 1; i >= 0; i--) {
            Object[] values = propertyValues[i];
            int n = values.length;
            int k = m % n;
            m /= n;
            state = state.withProperty(properties[i], (Comparable) values[k]);
        }
        Aunis.LOG.debug(String.format("BaseBlock.getStateFromMeta: %s --> %s", meta, state));
        return state;
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state;
    }

    protected ThreadLocal<TileEntity> harvestingTileEntity = new ThreadLocal();

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
        return getBlockLayer() == layer;
    }

    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.SOLID;
    }

    @Override
    public ICustomRenderer getCustomRenderer() {
        return null;
    }

    @Override
    public ModelSpec getModelSpec(IBlockState state) {
        return null;
    }

    @Override
    public Class getDefaultItemClass() {
        return BaseItemBlock.class;
    }

    @Override
    public String[] getTextureNames() {
        return new String[0];
    }

    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        for (int i = numProperties - 1; i >= 0; i--) {
            Object value = state.getValue(properties[i]);
            Object[] values = propertyValues[i];
            int k = values.length - 1;
            while (k > 0 && !values[k].equals(value)) --k;
            if (false) Aunis.LOG.debug(
                String.format(
                    "BaseBlock.getMetaFromState: property %s value %s --> %s of %s",
                    i,
                    value,
                    k,
                    values.length));
            meta = meta * values.length + k;
        }
        if (false) Aunis.LOG.debug(String.format("BaseBlock.getMetaFromState: %s --> %s", state, meta));
        return meta & 15; // To be on the safe side
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
                                    float hitY, float hitZ) {
        int meta = world.getBlockMetadata(x, y, z);
        IBlockState state = getStateFromMeta(meta);
        return onBlockActivated(world, new BlockPos(x, y, z), state, player, EnumFacing.VALUES[side], hitX, hitY, hitZ);
    }

    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
                                    float cx, float cy, float cz) {
        return false;
    }

    public TileEntity getTileEntity(IBlockAccess world, BlockPos pos) {
        return world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
    }

    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
        TileEntity te = harvestingTileEntity.get();
        harvestBlock(world, player, new BlockPos(x, y, z), getStateFromMeta(meta), te);
    }

    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
        super.harvestBlock(world, player, pos.getX(), pos.getY(), pos.getZ(), getMetaFromState(state));
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        BlockPos pos = new BlockPos(x, y, z);
        breakBlock(world, pos, getStateFromMeta(meta));
        if (hasTileEntity(meta)) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof IInventory) InventoryHelper.dropInventoryItems(world, x, y, z, (IInventory) te);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    public void breakBlock(World world, BlockPos pos, IBlockState state) {}

    @Override
    public boolean hasTileEntity(int meta) {
        return hasTileEntity(getStateFromMeta(meta));
    }

    public boolean hasTileEntity(IBlockState state) {
        return false;
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing facing)
    {
        return BlockFaceShape.SOLID;
    }

}
