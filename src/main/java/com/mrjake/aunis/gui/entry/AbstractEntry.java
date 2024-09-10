package com.mrjake.aunis.gui.entry;

import com.mrjake.aunis.gui.BetterButton;
import com.mrjake.aunis.gui.BetterTextField;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.packet.gui.entry.EntryActionEnum;
import com.mrjake.aunis.packet.gui.entry.EntryActionToServer;
import com.mrjake.aunis.packet.gui.entry.EntryDataTypeEnum;
import com.mrjake.aunis.stargate.network.SymbolInterface;
import cpw.mods.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEntry {

	protected Minecraft mc;
	protected int index;
	protected int maxIndex;
	protected String name;

	private ActionListener actionListener;

	protected GuiTextField nameField;
	protected BetterButton upButton;
	protected BetterButton downButton;
	protected BetterButton removeButton;

	protected List<BetterButton> buttons = new ArrayList<>();
	protected List<GuiTextField> textFields = new ArrayList<>();

	public AbstractEntry(Minecraft mc, int index, int maxIndex, String name, ActionListener actionListener) {
		this.mc = mc;
		this.index = index;
		this.maxIndex = maxIndex;
		this.name = name;
		this.actionListener = actionListener;

		// ----------------------------------------------------------------------------------------------------
		// Text fields

		int tId = 0;
		nameField = new BetterTextField(mc.fontRenderer, 0, 0, 100, 20, name)
				.setActionCallback(() -> action(EntryActionEnum.RENAME));

		nameField.setText(name);
		nameField.setMaxStringLength(getMaxNameLength());
		textFields.add(nameField);


		// ----------------------------------------------------------------------------------------------------
		// Buttons

		int bId = 0;
		upButton = new BetterButton(bId++, 0, 0, 20, 20, "▲")
				.setFgColor(GuiUtils.getColorCode('a', true))
				.setActionCallback(() -> action(EntryActionEnum.MOVE_UP));

		downButton = new BetterButton(bId++, 0, 0, 20, 20, "▼")
				.setFgColor(GuiUtils.getColorCode('c', true))
				.setActionCallback(() -> action(EntryActionEnum.MOVE_DOWN));

		removeButton = new BetterButton(bId++, 0, 0, 20, 20, "x")
				.setFgColor(GuiUtils.getColorCode('c', true))
				.setActionCallback(() -> action(EntryActionEnum.REMOVE));

		buttons.add(upButton);
		buttons.add(downButton);
		buttons.add(removeButton);
	}

	public void renderAt(int dx, int dy, int mouseX, int mouseY, float partialTicks) {
//		dy += getButtonOffset();

		// Fields
		for (GuiTextField tf : textFields) {
			tf.xPosition = dx;
			tf.yPosition = dy;
			tf.drawTextBox();

			dx += tf.width + 10;
		}


		// Buttons
		boolean first = (index == 0);
		boolean last = (index == maxIndex-1);
		upButton.enabled = !first;
		downButton.enabled = !last;

		for (GuiButton btn : buttons) {
			btn.xPosition = dx;
			btn.yPosition = dy;
			btn.drawButton(mc, mouseX, mouseY);

			dx += 25;
		}
	}


	// ----------------------------------------------------------------------------------------------------
	// Actions

	protected void action(EntryActionEnum action) {
		AunisPacketHandler.INSTANCE.sendToServer(new EntryActionToServer(getEntryDataType(), action, index, nameField.getText()));
		actionListener.action(action, index);
	}


	// ----------------------------------------------------------------------------------------------------
	// Interactions

	/**
	 * Called on mouse clicked on every instance of {@link AbstractEntry}
	 * @return {@code true} when a button was clicked, {@code false} if other or no element was activated.
	 */
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton != 0)
			return false;

		for (BetterButton btn : buttons) {
			if (btn.mousePressed(mc, mouseX, mouseY)) {
				// Mouse pressed inside of this button
				btn.func_146113_a(this.mc.getSoundHandler());
				btn.performAction();

				return true;
			}
		}

		for (GuiTextField tf : textFields) {
			tf.mouseClicked(mouseX, mouseY, mouseButton);
		}

		return false;
	}

	protected void keyTyped(char typedChar, int keyCode) {
		for (GuiTextField tf : textFields) {
			tf.textboxKeyTyped(typedChar, keyCode);
		}
	}

	public void updateScreen() {
		for (GuiTextField tf : textFields) {
			tf.updateCursorCounter();
		}
	}

	protected abstract int getHeight();
//	protected abstract int getButtonOffset();
	protected abstract int getMaxNameLength();
	protected abstract EntryDataTypeEnum getEntryDataType();

	protected static void renderSymbol(int x, int y, int sizeX, int sizeY, SymbolInterface symbol) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD);
        GL11.glColor4f(0.77f, 0.77f, 0.77f, 1);

		Minecraft.getMinecraft().getTextureManager().bindTexture(symbol.getIconResource());

		Gui.func_152125_a(x, y, 0, 0, 256, 256, sizeX, sizeY, 256, 256);

        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
	}

	static interface ActionListener {
		public void action(EntryActionEnum action, int index);
	}
}
