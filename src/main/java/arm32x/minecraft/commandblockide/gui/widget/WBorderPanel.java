package arm32x.minecraft.commandblockide.gui.widget;

import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import java.util.HashMap;
import java.util.Map;

public class WBorderPanel extends WPanel {
	public enum Position {
		TOP,
		BOTTOM,
		LEFT,
		RIGHT,
		CENTER
	}

	private final Map<Position, WWidget> childrenByPosition = new HashMap<>();

	private int spacing = 0;

	@Override
	public void layout() {
		int[] usedSpace = { 0, 0, 0, 0 };
		for (Position position : Position.values()) {
			layout(position, usedSpace);
		}
	}

	private void layout(Position position, int[] usedSpace) {
		WWidget child = childrenByPosition.get(position);
		if (child == null) {
			return;
		}

		switch (position) {
			case TOP:
				child.setLocation(0, 0);
				child.setSize(getWidth(), child.getHeight());
				usedSpace[0] += child.getHeight() + spacing;
				break;
			case BOTTOM:
				child.setLocation(0, getHeight() - child.getHeight());
				child.setSize(getWidth(), child.getHeight());
				usedSpace[1] += child.getHeight() + spacing;
				break;
			case LEFT:
				child.setLocation(0, usedSpace[0]);
				child.setSize(child.getWidth(), getHeight() - usedSpace[0] - usedSpace[1]);
				usedSpace[2] += child.getWidth() + spacing;
				break;
			case RIGHT:
				child.setLocation(getWidth() - child.getWidth(), usedSpace[0]);
				child.setSize(child.getWidth(), getHeight() - usedSpace[0] - usedSpace[1]);
				usedSpace[3] += child.getWidth() + spacing;
				break;
			case CENTER:
				child.setLocation(usedSpace[2], usedSpace[0]);
				child.setSize(getWidth() - usedSpace[2] - usedSpace[3], getHeight() - usedSpace[0] - usedSpace[1]);
				break;
		}

		if (child instanceof WPanel) {
			((WPanel)child).layout();
		}
	}

	public void add(WWidget widget, Position position) {
		WWidget oldWidget = childrenByPosition.put(position, widget);
		if (oldWidget != null) {
			children.remove(oldWidget);
		}
		widget.setParent(this);
		children.add(widget);
	}

	@Override
	public void remove(WWidget widget) {
		children.remove(widget);
		childrenByPosition.values().remove(widget);
	}

	public int getSpacing() {
		return spacing;
	}

	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}
}
