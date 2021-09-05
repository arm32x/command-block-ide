package arm32x.minecraft.commandblockide.client.gui;

import arm32x.minecraft.commandblockide.mixin.client.TextFieldWidgetAccessor;
import arm32x.minecraft.commandblockide.util.OrderedTexts;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import static org.lwjgl.glfw.GLFW.*;

@Environment(EnvType.CLIENT)
public class MultilineTextFieldWidget extends TextFieldWidget {
	/**
	 * Allows easy and convenient access to private fields in the superclass.
	 */
	private final TextFieldWidgetAccessor self = (TextFieldWidgetAccessor)this;

	private static final String INDENT = "    ";

	// TODO: Set and enforce maximum scroll offsets.
	private boolean horizontalScrollEnabled;
	private int horizontalScroll = 0;
	private boolean verticalScrollEnabled;
	private int verticalScroll = 0;
	public static final double SCROLL_SENSITIVITY = 15.0;

	private int lineHeight = 12;

	private int cursorX = -1;

	public MultilineTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text, boolean horizontalScrollEnabled, boolean verticalScrollEnabled) {
		super(textRenderer, x, y, width, height, text);
		this.horizontalScrollEnabled = horizontalScrollEnabled;
		this.verticalScrollEnabled = verticalScrollEnabled;
	}

	public MultilineTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
		this(textRenderer, x, y, width, height, text, true, true);
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
			case GLFW_KEY_DOWN -> {
				moveCursorVertically(1);
				yield true;
			}
			case GLFW_KEY_UP -> {
				moveCursorVertically(-1);
				yield true;
			}
			case GLFW_KEY_TAB -> {
				if (Screen.hasShiftDown()) {
					int index = getCursorLine();
					String line = getLine(index);
					if (line.startsWith(INDENT)) {
						int selectionStart = self.getSelectionStart();
						int selectionEnd = self.getSelectionEnd();
						setLine(index, line.substring(INDENT.length()));
						setSelectionStart(selectionStart - INDENT.length());
						setSelectionEnd(selectionEnd - INDENT.length());
					}
				} else {
					write(INDENT);
				}
				yield true;
			}
			default -> super.keyPressed(keyCode, scanCode, modifiers);
		};
	}

	@Override
	public void write(String text) {
		int normalizedSelectionStart = Math.min(self.getSelectionStart(), self.getSelectionEnd());
		int normalizedSelectionEnd = Math.max(self.getSelectionStart(), self.getSelectionEnd());
		int remainingLength = self.invokeGetMaxLength() - getText().length() - (normalizedSelectionStart - normalizedSelectionEnd);
		int length = text.length();
		if (remainingLength < length) {
			text = text.substring(0, remainingLength);
			length = remainingLength;
		}

		String newText = new StringBuilder(getText())
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
		boolean lineCursor = self.getSelectionStart() < getText().length() || getText().length() >= self.invokeGetMaxLength();

		int cursorLine = getCursorLine();
		int cursorX = x;
		int cursorY = y + lineHeight * cursorLine;

		OrderedText text = self.getRenderTextProvider().apply(getText(), 0);
		List<OrderedText> lines = OrderedTexts.split('\n', text);
		for (int index = 0; index < lines.size(); index++) {
			OrderedText line = lines.get(index);
			if (index == cursorLine) {
				int indexOfLastNewlineBeforeCursor = getText().lastIndexOf('\n', Math.max(self.getSelectionStart() - 1, 0));
				int codePointsBeforeCursor;
				if (indexOfLastNewlineBeforeCursor != -1) {
					codePointsBeforeCursor = getText().codePointCount(indexOfLastNewlineBeforeCursor, Math.max(self.getSelectionStart() - 1, 0));
				} else {
					codePointsBeforeCursor = getText().codePointCount(0, self.getSelectionStart());
				}
				int endX = self.getTextRenderer().drawWithShadow(matrices, OrderedTexts.limit(codePointsBeforeCursor, line), x, y + lineHeight * index, textColor) - 1;
				self.getTextRenderer().drawWithShadow(matrices, OrderedTexts.skip(codePointsBeforeCursor, line), endX, y + lineHeight * index, textColor);
				cursorX = endX - 1;
			} else {
				self.getTextRenderer().drawWithShadow(matrices, line, x, y + lineHeight * index, textColor);
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

	public int getLineCount() {
		return (int)getText()
			.codePoints()
			.filter(point -> point == '\n')
			.count() + 1;
	}

	public int getCursorLine() {
		return (int)getText()
			.substring(0, self.getSelectionStart())
			.codePoints()
			.filter(point -> point == '\n')
			.count();
	}

	public String getLine(int index) {
		int lineStart = StringUtils.ordinalIndexOf(getText(), "\n", index) + 1;
		int lineEnd = getText().indexOf('\n', lineStart);
		if (lineEnd == -1) {
			lineEnd = getText().length();
		}
		return getText().substring(lineStart, lineEnd);
	}

	public void setLine(int index, String line) {
		int lineStart = StringUtils.ordinalIndexOf(getText(), "\n", index) + 1;
		int lineEnd = getText().indexOf('\n', lineStart);
		if (lineEnd == -1) {
			lineEnd = getText().length();
		}

		StringBuilder builder = new StringBuilder(getText());
		builder.replace(lineStart, lineEnd, line);
		setText(builder.toString());
	}

	protected int getHorizontalScroll() {
		return horizontalScroll;
	}

	protected boolean setHorizontalScroll(int horizontalScroll) {
		int previous = this.horizontalScroll;
		int max = Math.max(0, Arrays.stream(getText().split("\n"))
			.mapToInt(self.getTextRenderer()::getWidth)
			.max()
			.orElse(0) + 8 - width);
		this.horizontalScroll = MathHelper.clamp(horizontalScroll, 0, max);
		return this.horizontalScroll != previous;
	}

	protected int getVerticalScroll() {
		return verticalScroll;
	}

	protected boolean setVerticalScroll(int verticalScroll) {
		int previous = this.verticalScroll;
		int max = Math.max(0, getLineCount() * getLineHeight() + 2 - height);
		this.verticalScroll = MathHelper.clamp(verticalScroll, 0, max);
		return this.verticalScroll != previous;
	}

	public boolean isHorizontalScrollEnabled() {
		return horizontalScrollEnabled;
	}

	public void setHorizontalScrollEnabled(boolean enabled) {
		horizontalScrollEnabled = enabled;
		horizontalScroll = 0;
	}

	public boolean isVerticalScrollEnabled() {
		return verticalScrollEnabled;
	}

	public void setVerticalScrollEnabled(boolean enabled) {
		verticalScrollEnabled = enabled;
		verticalScroll = 0;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.isVisible()) {
			return false;
		}

		if (self.isFocusUnlocked()) {
			setTextFieldFocused(isMouseOver(mouseX, mouseY));
		}

		if (isFocused() && isMouseOver(mouseX, mouseY) && button == 0) {
			int lineIndex = MathHelper.clamp(MathHelper.floor((mouseY - y - 2) / getLineHeight()), 0, getLineCount() - 1);
			int lineStart = StringUtils.ordinalIndexOf(getText(), "\n", lineIndex) + 1;
			int lineEnd = getText().indexOf('\n', lineStart);
			if (lineEnd == -1) {
				lineEnd = getText().length();
			}
			String line = getText().substring(lineStart, lineEnd);

			setCursor(self.getTextRenderer().trimToWidth(line, MathHelper.floor(mouseX) - this.x - (self.getDrawsBackground() ? 4 : 0) + 2).length() + lineStart);
			return true;
		}

		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (this.isMouseOver(mouseX, mouseY)) {
			if (Screen.hasShiftDown()) {
				if (isHorizontalScrollEnabled()) {
					return setHorizontalScroll(getHorizontalScroll() - (int)Math.round(amount * SCROLL_SENSITIVITY));
				}
			} else {
				if (isVerticalScrollEnabled()) {
					return setVerticalScroll(getVerticalScroll() - (int)Math.round(amount * SCROLL_SENSITIVITY));
				}
			}
			return false;
		} else {
			return super.mouseScrolled(mouseX, mouseY, amount);
		}
	}

	@Override
	public void setCursor(int cursor) {
		super.setCursor(cursor);
		cursorX = -1;
	}

	public void moveCursorVertically(int lines) {
		int currentLineStart = getText().lastIndexOf('\n', getCursor());
		int targetLine = MathHelper.clamp(getCursorLine() + lines, 0, getLineCount());
		int lineStart = StringUtils.ordinalIndexOf(getText(), "\n", targetLine) + 1;
		int lineEnd = getText().indexOf('\n', lineStart);
		if (lineEnd == -1) {
			lineEnd = getText().length();
		}

		if (cursorX == -1) {
			cursorX = self.getTextRenderer().getWidth(getText().substring(currentLineStart, getCursor()));
		}
		int indexInTargetLine = self.getTextRenderer().trimToWidth(getText().substring(lineStart, lineEnd), cursorX - 2).length() - 1;
		int prevCursorX = cursorX;
		setCursor(lineStart + indexInTargetLine);
		cursorX = prevCursorX;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	public void setLineHeight(int lineHeight) {
		this.lineHeight = lineHeight;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
