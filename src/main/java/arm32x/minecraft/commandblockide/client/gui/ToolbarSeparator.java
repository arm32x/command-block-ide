package arm32x.minecraft.commandblockide.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class ToolbarSeparator extends ClickableWidget {
	public static final int COLOR = 0x3FFFFFFF;

	public ToolbarSeparator() {
		super(0, 0, 0, 18, Text.empty());
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(getX(), getY() + 1, getX() + 1, getY() + 1 + height, COLOR);
	}

	@Override
	public SelectionType getType() {
		return SelectionType.NONE;
	}

	@Override
	public boolean isNarratable() {
		return false;
	}

	@Override
	protected MutableText getNarrationMessage() {
		return Text.empty();
	}

	@Override
	public void appendClickableNarrations(NarrationMessageBuilder builder) { }
}
