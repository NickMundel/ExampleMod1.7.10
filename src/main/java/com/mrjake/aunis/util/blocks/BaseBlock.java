package com.mrjake.aunis.util.blocks;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.util.EnumWorldBlockLayer;
import com.mrjake.aunis.util.IBlock;
import com.mrjake.aunis.util.ICustomRenderer;
import com.mrjake.aunis.util.ModelSpec;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Arrays;

import static com.mrjake.aunis.util.BaseUtils.getBlockStateFromMeta;

public class BaseBlock<TE extends TileEntity> extends BlockContainer implements IBlock {

    protected final BlockState blockState;
    protected IBlockState defaultBlockState;

    protected IProperty[] properties;
    protected Object[][] propertyValues;
    protected int numProperties; // Do not explicitly initialise

    protected Class<? extends TileEntity> tileEntityClass = null;
    public BaseBlock(Material material) {
        this(material, null, null);
    }

    public BaseBlock(Material material, Class<TE> teClass) {
        this(material, teClass, null);
    }

    public BaseBlock(Material material, Class<TE> teClass, String teID) {
        super(material);
        tileEntityClass = teClass;
        if (teClass != null) {
            if (teID == null) teID = teClass.getName();
            try {
                GameRegistry.registerTileEntity(teClass, teID);
            } catch (IllegalArgumentException e) {
                // Ignore redundant registration
            }
        }
        blockState = createBlockState();
        defaultBlockState = blockState.getBaseState();
        opaque = true;
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

    @Override
    public void setRenderType(int id) {

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
}
