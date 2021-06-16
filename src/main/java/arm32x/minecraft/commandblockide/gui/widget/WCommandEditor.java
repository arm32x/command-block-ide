package arm32x.minecraft.commandblockide.gui.widget;

import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Axis;

public abstract class WCommandEditor extends WBorderPanel {
	private int lineNumber;

	private final WBox leftBox;
	private final WBox rightBox;

	public WCommandEditor(int lineNumber) {
		this.lineNumber = lineNumber;
		setSpacing(8);

		WTextField commandField = new WTextField();
		super.add(commandField, Position.CENTER);

		leftBox = new WBox(Axis.HORIZONTAL);
		super.add(leftBox, Position.LEFT);
		leftBox.setSpacing(8);

		rightBox = new WBox(Axis.VERTICAL);
		super.add(rightBox, Position.RIGHT);
		rightBox.setSpacing(8);
	}

	@Override
	public void add(WWidget widget, Position position) {
		add(widget, position, 20);
	}

	public void add(WWidget widget, Position position, int width) {
		switch (position) {
			case LEFT:
				leftBox.add(widget, width, 20);
				break;
			case RIGHT:
				rightBox.add(widget, width, 20);
				break;
			default:
				throw new UnsupportedOperationException("WCommandEditor only supports adding widgets with Position.LEFT and Position.RIGHT.");
		}
	}

	@Override
	public void remove(WWidget widget) {
		leftBox.remove(widget);
		rightBox.remove(widget);
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
}
