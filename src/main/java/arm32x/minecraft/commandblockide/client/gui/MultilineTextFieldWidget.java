package arm32x.minecraft.commandblockide.client.gui;

import arm32x.minecraft.commandblockide.mixin.client.TextFieldWidgetAccessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.*;

public class MultilineTextFieldWidget extends TextFieldWidget {
	/**
	 * Allows easy and convenient access to private fields in the superclass.
	 */
	private final TextFieldWidgetAccessor self = (TextFieldWidgetAccessor)this;

	public MultilineTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
		super(textRenderer, x, y, width, height, text);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return switch (keyCode) {
			case GLFW_KEY_ENTER, GLFW_KEY_KP_ENTER -> {
				if (Screen.hasShiftDown()) {
					write("\n");
					yield true;
				} else {
					yield false;
				}
			}
			default -> super.keyPressed(keyCode, scanCode, modifiers);
		};
	}

	@Override
	public void write(String text) {
		int normalizedSelectionStart = Math.min(self.getSelectionStart(), self.getSelectionEnd());
		int normalizedSelectionEnd = Math.max(self.getSelectionStart(), self.getSelectionEnd());
		int remainingLength = self.getMaxLength() - self.getText().length() - (normalizedSelectionStart - normalizedSelectionEnd);
		int length = text.length();
		if (remainingLength < length) {
			text = text.substring(0, remainingLength);
			length = remainingLength;
		}

		String newText = new StringBuilder(self.getText())
			.replace(normalizedSelectionStart, normalizedSelectionEnd, text)
			.toString();
		if (self.getTextPredicate().test(newText)) {
			this.setText(newText);
			this.setSelectionStart(normalizedSelectionStart + length);
			this.setSelectionEnd(self.getSelectionStart());
			self.invokeOnChanged(this.getText());
		}
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
