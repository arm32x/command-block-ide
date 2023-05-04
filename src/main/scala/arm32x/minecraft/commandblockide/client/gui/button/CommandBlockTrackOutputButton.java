package arm32x.minecraft.commandblockide.client.gui.button;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class CommandBlockTrackOutputButton extends IconButton implements Dirtyable {
	private boolean trackingOutput = false;

	private boolean dirty = false;

	public CommandBlockTrackOutputButton(int x, int y) {
		super(x, y, 16, 16);
		updateTooltip();
	}

	@Override
	protected Identifier getTexture() {
		return trackingOutput
			? new Identifier("minecraft", "textures/item/writable_book.png")
			: new Identifier("minecraft", "textures/item/written_book.png");
	}

	@Override
	public MutableText getNarrationMessage() {
		return getNarrationMessage(getTooltipText());
	}

	private Text getTooltipText() {
		return trackingOutput
			? Text.translatable("commandBlockIDE.lastOutput.on")
			: Text.translatable("commandBlockIDE.lastOutput.off");
	}

	@Override
	public void onPress() {
		trackingOutput = !trackingOutput;
		dirty = true;
		updateTooltip();
	}

	@Override
	public boolean isDirty() { return dirty; }

	private void updateTooltip() {
		setTooltip(Tooltip.of(getTooltipText()));
	}

	public boolean isTrackingOutput() {
		return trackingOutput;
	}

	public void setTrackingOutput(boolean trackingOutput) {
		this.trackingOutput = trackingOutput;
		updateTooltip();
	}
}
