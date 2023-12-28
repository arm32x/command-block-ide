package arm32x.minecraft.commandblockide.client.gui.button;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class IconButton extends PressableWidget {
	protected final int iconWidth;
	protected final int iconHeight;

	public IconButton(int x, int y, int width, int height) {
		this(x, y, width, height, width, height);
	}

	public IconButton(int x, int y, int width, int height, int iconWidth, int iconHeight) {
		super(x, y, width, height, Text.empty());
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
	}

	@Override
	public final Text getMessage() {
		return Text.empty();
	}

	@Override
	protected abstract MutableText getNarrationMessage();

	protected abstract Identifier getTexture();

	/**
	 * Whether the standard Minecraft button background will be rendered. This
	 * does not affect the clickable area or the tooltip.
	 */
	public boolean drawsBackground() {
		return false;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		boolean drawsBackground = drawsBackground();
		if (drawsBackground) {
			super.renderWidget(context, mouseX, mouseY, delta);
		}

		var texture = getTexture();

		int iconX = getX() + (width - iconWidth) / 2;
		int iconY = getY() + (height - iconHeight) / 2;

		if (drawsBackground) {
			float brightness = active ? 1.0f : (float)0xA0 / 0xFF;
			RenderSystem.setShaderColor(brightness / 4, brightness / 4, brightness / 4, alpha);
			context.drawTexture(texture, iconX + 1, iconY + 1, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
			RenderSystem.setShaderColor(brightness, brightness, brightness, alpha);
			context.drawTexture(texture, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
		} else {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();

			if (active) {
				RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 0.25f * alpha);
				context.drawTexture(texture, iconX + 1, iconY + 1, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
			}
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, active ? alpha : 0.5f * alpha);
			context.drawTexture(texture, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);

			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			RenderSystem.disableDepthTest();
			RenderSystem.disableBlend();
		}
	}

	@Override
	public void appendClickableNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
}
