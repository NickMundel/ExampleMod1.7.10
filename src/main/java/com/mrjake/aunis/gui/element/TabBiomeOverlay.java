package com.mrjake.aunis.gui.element;

import com.mrjake.aunis.config.AunisConfig;
import com.mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import com.mrjake.aunis.util.ItemMetaPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TabBiomeOverlay extends Tab {

	private EnumSet<BiomeOverlayEnum> supportedOverlays;
	private SlotTab slot;

	private int slotTexX;
	private int slotTexY;

	protected TabBiomeOverlay(TabBiomeOverlayBuilder builder) {
		super(builder);

		supportedOverlays = builder.supportedOverlays;
		slotTexX = builder.slotTexX;
		slotTexY = builder.slotTexY;
	}

	@Override
	public void render(FontRenderer fontRenderer, int mouseX, int mouseY) {
		super.render(fontRenderer, mouseX, mouseY);

		// Draw page slot
		Minecraft.getMinecraft().getTextureManager().bindTexture(bgTexLocation);
		GL11.glColor4f(1, 1, 1, 1);
		Gui.func_146110_a(guiLeft+currentOffsetX+5, guiTop+defaultY+24, slotTexX, slotTexY, 18, 18, textureSize, textureSize);
	}

	@Override
	public void renderFg(GuiScreen screen, FontRenderer fontRenderer, int mouseX, int mouseY) {
		super.renderFg(screen, fontRenderer, mouseX, mouseY);

		if (isVisible() && isOpen()) {
			if (GuiHelper.isPointInRegion(guiLeft+currentOffsetX+6, guiTop+defaultY+25, 16, 16, mouseX, mouseY) && !slot.getHasStack()) {
				List<String> text = new ArrayList<>();
				text.add(I18n.format("gui.stargate.biome_overlay.help"));

				for (BiomeOverlayEnum biomeOverlay : BiomeOverlayEnum.values()) {
					if (!supportedOverlays.contains(biomeOverlay))
						continue;

					String line = biomeOverlay.getLocalizedColorizedName() + ": ";

					for (ItemMetaPair itemMeta : AunisConfig.stargateConfig.getBiomeOverrideBlocks().get(biomeOverlay)) {
						line += itemMeta.getDisplayName() + ", ";
					}

					text.add(line);
				}

				screen.drawHoveringText(text, mouseX-guiLeft, mouseY-guiTop);
			}
		}
	}

    public SlotTab createAndSaveSlot(Slot slot) {
        this.slot = new SlotTab(slot);
        this.slot.xDisplayPosition = currentOffsetX + 6;
        this.slot.yDisplayPosition = defaultY + 25;
        return this.slot;
    }

	// ------------------------------------------------------------------------------------------------
	// Builder

	public static TabBiomeOverlayBuilder builder() {
		return new TabBiomeOverlayBuilder();
	}

	public static class TabBiomeOverlayBuilder extends TabBuilder {

		private EnumSet<BiomeOverlayEnum> supportedOverlays;
		private int slotTexX;
		private int slotTexY;

		public TabBiomeOverlayBuilder setSupportedOverlays(EnumSet<BiomeOverlayEnum> supportedOverlays) {
			this.supportedOverlays = supportedOverlays;
			return this;
		}

		public TabBiomeOverlayBuilder setSlotTexture(int x, int y) {
			slotTexX = x;
			slotTexY = y;

			return this;
		}

		@Override
		public TabBiomeOverlay build() {
			return new TabBiomeOverlay(this);
		}
	}
}
