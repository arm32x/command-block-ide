package arm32x.minecraft.commandblockide.client.gui;

import arm32x.minecraft.commandblockide.mixin.client.TextFieldWidgetAccessor;
import arm32x.minecraft.commandblockide.util.OrderedTexts;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import static org.lwjgl.glfw.GLFW.*;

@Environment(EnvType.CLIENT)
public class MultilineTextFieldWidget extends TextFieldWidget {
	/**
	 * Allows easy and convenient access to private fields in the superclass.
	 */
	private final TextFieldWidgetAccessor self = (TextFieldWidgetAccessor)this;

	// TODO: Set and enforce maximum scroll offsets.
	private int horizontalScroll = 0;
	private int verticalScroll = 0;
	public static final double SCROLL_SENSITIVITY = 15.0;

	private int lineSpacing = 12;

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
		int remainingLength = self.invokeGetMaxLength() - self.getText().length() - (normalizedSelectionStart - normalizedSelectionEnd);
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
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (!isVisible()) {
			return;
		}

		if (self.getDrawsBackground()) {
			int borderColor = this.isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0;
			fill(matrices, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, borderColor);
			fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
		}

		Window window = MinecraftClient.getInstance().getWindow();
		double scaleFactor = window.getScaleFactor();
		RenderSystem.enableScissor(
			(int)Math.round(this.x * scaleFactor),
			// OpenGL coordinates start from the bottom left.
			window.getHeight() - (int)Math.round(this.y * scaleFactor + this.height * scaleFactor),
			(int)Math.round(this.width * scaleFactor),
			(int)Math.round(this.height * scaleFactor)
		);

		int textColor = self.isEditable() ? self.getEditableColor() : self.getUneditableColor();
		int x = this.x + (self.getDrawsBackground() ? 4 : 0) - horizontalScroll;
		int y = this.y + (self.getDrawsBackground() ? 3 : 0) - verticalScroll;

		boolean showCursor = isFocused() && self.getFocusedTicks() / 6 % 2 == 0;
		boolean lineCursor = self.getSelectionStart() < self.getText().length() || self.getText().length() >= self.invokeGetMaxLength();

		int cursorLine = (int)self.getText()
			.substring(0, self.getSelectionStart())
			.codePoints()
			.filter(point -> point == '\n')
			.count();

		int cursorX = x;
		int cursorY = y + lineSpacing * cursorLine;

		OrderedText text = self.getRenderTextProvider().apply(self.getText(), 0);
		List<OrderedText> lines = OrderedTexts.split('\n', text);
		for (int index = 0; index < lines.size(); index++) {
			OrderedText line = lines.get(index);
			if (index == cursorLine) {
				int indexOfLastNewlineBeforeCursor = self.getText().lastIndexOf('\n', Math.max(self.getSelectionStart() - 1, 0));
				int codePointsBeforeCursor;
				if (indexOfLastNewlineBeforeCursor != -1) {
					codePointsBeforeCursor = self.getText().codePointCount(indexOfLastNewlineBeforeCursor, Math.max(self.getSelectionStart() - 1, 0));
				} else {
					codePointsBeforeCursor = self.getText().codePointCount(0, self.getSelectionStart());
				}
				int endX = self.getTextRenderer().drawWithShadow(matrices, OrderedTexts.limit(codePointsBeforeCursor, line), x, y + lineSpacing * index, textColor) - 1;
				self.getTextRenderer().drawWithShadow(matrices, OrderedTexts.skip(codePointsBeforeCursor, line), endX, y + lineSpacing * index, textColor);
				cursorX = endX - 1;
			} else {
				self.getTextRenderer().drawWithShadow(matrices, line, x, y + lineSpacing * index, textColor);
			}
		}

		if (showCursor) {
			if (lineCursor) {
				fill(matrices, cursorX, cursorY - 1, cursorX + 1, cursorY + 10, 0xFFD0D0D0);
			} else {
				self.getTextRenderer().drawWithShadow(matrices, "_", cursorX + 1, cursorY, textColor);
			}
		}

		// TODO: Draw selection.

		RenderSystem.disableScissor();
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

	public int getHorizontalScroll() {
		return horizontalScroll;
	}

	public void setHorizontalScroll(int horizontalScroll) {
		this.horizontalScroll = horizontalScroll;
	}

	public int getVerticalScroll() {
		return verticalScroll;
	}

	public void setVerticalScroll(int verticalScroll) {
		this.verticalScroll = verticalScroll;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (this.isMouseOver(mouseX, mouseY)) {
			if (Screen.hasShiftDown()) {
				setHorizontalScroll(getHorizontalScroll() - (int)Math.round(amount * SCROLL_SENSITIVITY));
			} else {
				setVerticalScroll(getVerticalScroll() - (int)Math.round(amount * SCROLL_SENSITIVITY));
			}
			return true;
		} else {
			return super.mouseScrolled(mouseX, mouseY, amount);
		}
	}

	public int getLineSpacing() {
		return lineSpacing;
	}

	public void setLineSpacing(int lineSpacing) {
		this.lineSpacing = lineSpacing;
	}
}
