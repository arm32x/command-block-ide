package arm32x.minecraft.commandblockide.gui.widget;

import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import org.jetbrains.annotations.Nullable;

public final class WMarginPanel extends WPanel {
	private @Nullable WWidget child = null;
	private int margin = 4;

	@Override
	public void layout() {
		if (child != null) {
			child.setLocation(margin, margin);
			child.setSize(getWidth() - margin * 2, getHeight() - margin * 2);
			if (child instanceof WPanel) {
				((WPanel)child).layout();
			}
		}
	}

	public @Nullable WWidget getChild() {
		return child;
	}

	public void setChild(@Nullable WWidget child) {
		this.child = child;
		if (children.size() > 0) {
			children.clear();
		}
		if (child != null) {
			children.add(child);
		}
	}

	public void removeChild() {
		setChild(null);
	}

	@Override
	public void remove(WWidget widget) {
		if (widget.equals(child)) {
			removeChild();
		}
	}

	public int getMargin() {
		return margin;
	}

	public void setMargin(int margin) {
		this.margin = margin;
	}
}
