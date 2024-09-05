package com.mrjake.aunis.item;

import com.mrjake.aunis.Aunis;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class AunisItems {

    public static String assetKey = Aunis.MODID.toLowerCase();
    public static String itemDomain = assetKey;

    public static List<Item> registeredItems = new ArrayList<Item>();

	/**
	 * DHD power/control crystal
	 */
	public static final Item CRYSTAL_CONTROL_DHD = newItem("crystal_control_dhd");

	/**
	 * These allow for dialing 8th glyph(cross dimension travel) and show different address spaces
	 */
	public static final Item CRYSTAL_GLYPH_DHD = newItem("crystal_glyph_dhd");
	public static final Item CRYSTAL_GLYPH_STARGATE = newItem("crystal_glyph_stargate");
	public static final Item CRYSTAL_GLYPH_MILKYWAY = newItem("crystal_glyph_milkyway");
	public static final Item CRYSTAL_GLYPH_PEGASUS = newItem("crystal_glyph_pegasus");
	public static final Item CRYSTAL_GLYPH_UNIVERSE = newItem("crystal_glyph_universe");

	/**
	 * Diffrent Naquadah(main Stargate building material) stages of purity
	 */
	public static final Item NAQUADAH_SHARD = newItem("naquadah_shard");
	public static final Item NAQUADAH_ALLOY_RAW = newItem("naquadah_alloy_raw");
	public static final Item NAQUADAH_ALLOY = newItem("naquadah_alloy");

	/**
	 * Crafting items
	 */
	public static final Item CRYSTAL_SEED = newItem("crystal_fragment");
	public static final Item CRYSTAL_BLUE = newItem("crystal_blue");
	public static final Item CRYSTAL_RED = newItem("crystal_red");
	public static final Item CRYSTAL_ENDER = newItem("crystal_ender");
	public static final Item CRYSTAL_YELLOW = newItem("crystal_yellow");
	public static final Item CRYSTAL_WHITE = newItem("crystal_white");

	public static final Item CIRCUIT_CONTROL_BASE = newItem("circuit_control_base");
	public static final Item CIRCUIT_CONTROL_CRYSTAL = newItem("circuit_control_crystal");
	public static final Item CIRCUIT_CONTROL_NAQUADAH = newItem("circuit_control_naquadah");

	public static final Item STARGATE_RING_FRAGMENT = newItem("stargate_ring_fragment");
	public static final Item UNIVERSE_RING_FRAGMENT = newItem("universe_ring_fragment");
	public static final Item TR_RING_FRAGMENT = newItem("transportrings_ring_fragment");
	public static final Item HOLDER_CRYSTAL = newItem("holder_crystal");

	public static final Item DHD_BRB = newItem("dhd_brb");

	//public static final NotebookItem NOTEBOOK_ITEM = new NotebookItem();
    //public static final PageNotebookItem PAGE_NOTEBOOK_ITEM = new PageNotebookItem();
    //public static final PageMysteriousItem PAGE_MYSTERIOUS_ITEM = new PageMysteriousItem();
    //public static final UniverseDialerItem UNIVERSE_DIALER = new UniverseDialerItem();

	public static final Item BEAMER_CRYSTAL_POWER = newItem("beamer_crystal_power");
	public static final Item BEAMER_CRYSTAL_FLUID = newItem("beamer_crystal_fluid");
	public static final Item BEAMER_CRYSTAL_ITEMS = newItem("beamer_crystal_items");

	private static Item[] items = {
		CRYSTAL_CONTROL_DHD,

		CRYSTAL_GLYPH_DHD,
		CRYSTAL_GLYPH_STARGATE,
		CRYSTAL_GLYPH_MILKYWAY,
		CRYSTAL_GLYPH_PEGASUS,
		CRYSTAL_GLYPH_UNIVERSE,

		NAQUADAH_SHARD,
		NAQUADAH_ALLOY,
		NAQUADAH_ALLOY_RAW,

		CRYSTAL_SEED,
		CRYSTAL_BLUE,
		CRYSTAL_RED,
		CRYSTAL_ENDER,
		CRYSTAL_YELLOW,
		CRYSTAL_WHITE,

		CIRCUIT_CONTROL_BASE,
		CIRCUIT_CONTROL_CRYSTAL,
		CIRCUIT_CONTROL_NAQUADAH,

		STARGATE_RING_FRAGMENT,
		UNIVERSE_RING_FRAGMENT,
		TR_RING_FRAGMENT,
		HOLDER_CRYSTAL,

		DHD_BRB,
		//NOTEBOOK_ITEM,
		//PAGE_NOTEBOOK_ITEM,
		//PAGE_MYSTERIOUS_ITEM,
		//UNIVERSE_DIALER,

		BEAMER_CRYSTAL_POWER,
		BEAMER_CRYSTAL_FLUID,
		BEAMER_CRYSTAL_ITEMS
	};

	public static Item[] getItems() {
		return items;
	}

    public static Item newItem(String name) {
        return newItem(name, Item.class);
    }

    public static <ITEM extends Item> ITEM newItem(String name, Class<ITEM> cls) {
        ITEM item;
        try {
            Constructor<ITEM> ctor = cls.getConstructor();
            item = ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return addItem(item, name);
    }

    public static <ITEM extends Item> ITEM addItem(ITEM item, String name) {
        String qualName = itemDomain + ":" + name;
        item.setUnlocalizedName(qualName);
        item.setTextureName(assetKey + ":" + name);
        GameRegistry.registerItem(item, name);
        Aunis.LOG.debug(String.format("BaseMod.addItem: Registered %s as %s", item, name));
        Aunis.LOG.debug(String.format("BaseMod.addItem: Setting creativeTab of %s to %s", name, Aunis.aunisCreativeTab));
        item.setCreativeTab(Aunis.aunisCreativeTab);
        registeredItems.add(item);
        return item;
    }
}
