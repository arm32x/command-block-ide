package arm32x.minecraft.commandblockide.client.gui.button;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public abstract class IconButton extends AbstractButton {
	protected final int iconWidth;
	protected final int iconHeight;

	public IconButton(int x, int y, int width, int height) {
		this(x, y, width, height, width, height);
	}

	public IconButton(int x, int y, int width, int height, int iconWidth, int iconHeight) {
		super(x, y, width, height, Component.empty());
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
	}

	@Override
	public final Component getMessage() {
		return Component.empty();
	}

	@Override
	protected abstract MutableComponent createNarrationMessage();

	protected abstract Identifier getTexture();

	/**
	 * Whether the standard Minecraft button background will be rendered. This
	 * does not affect the clickable area or the tooltip.
	 */
	public boolean drawsBackground() {
		return false;
	}

	@Override
	public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		boolean drawsBackground = drawsBackground();
		if (drawsBackground) {
			this.extractDefaultSprite(context);
		}

		var texture = getTexture();

		int iconX = getX() + (width - iconWidth) / 2;
		int iconY = getY() + (height - iconHeight) / 2;

		if (drawsBackground) {
			float brightness = active ? 1.0f : (float)0xA0 / 0xFF;

			int color = ARGB.colorFromFloat(1.0f, brightness, brightness, brightness);
			int shadowColor = ARGB.colorFromFloat(1.0f, brightness / 4, brightness / 4, brightness / 4);

			context.blit(RenderPipelines.GUI_TEXTURED, texture, iconX + 1, iconY + 1, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight, shadowColor);
			context.blit(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight, color);
		} else {
			// RenderSystem.enableBlend();
			// RenderSystem.defaultBlendFunc();
			// RenderSystem.enableDepthTest();

			int color = active ? 0xFFFFFFFF : 0x7FFFFFFF;
			int shadowColor = 0x3F000000;

			if (active) {
				context.blit(RenderPipelines.GUI_TEXTURED, texture, iconX + 1, iconY + 1, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight, shadowColor);
			}
			context.blit(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight, color);

			// RenderSystem.disableDepthTest();
			// RenderSystem.disableBlend();
		}
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput builder) {
		defaultButtonNarrationText(builder);
	}
}
