package arm32x.minecraft.commandblockide.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;

public abstract class Container extends AbstractParentElement implements Drawable, Element {
	protected final List<Element> children = new ArrayList<>();

	@SuppressWarnings("UnusedReturnValue")
	protected <T extends Element> T addChild(T child) {
		children.add(child);
		return child;
	}

	@Override
	public List<? extends Element> children() {
		return children;
	}
}
