package arm32x.minecraft.commandblockide.client.gui;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

public final class ToolbarSeparator extends DrawableHelper implements Drawable, Element, Selectable {
	public static final int COLOR = 0x3FFFFFFF;

	public int x, y, height;

	public ToolbarSeparator(int x, int y, int height) {
		this.x = x;
		this.y = y;
		this.height = height;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		fill(matrices, x, y, x + 1, y + height, COLOR);
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
	public void appendNarrations(NarrationMessageBuilder builder) {

	}
}
