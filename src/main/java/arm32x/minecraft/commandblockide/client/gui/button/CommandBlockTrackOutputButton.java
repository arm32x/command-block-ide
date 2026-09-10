package arm32x.minecraft.commandblockide.client.gui.button;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

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
			? Identifier.fromNamespaceAndPath("minecraft", "textures/item/writable_book.png")
			: Identifier.fromNamespaceAndPath("minecraft", "textures/item/written_book.png");
	}

	@Override
	public MutableComponent createNarrationMessage() {
		return wrapDefaultNarrationMessage(getTooltipText());
	}

	private Component getTooltipText() {
		return trackingOutput
			? Component.translatable("commandBlockIDE.lastOutput.on")
			: Component.translatable("commandBlockIDE.lastOutput.off");
	}

	@Override
	public void onPress(InputWithModifiers input) {
		trackingOutput = !trackingOutput;
		dirty = true;
		updateTooltip();
	}

	@Override
	public boolean isDirty() { return dirty; }

	private void updateTooltip() {
		setTooltip(Tooltip.create(getTooltipText()));
	}

	public boolean isTrackingOutput() {
		return trackingOutput;
	}

	public void setTrackingOutput(boolean trackingOutput) {
		this.trackingOutput = trackingOutput;
		updateTooltip();
	}
}
