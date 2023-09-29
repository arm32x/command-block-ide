package arm32x.minecraft.commandblockide.client.gui.button;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class CommandBlockAutoButton extends IconButton implements Dirtyable {
	private boolean auto = false;

	private boolean dirty = false;

	public CommandBlockAutoButton(int x, int y) {
		super(x, y, 16, 16);
		setTooltip(Tooltip.of(getTooltipText()));
	}

	@Override
	protected Identifier getTexture() {
		return auto
			? new Identifier("minecraft", "textures/item/gunpowder.png")
			: new Identifier("minecraft", "textures/item/redstone.png");
	}

	@Override
	public MutableText getNarrationMessage() {
		return getNarrationMessage(getTooltipText());
	}

	private Text getTooltipText() {
		return auto
			? Text.translatable("advMode.mode.autoexec.bat")
			: Text.translatable("advMode.mode.redstoneTriggered");
	}

	@Override
	public void onPress() {
		auto = !auto;
		dirty = true;
		updateTooltip();
	}

	@Override
	public boolean isDirty() { return dirty; }

	public boolean isAuto() {
		return auto;
	}

	public void setAuto(boolean auto) {
		this.auto = auto;
		updateTooltip();
	}

	private void updateTooltip() {
		setTooltip(Tooltip.of(getTooltipText()));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
}
