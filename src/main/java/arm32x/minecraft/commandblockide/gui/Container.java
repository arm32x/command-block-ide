package arm32x.minecraft.commandblockide.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Container extends AbstractParentElement implements Drawable, Element {
	protected final List<Element> children = new ArrayList<>();
	protected final List<AbstractButtonWidget> buttons = new ArrayList<>();

	protected <T extends Element> T addChild(T child) {
		children.add(child);
		return child;
	}

	protected <T extends AbstractButtonWidget> T addButton(T button) {
		buttons.add(button);
		return addChild(button);
	}

	@Override
	public List<? extends Element> children() { return children; }

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		for (AbstractButtonWidget button : buttons) {
			button.render(matrices, mouseX, mouseY, delta);
		}
	}
}
