package arm32x.minecraft.commandblockide.client.gui;

import arm32x.minecraft.commandblockide.mixin.client.EditBoxAccessor;
import arm32x.minecraft.commandblockide.mixin.client.TextFieldWidgetAccessor;
import arm32x.minecraft.commandblockide.util.OrderedTexts;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CursorMovement;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class MultilineTextFieldWidget extends TextFieldWidget {
	/**
	 * Allows easy and convenient access to private fields in the superclass.
	 */
	private final TextFieldWidgetAccessor self = (TextFieldWidgetAccessor)this;

    // TODO: Allow the user to configure this or to indent with tabs.
    // Note that both the text field renderer and the command processor do not
    // support tabs yet.
	private static final int INDENT_SIZE = 4;

	private final EditBox editBox;

	private boolean horizontalScrollEnabled;
	private int horizontalScroll = 0;
	private boolean verticalScrollEnabled;
	private int verticalScroll = 0;
	public static final double SCROLL_SENSITIVITY = 15.0;

	private int lineHeight = 12;

	private @Nullable Runnable cursorChangeListener = null;

	public MultilineTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text, boolean horizontalScrollEnabled, boolean verticalScrollEnabled) {
		super(textRenderer, x, y, width, height, text);
		this.horizontalScrollEnabled = horizontalScrollEnabled;
		this.verticalScrollEnabled = verticalScrollEnabled;

		// TODO: Support soft wrap.
		editBox = new EditBox(textRenderer, Integer.MAX_VALUE);
	}

	public MultilineTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
		this(textRenderer, x, y, width, height, text, true, true);
		editBox.setCursorChangeListener(() -> {
			scrollToEnsureCursorVisible();
			if (cursorChangeListener != null) {
				cursorChangeListener.run();
			}
		});
	}

	@Override
	public void setChangedListener(@Nullable Consumer<String> changedListener) {
		editBox.setChangeListener(Objects.requireNonNullElseGet(changedListener, () -> text -> {}));
	}

	public void setCursorChangeListener(@Nullable Runnable cursorChangeListener) {
		this.cursorChangeListener = cursorChangeListener;
	}

    @Override
    public void setText(String text) {
        editBox.setText(text);
    }

	@Override
	public String getText() {
        return editBox.getText();
    }

	@Override
	public String getSelectedText() {
        return editBox.getSelectedText();
    }

    @Override
    public void setTextPredicate(Predicate<String> textPredicate) {
        throw new UnsupportedOperationException();
    }

	@Override
	public void write(String text) {
        editBox.replaceSelection(text);
	}

    @Override
    public void eraseWords(int wordOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void eraseCharacters(int characterOffset) {
        editBox.delete(characterOffset);
    }

    @Override
    public int getWordSkipPosition(int wordOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveCursor(int offset) {
        editBox.moveCursor(CursorMovement.RELATIVE, offset);
    }

    private void moveCursor(double mouseX, double mouseY) {
        double virtualX = mouseX - getInnerX() + getHorizontalScroll();
        double virtualY = mouseY - getInnerY() + getVerticalScroll();

		int lineIndex = MathHelper.floor(virtualY / getLineHeight());

		// Get a rough estimate of where the cursor should be.
		EditBox.Substring lineSubstring = editBox.getLine(lineIndex);
		String line = getText().substring(lineSubstring.beginIndex(), lineSubstring.endIndex());
		int charIndexInLine = self.getTextRenderer().trimToWidth(line, MathHelper.floor(virtualX)).length();
		int charIndex = lineSubstring.beginIndex() + charIndexInLine;

		// Refine the estimate by determining the nearest character boundary.
		double leftCharacterXDistance = Math.abs(getCharacterVirtualX(charIndex) - virtualX);
		double rightCharacterXDistance = Math.abs(getCharacterVirtualX(charIndex + 1) - virtualX);
		if (rightCharacterXDistance < leftCharacterXDistance) {
			charIndex++;
		}

		setCursor(charIndex);
    }

    @Override
    public void setCursor(int cursor) {
        editBox.moveCursor(CursorMovement.ABSOLUTE, cursor);
    }

    @Override
    public void setSelectionStart(int cursor) {
        editBox.setSelecting(true);
        setCursor(cursor);
    }

	@Override
	public void setSelectionEnd(int index) {
		((EditBoxAccessor)editBox).setSelectionEnd(index);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_TAB) {
            if (editBox.hasSelection()) {
                logger.warn("Indenting selected lines is not yet supported");
            } else {
                int cursorLeft = getCursor() - getLineStartBefore(getCursor());
                String indent = " ".repeat(4 - cursorLeft % INDENT_SIZE);
                editBox.replaceSelection(indent);
            }
            return true;
        } else {
			return editBox.handleSpecialKey(keyCode);
		}
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isVisible()) {
            return false;
        }
        if (self.isFocusUnlocked()) {
            setFocused(isMouseOver(mouseX, mouseY));
        }
        if (isFocused() && isMouseOver(mouseX, mouseY) && button == 0) {
            editBox.setSelecting(Screen.hasShiftDown());
            moveCursor(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!this.isVisible()) {
            return false;
        }
        if (self.isFocusUnlocked()) {
            setFocused(isMouseOver(mouseX, mouseY));
        }
        if (isFocused() && isMouseOver(mouseX, mouseY) && button == 0) {
            editBox.setSelecting(true);
            moveCursor(mouseX, mouseY);
            editBox.setSelecting(Screen.hasShiftDown());
            return true;
        }
        return false;
    }

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (this.isMouseOver(mouseX, mouseY)) {
			boolean changed = false;
			if (Screen.hasShiftDown() && isHorizontalScrollEnabled()) {
				changed = setHorizontalScroll(getHorizontalScroll() - (int)Math.round(amount * SCROLL_SENSITIVITY));
			} else if (isVerticalScrollEnabled()) {
				changed = setVerticalScroll(getVerticalScroll() - (int)Math.round(amount * SCROLL_SENSITIVITY));
			}
			// This updates the position of the suggestions window.
			if (cursorChangeListener != null) {
				cursorChangeListener.run();
			}
			return changed;
		} else {
			return super.mouseScrolled(mouseX, mouseY, amount);
		}
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (!isVisible()) {
			return;
		}

		if (self.getDrawsBackground()) {
			int borderColor = this.isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0;
			fill(matrices, this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, borderColor);
			fill(matrices, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF000000);
		}

		Window window = MinecraftClient.getInstance().getWindow();
		double scaleFactor = window.getScaleFactor();
		RenderSystem.enableScissor(
			(int)Math.round(this.getX() * scaleFactor),
			// OpenGL coordinates start from the bottom left.
			window.getHeight() - (int)Math.round(this.getY() * scaleFactor + this.height * scaleFactor),
			(int)Math.round(this.width * scaleFactor),
			(int)Math.round(this.height * scaleFactor)
		);

		int textColor = self.isEditable() ? self.getEditableColor() : self.getUneditableColor();
		int x = this.getX() + (self.getDrawsBackground() ? 4 : 0) - horizontalScroll;
		int y = this.getY() + (self.getDrawsBackground() ? 3 : 0) - verticalScroll;

		boolean showCursor = isFocused() && self.getFocusedTicks() / 6 % 2 == 0;
		boolean lineCursor = getCursor() < getText().length() || getText().length() >= self.invokeGetMaxLength();

		int cursorLine = getCurrentLineIndex();
		int cursorX = x - 1;
		int cursorY = y + lineHeight * cursorLine;

		OrderedText text = self.getRenderTextProvider().apply(getText(), 0);
		List<OrderedText> lines = OrderedTexts.split('\n', text);
		for (int index = 0; index < lines.size(); index++) {
			OrderedText line = lines.get(index);
			if (index == cursorLine) {
				int indexOfLastNewlineBeforeCursor = getLineStartBefore(getCursor()) - 1;
				int codePointsBeforeCursor;
				if (indexOfLastNewlineBeforeCursor != -1) {
					codePointsBeforeCursor = getText().codePointCount(indexOfLastNewlineBeforeCursor, Math.max(getCursor() - 1, 0));
				} else {
					codePointsBeforeCursor = getText().codePointCount(0, getCursor());
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

		if (isFocused() && editBox.hasSelection()) {
			renderSelection(matrices, x, y);
		}

		RenderSystem.disableScissor();
	}

	private void renderSelection(MatrixStack matrices, int x, int y) {
		var selection = editBox.getSelection();
		int normalizedSelectionStart = selection.beginIndex();
		int normalizedSelectionEnd = selection.endIndex();

		int startX = x + self.getTextRenderer().getWidth(getText().substring(getLineStartBefore(normalizedSelectionStart), normalizedSelectionStart)) - 1;
		int startY = y + lineHeight * getLineIndex(normalizedSelectionStart) - 1;
		int endX = x + self.getTextRenderer().getWidth(getText().substring(getLineStartBefore(normalizedSelectionEnd), normalizedSelectionEnd)) - 1;
		int endY = y + lineHeight * getLineIndex(normalizedSelectionEnd) - 1;

		int leftEdge = this.getX() + (self.getDrawsBackground() ? 4 : 0);
		int rightEdge = leftEdge + this.getInnerWidth();

		Matrix4f matrix = matrices.peek().getPositionMatrix();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

		if (startY == endY) {
			// Selection spans one line
			bufferBuilder.vertex(matrix, endX, startY, 0.0f).next();
			bufferBuilder.vertex(matrix, startX, startY, 0.0f).next();
			bufferBuilder.vertex(matrix, startX, endY + lineHeight - 1, 0.0f).next();
			bufferBuilder.vertex(matrix, endX, endY + lineHeight - 1, 0.0f).next();
		} else {
			// Selection spans two or more lines
			bufferBuilder.vertex(matrix, rightEdge, startY, 0.0f).next();
			bufferBuilder.vertex(matrix, startX, startY, 0.0f).next();
			bufferBuilder.vertex(matrix, startX, startY + lineHeight, 0.0f).next();
			bufferBuilder.vertex(matrix, rightEdge, startY + lineHeight, 0.0f).next();

			if (!(startY - lineHeight == endY || endY - lineHeight == startY)) {
				// Selection spans three or more lines
				bufferBuilder.vertex(matrix, rightEdge, startY + lineHeight, 0.0f).next();
				bufferBuilder.vertex(matrix, leftEdge, startY + lineHeight, 0.0f).next();
				bufferBuilder.vertex(matrix, leftEdge, endY, 0.0f).next();
				bufferBuilder.vertex(matrix, rightEdge, endY, 0.0f).next();
			}

			bufferBuilder.vertex(matrix, endX, endY, 0.0f).next();
			bufferBuilder.vertex(matrix, leftEdge, endY, 0.0f).next();
			bufferBuilder.vertex(matrix, leftEdge, endY + lineHeight - 1, 0.0f).next();
			bufferBuilder.vertex(matrix, endX, endY + lineHeight - 1, 0.0f).next();
		}

		tessellator.draw();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableColorLogicOp();
	}

    @Override
    public void setMaxLength(int maxLength) {
        editBox.setMaxLength(maxLength);
    }

    @Override
    public int getCursor() {
        return editBox.getCursor();
    }

	public int getLineCount() {
        return editBox.getLineCount();
	}

	public int getCurrentLineIndex() {
		return getLineIndex(getCursor());
	}

	private int getLineIndex(int charIndex) {
		return (int)getText()
			.substring(0, charIndex)
			.codePoints()
			.filter(point -> point == '\n')
			.count();
	}

	private int getLineStartBefore(int charIndex) {
		return getText().lastIndexOf('\n', Math.max(charIndex - 1, 0)) + 1;
	}

    // Naming things is hard.
    public boolean isBeforeFirstNonWhitespaceCharacterInLine(int charIndex) {
        return getText()
            .substring(getLineStartBefore(charIndex), charIndex)
            .chars()
            .allMatch(Character::isWhitespace);
    }

	public String getLine(int lineIndex) {
		var line = editBox.getLine(lineIndex);
		return getText().substring(line.beginIndex(), line.endIndex());
	}

	protected int getHorizontalScroll() {
		return horizontalScroll;
	}

	protected int getMaxHorizontalScroll() {
		return Math.max(0, Arrays.stream(getText().split("\n"))
			.mapToInt(self.getTextRenderer()::getWidth)
			.max()
			.orElse(0) + 8 - width);
	}

	protected boolean setHorizontalScroll(int horizontalScroll) {
		int previous = this.horizontalScroll;
		this.horizontalScroll = MathHelper.clamp(horizontalScroll, 0, getMaxHorizontalScroll());
		return this.horizontalScroll != previous;
	}

	protected int getVerticalScroll() {
		return verticalScroll;
	}

	protected int getMaxVerticalScroll() {
		return Math.max(0, getLineCount() * getLineHeight() + 2 - height);
	}

	protected boolean setVerticalScroll(int verticalScroll) {
		int previous = this.verticalScroll;
		this.verticalScroll = MathHelper.clamp(verticalScroll, 0, getMaxVerticalScroll());
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

	protected void scrollToEnsureCursorVisible() {
		int virtualX = getCharacterVirtualX(getCursor());
		int virtualY = getCharacterVirtualY(getCursor());

		setHorizontalScroll(MathHelper.clamp(horizontalScroll, virtualX - getInnerWidth(), virtualX));
		setVerticalScroll(MathHelper.clamp(verticalScroll, virtualY - getInnerHeight(), virtualY));
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

	public int getCharacterVirtualX(int charIndex) {
		if (charIndex > getText().length()) {
			return 0;
		}
		String line = getLine(getLineIndex(charIndex));

		int indexInLine = charIndex - getLineStartBefore(charIndex);
		if (indexInLine > line.length()) {
			indexInLine = line.length();
		}

		return self.getTextRenderer().getWidth(line.substring(0, indexInLine));
	}

	public int getCharacterRealX(int charIndex) {
		return getInnerX() - horizontalScroll + getCharacterVirtualX(charIndex);
	}

	/**
	 * Gets the desired X position of the {@link ChatInputSuggestor} window.
	 *
	 * <p>This function is marked as deprecated because it <i>does not do what
	 * the method name says</i> and is only here to be called by
	 * {@code ChatInputSuggestor}.</p>
	 *
	 * @param charIndex The index of the character to place the suggestion
	 *                  window at.
	 * @return The desired X position of the suggestion window.
	 */
	@Deprecated
	@Override
	public int getCharacterX(int charIndex) {
		// Since getInnerX isn't a method in the original TextFieldWidget,
		// ChatInputSuggestor calls getCharacterX(0) instead.
		if (charIndex == 0) {
			return getInnerX();
		}
		// Enforce a lower bound on position. ChatInputSuggestor will enforce
		// the upper bound using getInnerWidth().
		return Math.max(getCharacterRealX(charIndex), getInnerX());
	}

	public int getCharacterVirtualY(int charIndex) {
		if (charIndex > getText().length()) {
			charIndex = getText().length();
		}
		int lineIndex = getLineIndex(charIndex);

		return lineIndex * getLineHeight();
	}

	public int getCharacterRealY(int charIndex) {
		return getInnerY() - verticalScroll + getCharacterVirtualY(charIndex);
	}

	private int getInnerX() {
		return this.getX() + (self.getDrawsBackground() ? 4 : 0);
	}

	private int getInnerY() {
		return this.getY() + (self.getDrawsBackground() ? 3 : 0);
	}

	private int getInnerHeight() {
		return self.getDrawsBackground() ? this.height - 6 : this.height;
	}

    private static final Logger logger = LogManager.getLogger();
}
