package com.mrjake.aunis.gui;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.packet.AunisPacketHandler;
import com.mrjake.aunis.state.TransportRingsGuiState;
import com.mrjake.aunis.transportrings.TransportRings;
import com.mrjake.aunis.util.minecraft.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RingsGUI extends GuiBase {

	private BlockPos pos;
	public TransportRingsGuiState state;

	public RingsGUI(BlockPos pos, TransportRingsGuiState state) {
		super(196, 160, 8, FRAME_COLOR, BG_COLOR, TEXT_COLOR, 4);

		this.pos = pos;
		this.state = state;
	}

	private List<GuiTextField> textFields = new ArrayList<>();

	private GuiTextField addressTextField;
	private GuiTextField nameTextField;

	private AunisGuiButton saveButton;

	@Override
	public void initGui() {
		super.initGui();

		addressTextField = createTextField(50, 20, 1, state.isInGrid() ? "" + state.getAddress() : "");
		textFields.add(addressTextField);

		nameTextField = createTextField(50, 35, 16, state.getName());
		textFields.add(nameTextField);

		saveButton = new AunisGuiButton(id++, getBottomRightInside(false)-90, getBottomRightInside(true)-20, 90, 20, Aunis.proxy.localize("tile.aunis.transportrings_block.rings_save"));
		buttonList.add(saveButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//		drawDefaultBackground();

		mouseX -= getTopLeftAbsolute(false);
		mouseY -= getTopLeftAbsolute(true);

		GL11.glPushMatrix();
		translateToCenter();
		drawBackground();

		if (state.isInGrid()) {
			drawVerticallCenteredString(new ChatComponentTranslation("tile.aunis.transportrings_block.rings_no", state.getAddress()).getFormattedText(), 0, 0, 0xAA5500);
//			drawText("Connected to:", 0, 13, color(88, 97, 115, 255));
		}

		else {
			drawVerticallCenteredString(Aunis.proxy.localize("tile.aunis.transportrings_block.rings_not_in_grid"), 0, 0, 0xB36262);
		}

		drawString(Aunis.proxy.localize("tile.aunis.transportrings_block.rings_address") + ": ", 0, 20, 0x00AA00);
		drawString(Aunis.proxy.localize("tile.aunis.transportrings_block.rings_name") + ": ", 0, 35, 0x00AAAA);
//		this.addressTextField.drawTextBox();

		for (GuiTextField tf : textFields)
			drawTextBox(tf);

		int y = 50;
		for (TransportRings rings : state.getRings()) {
			drawString(""+rings.getAddress(), 60, y, 0x00AA00);
			drawString(rings.getName(), 70, y, 0x00AAAA);

			y += 12;
//			drawString(rings.getName(), len, 40+i*10, textColor);
		}

//		addressTextField.setText(String.valueOf(state.getAddress()));

		// ------------------------------------------------------------------------------
//		int x = frameThickness+padding;
//		drawRect(x, x, x+fontRenderer.getStringWidth("123"), x+10, 0xaaffffff);


		super.drawScreen(mouseX, mouseY, partialTicks);

		GL11.glPopMatrix();


	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button == saveButton) {
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;

			try {
				int address = Integer.valueOf(addressTextField.getText());
				String name = nameTextField.getText();

				if (address > 0 && address <= 6) {
					//AunisPacketHandler.INSTANCE.sendToServer(new SaveRingsParametersToServer(pos, address, name));
				}

				else {
					player.addChatMessage(new ChatComponentTranslation("tile.aunis.transportrings_block.wrong_address"));
				}
			}

			catch (NumberFormatException e) {
				player.addChatMessage(new ChatComponentTranslation("tile.aunis.transportrings_block.wrong_address"));
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		super.keyTyped(typedChar, keyCode);

		for (GuiTextField tf : textFields)
			tf.textboxKeyTyped(typedChar, keyCode);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		for (GuiTextField tf : textFields)
			tf.updateCursorCounter();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		mouseX -= getTopLeftAbsolute(false);
		mouseY -= getTopLeftAbsolute(true);

		super.mouseClicked(mouseX, mouseY, mouseButton);

		for (GuiTextField tf : textFields)
			tf.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
