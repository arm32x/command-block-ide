package arm32x.minecraft.commandblockide.client.gui.button;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

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

	public boolean drawsBackground() {
		return false;
	}

	@Override
	public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		var texture = getTexture();

		int iconX = getX() + (width - iconWidth) / 2;
		int iconY = getY() + (height - iconHeight) / 2;

		int color = active ? 0xFFFFFFFF : 0x7FFFFFFF;

		context.blit(
				RenderPipelines.GUI_TEXTURED,
				texture,
				iconX,
				iconY,
				0.0F,
				0.0F,
				iconWidth,
				iconHeight,
				iconWidth,
				iconHeight,
				color
		);
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput builder) {
		defaultButtonNarrationText(builder);
	}
}