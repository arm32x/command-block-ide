package arm32x.minecraft.commandblockide.client.gui;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import java.util.Collections;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public final class CommandBlockTrackOutputButton extends DynamicTexturedButton implements Dirtyable {
	public boolean trackingOutput = false;

	private final Screen screen;

	private boolean dirty = false;

	public CommandBlockTrackOutputButton(Screen screen, int x, int y) {
		super(x, y, 16, 16, 0, 0, 16, 16);
		this.screen = screen;
	}

	@Override
	protected Identifier getTexture() {
		return trackingOutput ? new Identifier("minecraft", "textures/item/writable_book.png") : new Identifier("minecraft", "textures/item/written_book.png");
	}

	@Override
	public Text getMessage() {
		return trackingOutput ? new TranslatableText("commandBlockIDE.lastOutput.on") : new TranslatableText("commandBlockIDE.lastOutput.off");
	}

	@Override
	public void onPress() {
		trackingOutput = !trackingOutput;
		markDirty();
	}

	@Override
	public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
		screen.renderOrderedTooltip(matrices, Collections.singletonList(getMessage().asOrderedText()), mouseX, mouseY);
	}

	@Override
	public boolean isDirty() { return dirty; }

	@Override
	public void markDirty() { dirty = true; }
}
