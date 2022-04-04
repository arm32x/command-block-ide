package arm32x.minecraft.commandblockide.client.gui.button;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import java.util.Collections;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public final class CommandBlockAutoButton extends IconButton implements Dirtyable {
	public boolean auto = false;

	private final Screen screen;

	private boolean dirty = false;

	public CommandBlockAutoButton(Screen screen, int x, int y) {
		super(x, y, 16, 16);
		this.screen = screen;
	}

	@Override
	protected Identifier getTexture() {
		return auto
			? new Identifier("minecraft", "textures/item/gunpowder.png")
			: new Identifier("minecraft", "textures/item/redstone.png");
	}

	@Override
	public MutableText getNarrationMessage() {
		return getNarrationMessage(getTooltip());
	}

	private Text getTooltip() {
		return auto
			? new TranslatableText("advMode.mode.autoexec.bat")
			: new TranslatableText("advMode.mode.redstoneTriggered");
	}

	@Override
	public void onPress() {
		auto = !auto;
		dirty = true;
	}

	@Override
	public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
		screen.renderOrderedTooltip(matrices, Collections.singletonList(getTooltip().asOrderedText()), mouseX, mouseY);
	}

	@Override
	public boolean isDirty() { return dirty; }
}
