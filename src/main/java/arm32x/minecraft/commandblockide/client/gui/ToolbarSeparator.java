package arm32x.minecraft.commandblockide.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ToolbarSeparator extends AbstractWidget {
	public static final int COLOR = 0x3FFFFFFF;

	public ToolbarSeparator() {
		super(0, 0, 0, 18, Component.empty());
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		context.fill(getX(), getY() + 1, getX() + 1, getY() + 1 + height, COLOR);
	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	protected MutableComponent createNarrationMessage() {
		return Component.empty();
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput builder) { }
}
