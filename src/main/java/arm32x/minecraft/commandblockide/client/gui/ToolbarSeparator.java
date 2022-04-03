package arm32x.minecraft.commandblockide.client.gui;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

public final class ToolbarSeparator extends ClickableWidget implements Drawable, Element, Selectable {
	public static final int COLOR = 0x3FFFFFFF;

	public ToolbarSeparator() {
		super(0, 0, 0, 18, LiteralText.EMPTY);
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		fill(matrices, x, y + 1, x + 1, y + 1 + height, COLOR);
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
		return LiteralText.EMPTY.copy();
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) { }
}
