package com.mrjake.aunis.gui.element;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.util.minecraft.TextFormatting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Diode {

	public static final ResourceLocation DIODE_TEXTURE = new ResourceLocation(Aunis.MODID, "textures/gui/diodes.png");

	private GuiScreen screen;

    private FontRenderer fontRenderer;
	private int x;
	private int y;

	private String description;
	private Map<DiodeStatus, String> statusStringMap;
	private DiodeStatus status;
	private StatusMapperInterface statusMapper;
	private StatusStringMapperInterface statusStringMapper;

	public Diode(FontRenderer fontrendererObj,GuiScreen screen, int x, int y, String description) {
        this.fontRenderer = fontrendererObj;
        this.screen = screen;
		this.x = x;
		this.y = y;
		this.description = description;
		this.statusStringMap = new HashMap<DiodeStatus, String>(3);
	}

	public Diode putStatus(DiodeStatus status, String statusString) {
		statusStringMap.put(status, statusString);
		return this;
	}

	public Diode setStatusMapper(StatusMapperInterface statusMapper) {
		this.statusMapper = statusMapper;
		return this;
	}

	public Diode setStatusStringMapper(StatusStringMapperInterface statusStringMapper) {
		this.statusStringMapper = statusStringMapper;
		return this;
	}

	public Diode setDiodeStatus(DiodeStatus status) {
		this.status = status;
		return this;
	}

	public boolean render(int mouseX, int mouseY) {
		status = statusMapper.get();

		GL11.glEnable(GL11.GL_BLEND);
		screen.mc.getTextureManager().bindTexture(DIODE_TEXTURE);
		Gui.func_146110_a(x, y, status.xTex, status.yTex, 8, 7, 16, 16);
		GL11.glDisable(GL11.GL_BLEND);

		return GuiHelper.isPointInRegion(x, y, 8, 8, mouseX, mouseY);
	}

	public void renderTooltip(int mouseX, int mouseY) {
		String statusString = null;

		if (statusStringMapper != null)
			statusString = statusStringMapper.get();

		if (statusString == null)
			statusString = statusStringMap.get(status);

        String formattedStatusString = EnumChatFormatting.ITALIC + statusString;
        formattedStatusString = status.color + formattedStatusString + EnumChatFormatting.RESET;

        screen.drawHoveringText(Arrays.asList(
            description,
            formattedStatusString), mouseX, mouseY);
	}

	public static enum DiodeStatus {
		OFF(0, 0, TextFormatting.DARK_RED),
		WARN(8, 0, TextFormatting.YELLOW),
		ON(0, 7, TextFormatting.GREEN);

		public int xTex;
		public int yTex;
		public TextFormatting color;

		private DiodeStatus(int xTex, int yTex, TextFormatting color) {
			this.xTex = xTex;
			this.yTex = yTex;
			this.color = color;
		}
	}

	public static interface StatusMapperInterface {
		public DiodeStatus get();
	}

	public static interface StatusStringMapperInterface {

		/**
		 * @return Custom status string or {@code null} to use {@link Map} one.
		 */
		@Nullable
		public String get();
	}
}
