package arm32x.minecraft.commandblockide.client.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public abstract class Container extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
	protected final List<GuiEventListener> children = new ArrayList<>();
	protected final List<NarratableEntry> selectables = new ArrayList<>();
	protected final List<Renderable> drawables = new ArrayList<>();

	protected <T extends GuiEventListener & NarratableEntry & Renderable> T addDrawableChild(T child) {
		drawables.add(child);
		return addSelectableChild(child);
	}

	protected <T extends Renderable> T addDrawable(T drawable) {
		drawables.add(drawable);
		return drawable;
	}

	protected <T extends GuiEventListener & NarratableEntry> T addSelectableChild(T child) {
		children.add(child);
		selectables.add(child);
		return child;
	}

	protected void remove(GuiEventListener child) {
		if (child instanceof Renderable) {
			drawables.remove(child);
		}
		if (child instanceof NarratableEntry) {
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
	public NarrationPriority narrationPriority() {
		return selectables.stream()
			.map(NarratableEntry::narrationPriority)
			.max(Comparator.naturalOrder())
			.orElse(NarrationPriority.NONE);
	}

	@Override
	public List<? extends GuiEventListener> children() { return children; }

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		for (Renderable drawable : drawables) {
			drawable.render(context, mouseX, mouseY, delta);
		}
	}
}
