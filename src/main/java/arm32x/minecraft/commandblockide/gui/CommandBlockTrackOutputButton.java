package arm32x.minecraft.commandblockide.gui;

import java.util.Collections;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public final class CommandBlockTrackOutputButton extends DynamicTexturedButton {
	public boolean trackingOutput = false;

	private final Screen screen;

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
		return trackingOutput ? new TranslatableText("commandBlockIDE.trackingOutput.on") : new TranslatableText("commandBlockIDE.trackingOutput.off");
	}

	@Override
	public void onPress() {
		trackingOutput = !trackingOutput;
	}

	@Override
	public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
		screen.renderOrderedTooltip(matrices, Collections.singletonList(getMessage().asOrderedText()), mouseX, mouseY);
	}
}
