package arm32x.minecraft.commandblockide.client.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Container extends AbstractParentElement implements Drawable, Selectable, Element {
	protected final List<Element> children = new ArrayList<>();
	protected final List<Selectable> selectables = new ArrayList<>();
	protected final List<Drawable> drawables = new ArrayList<>();

	protected <T extends Element & Selectable & Drawable> T addDrawableChild(T child) {
		drawables.add(child);
		return addSelectableChild(child);
	}

	protected <T extends Drawable> T addDrawable(T drawable) {
		drawables.add(drawable);
		return drawable;
	}

	protected <T extends Element & Selectable> T addSelectableChild(T child) {
		children.add(child);
		selectables.add(child);
		return child;
	}

	protected void remove(Element child) {
		if (child instanceof Drawable) {
			drawables.remove(child);
		}
		if (child instanceof Selectable) {
			selectables.remove(child);
		}
		children.remove(child);
	}

	protected void clearChildren() {
		drawables.clear();
		children.clear();
		selectables.clear();
	}

	@Override
	public SelectionType getType() {
		return selectables.stream()
			.map(Selectable::getType)
			.max(Comparator.naturalOrder())
			.orElse(SelectionType.NONE);
	}

	@Override
	public List<? extends Element> children() { return children; }

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		for (Drawable drawable : drawables) {
			drawable.render(matrices, mouseX, mouseY, delta);
		}
	}
}
