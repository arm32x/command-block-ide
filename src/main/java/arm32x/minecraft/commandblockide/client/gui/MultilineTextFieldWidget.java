package arm32x.minecraft.commandblockide.client.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class MultilineTextFieldWidget extends TextFieldWidget {
	public MultilineTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
		super(textRenderer, x, y, width, height, text);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (isActive()) {
			write(Character.toString(chr));
			return true;
		} else {
			return false;
		}
	}
}
