package com.mrjake.aunis.item.dialer;

import com.mrjake.aunis.block.AunisBlocks;
import com.mrjake.aunis.util.BlockMatcher;
import com.mrjake.aunis.util.EnumKeyInterface;
import com.mrjake.aunis.util.EnumKeyMap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;

public enum UniverseDialerMode implements EnumKeyInterface<Byte> {
    //TODO:FIX
	NEARBY(0, "item.aunis.universe_dialer.mode_scan", true, "linkedGate", "nearby", BlockMatcher.forBlock(AunisBlocks.INVISIBLE_BLOCK)),
	MEMORY(1, "item.aunis.universe_dialer.mode_saved", true, "linkedGate", "saved", BlockMatcher.forBlock(AunisBlocks.INVISIBLE_BLOCK)),
	RINGS(2, "item.aunis.universe_dialer.mode_rings", true, "linkedRings", "rings", BlockMatcher.forBlock(AunisBlocks.INVISIBLE_BLOCK)),
	OC(3, "item.aunis.universe_dialer.mode_oc", false, null, "ocmess", null);

	public final byte id;
	public final String translationKey;
	public final boolean linkable;
	public final String tagPosName;
	public final String tagListName;
	public final BlockMatcher matcher;

	private UniverseDialerMode(int id, String translationKey, boolean linkable, String tagPosName, String tagListName, BlockMatcher matcher) {
		this.id = (byte) id;
		this.translationKey = translationKey;
		this.linkable = linkable;
		this.tagPosName = tagPosName;
		this.tagListName = tagListName;
		this.matcher = matcher;
	}

	public UniverseDialerMode next() {
		switch (this) {
		case NEARBY: return MEMORY;
		case MEMORY: return RINGS;
		case RINGS: return NEARBY;
		case OC: return NEARBY;
		}

		return null;
	}

	public UniverseDialerMode prev() {
		switch (this) {
			case NEARBY: return RINGS;
			case MEMORY: return NEARBY;
			case RINGS: return MEMORY;
			case OC: return RINGS;
		}

		return null;
	}

	@SideOnly(Side.CLIENT)
	public String localize() {
		return I18n.format(translationKey);
	}

	@Override
	public Byte getKey() {
		return id;
	}

	private static final EnumKeyMap<Byte, UniverseDialerMode> ID_MAP = new EnumKeyMap<Byte, UniverseDialerMode>(values());

	public static UniverseDialerMode valueOf(byte id) {
		return ID_MAP.valueOf(id);
	}
}
