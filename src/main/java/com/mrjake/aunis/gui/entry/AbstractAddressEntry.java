package com.mrjake.aunis.gui.entry;


import com.mrjake.aunis.stargate.network.StargateAddress;
import com.mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.Minecraft;

public abstract class AbstractAddressEntry extends AbstractEntry {

	protected SymbolTypeEnum symbolType;
	protected StargateAddress stargateAddress;
	protected int maxSymbols;

	public AbstractAddressEntry(Minecraft mc, int index, int maxIndex, String name, ActionListener actionListener, SymbolTypeEnum type, StargateAddress addr, int maxSymbols) {
		super(mc, index, maxIndex, name, actionListener);

		this.symbolType = type;
		this.stargateAddress = addr;
		this.maxSymbols = maxSymbols;
	}
}
