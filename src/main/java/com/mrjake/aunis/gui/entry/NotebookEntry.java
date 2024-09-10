package com.mrjake.aunis.gui.entry;

import com.mrjake.aunis.packet.gui.entry.EntryDataTypeEnum;
import com.mrjake.aunis.stargate.network.StargateAddress;
import com.mrjake.aunis.stargate.network.SymbolInterface;
import com.mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.Minecraft;
public class NotebookEntry extends AbstractAddressEntry {

	public static final int ADDRESS_WIDTH = 160;
	public static final int BUTTON_COUNT = 3;

	public NotebookEntry(Minecraft mc, int index, int maxIndex, String name, ActionListener reloadListener, SymbolTypeEnum type, StargateAddress addr, int maxSymbols) {
		super(mc, index, maxIndex, name, reloadListener, type, addr, maxSymbols);
	}

	@Override
	public void renderAt(int dx, int dy, int mouseX, int mouseY, float partialTicks) {
		final int size = 20;
		int x = dx+(ADDRESS_WIDTH-size*(maxSymbols))/2;

		for (int i=0; i<maxSymbols; i++) {
			SymbolInterface symbol = stargateAddress.get(i);

			renderSymbol(x, dy, size, size, symbol);
			x += size;
		}

		super.renderAt(dx+ADDRESS_WIDTH+10, dy, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getHeight() {
		return 20;
	}

	@Override
	protected int getMaxNameLength() {
		return 11;
	}

	@Override
	protected EntryDataTypeEnum getEntryDataType() {
		return EntryDataTypeEnum.PAGE;
	}
}
