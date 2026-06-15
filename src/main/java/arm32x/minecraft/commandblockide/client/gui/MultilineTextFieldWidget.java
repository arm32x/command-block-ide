package arm32x.minecraft.commandblockide.client.gui;

import arm32x.minecraft.commandblockide.mixin.client.MultilineTextFieldAccessor;
import arm32x.minecraft.commandblockide.mixin.client.EditBoxAccessor;
import arm32x.minecraft.commandblockide.util.OrderedTexts;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultilineTextField;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class MultilineTextFieldWidget extends EditBox {
	/**
	 * Allows easy and convenient access to private fields in the superclass.
	 */
	private final EditBoxAccessor self = (EditBoxAccessor)this;

    // TODO: Allow the user to configure this or to indent with tabs.
    // Note that both the text field renderer and the command processor do not
    // support tabs yet.
	private static final int INDENT_SIZE = 4;

	// The amount of time the cursor will spend being either visible or
	// invisible before switching to the other state.
    private static final long CURSOR_BLINK_INTERVAL_MS = 300;

	private final MultilineTextField editBox;

	private boolean horizontalScrollEnabled;
	private int horizontalScroll = 0;
	private boolean verticalScrollEnabled;
	private int verticalScroll = 0;
	public static final double SCROLL_SENSITIVITY = 15.0;

	private int lineHeight = 12;
	private SyntaxHighlighter syntaxHighlighter = SyntaxHighlighter.NONE;

	private @Nullable Runnable cursorChangeListener = null;

	public MultilineTextFieldWidget(Font textRenderer, int x, int y, int width, int height, Component text, boolean horizontalScrollEnabled, boolean verticalScrollEnabled) {
		super(textRenderer, x, y, width, height, text);
		this.horizontalScrollEnabled = horizontalScrollEnabled;
		this.verticalScrollEnabled = verticalScrollEnabled;

		// TODO: Support soft wrap.
		editBox = new MultilineTextField(textRenderer, Integer.MAX_VALUE);
	}

	public MultilineTextFieldWidget(Font textRenderer, int x, int y, int width, int height, Component text) {
		this(textRenderer, x, y, width, height, text, true, true);
		editBox.setCursorListener(() -> {
			scrollToEnsureCursorVisible();
			if (cursorChangeListener != null) {
				cursorChangeListener.run();
			}
		});
	}

	@Override
	public void setResponder(@Nullable Consumer<String> changedListener) {
		editBox.setValueListener(Objects.requireNonNullElseGet(changedListener, () -> text -> {}));
	}

	public void setCursorChangeListener(@Nullable Runnable cursorChangeListener) {
		this.cursorChangeListener = cursorChangeListener;
	}

    @Override
    public void setValue(String text) {
        editBox.setValue(text);
    }

	@Override
	public String getValue() {
        return editBox.value();
    }

	@Override
	public String getHighlighted() {
        return editBox.getSelectedText();
    }

	@Override
    @Deprecated
	public void addFormatter(EditBox.TextFormatter formatter) {
		// Do nothing, since we use our own syntax highlighting system. I would
        // love to throw an UnsupportedOperationException, but this is called by
        // ChatInputSuggestor.
	}

    public SyntaxHighlighter getSyntaxHighlighter() {
        return syntaxHighlighter;
    }

    public void setSyntaxHighlighter(SyntaxHighlighter syntaxHighlighter) {
        this.syntaxHighlighter = syntaxHighlighter;
    }

	@Override
	public void insertText(String text) {
        editBox.insertText(text);
	}

    @Override
    public void deleteWords(int wordOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteChars(int characterOffset) {
        editBox.deleteText(characterOffset);
    }

    @Override
    public int getWordPosition(int wordOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveCursor(int offset, boolean hasShiftDown) {
		editBox.setSelecting(hasShiftDown);
        editBox.seekCursor(Whence.RELATIVE, offset);
    }

    private void moveCursor(double mouseX, double mouseY, boolean hasShiftDown) {
        double virtualX = mouseX - getInnerX() + getHorizontalScroll();
        double virtualY = mouseY - getInnerY() + getVerticalScroll();

		int lineIndex = Mth.floor(virtualY / getLineHeight());

		// Get a rough estimate of where the cursor should be.
		MultilineTextField.StringView lineSubstring = editBox.getLineView(lineIndex);
		String line = getValue().substring(lineSubstring.beginIndex(), lineSubstring.endIndex());
		int charIndexInLine = self.getFont().plainSubstrByWidth(line, Mth.floor(virtualX)).length();
		int charIndex = lineSubstring.beginIndex() + charIndexInLine;

		// Refine the estimate by determining the nearest character boundary.
		double leftCharacterXDistance = Math.abs(getCharacterVirtualX(charIndex) - virtualX);
		double rightCharacterXDistance = Math.abs(getCharacterVirtualX(charIndex + 1) - virtualX);
		if (rightCharacterXDistance < leftCharacterXDistance) {
			charIndex++;
		}

		moveCursorTo(charIndex, hasShiftDown);
    }

    @Override
    public void moveCursorTo(int cursor, boolean hasShiftDown) {
		editBox.setSelecting(hasShiftDown);
        editBox.seekCursor(Whence.ABSOLUTE, cursor);
    }

    @Override
    public void setCursorPosition(int cursor) {
		moveCursorTo(cursor, true);
    }

	@Override
	public void setHighlightPos(int index) {
		((MultilineTextFieldAccessor)editBox).setSelectCursor(index);
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		if (input.key() == GLFW.GLFW_KEY_TAB) {
            if (editBox.hasSelection()) {
                logger.warn("Indenting selected lines is not yet supported");
            } else {
                int cursorLeft = getCursorPosition() - getLineStartBefore(getCursorPosition());
                String indent = " ".repeat(4 - cursorLeft % INDENT_SIZE);
                editBox.insertText(indent);
            }
            return true;
        } else {
			return editBox.keyPressed(input);
		}
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (!this.isVisible()) {
            return false;
        }
        if (self.isCanLoseFocus()) {
            setFocused(isMouseOver(click.x(), click.y()));
        }
        if (isFocused() && isMouseOver(click.x(), click.y()) && click.button() == 0) {
            moveCursor(click.x(), click.y(), click.hasShiftDown());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (!this.isVisible()) {
            return false;
        }
        if (self.isCanLoseFocus()) {
            setFocused(isMouseOver(click.x(), click.y()));
        }
        if (isFocused() && isMouseOver(click.x(), click.y()) && click.button() == 0) {
            moveCursor(click.x(), click.y(), true);
            editBox.setSelecting(click.hasShiftDown());
            return true;
        }
        return false;
    }

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.isMouseOver(mouseX, mouseY)) {
			boolean changed = setHorizontalScroll(getHorizontalScroll() - (int)Math.round(horizontalAmount * SCROLL_SENSITIVITY));
			changed = changed || setVerticalScroll(getVerticalScroll() - (int)Math.round(verticalAmount * SCROLL_SENSITIVITY));

			// This updates the position of the suggestions window.
			if (cursorChangeListener != null) {
				cursorChangeListener.run();
			}
			return changed;
		} else {
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		if (!isVisible()) {
			return;
		}

		if (isBordered()) {
			var textureId = EditBoxAccessor.getTextures().get(isActive(), isFocused());
			context.blitSprite(RenderPipelines.GUI_TEXTURED, textureId, getX(), getY(), getWidth(), getHeight());
		}

		context.enableScissor(
			this.getX() + 1,
			this.getY() + 1,
			this.getX() + this.getWidth() - 1,
			this.getY() + this.getHeight() - 1
		);

		int textColor = self.invokeIsEditable() ? self.getTextColor() : self.getTextColorUneditable();
		int x = getInnerX() - horizontalScroll;
		int y = getInnerY() - verticalScroll;

		long timeSinceLastSwitchFocusMs = Util.getMillis() - self.getFocusedTime();
        boolean showCursor = isFocused() && timeSinceLastSwitchFocusMs / CURSOR_BLINK_INTERVAL_MS % 2 == 0;
		boolean lineCursor = getCursorPosition() < getValue().length() || getValue().length() >= self.invokeGetMaxLength();

		int cursorLine = getCurrentLineIndex();
		int cursorY = y + lineHeight * cursorLine;

		List<FormattedCharSequence> lines = getSyntaxHighlighter().highlight(getValue());
		for (int index = 0; index < lines.size(); index++) {
			FormattedCharSequence line = lines.get(index);
            context.text(self.getFont(), line, x, y + lineHeight * index, textColor);
		}

		if (showCursor) {
            // Figure out the cursor X position by measuring the text before it.
            // This assumes that the highlighter returns the same characters as
            // the original text, which is not enforced by the API.
            int indexOfLastNewlineBeforeCursor = getLineStartBefore(getCursorPosition()) - 1;
            int codePointsBeforeCursor;
            if (indexOfLastNewlineBeforeCursor != -1) {
                codePointsBeforeCursor = getValue().codePointCount(indexOfLastNewlineBeforeCursor, Math.max(getCursorPosition() - 1, 0));
            } else {
                codePointsBeforeCursor = getValue().codePointCount(0, getCursorPosition());
            }
            FormattedCharSequence textBeforeCursor = OrderedTexts.limit(codePointsBeforeCursor, lines.get(cursorLine));
            int cursorX = x + self.getFont().width(textBeforeCursor) - 1;

			if (lineCursor) {
				context.fill(cursorX, cursorY - 1, cursorX + 1, cursorY + 10, 0xFFD0D0D0);
			} else {
				context.text(self.getFont(), "_", cursorX + 1, cursorY, textColor);
			}
		}

		if (isFocused() && editBox.hasSelection()) {
			renderSelection(context, x, y);
		}

		context.disableScissor();

        if (isHovered()) {
            context.requestCursor(CursorTypes.IBEAM);
        }
	}

	private void renderSelection(GuiGraphicsExtractor context, int x, int y) {
        var selection = editBox.getSelected();
        int normalizedSelectionStart = selection.beginIndex();
        int normalizedSelectionEnd = selection.endIndex();

        int startX = x + self.getFont().width(getValue().substring(getLineStartBefore(normalizedSelectionStart), normalizedSelectionStart)) - 1;
        int startY = y + lineHeight * getLineIndex(normalizedSelectionStart) - 1;
        int endX = x + self.getFont().width(getValue().substring(getLineStartBefore(normalizedSelectionEnd), normalizedSelectionEnd)) - 1;
        int endY = y + lineHeight * getLineIndex(normalizedSelectionEnd) - 1;

        int leftEdge = getInnerX() - 1;
        int rightEdge = getInnerX() + this.getInnerWidth() + 1;

        if (startY == endY) {
            // Selection spans one line
            context.textHighlight(startX, startY, endX, endY + lineHeight - 1, true);
        } else {
            // Selection spans two or more lines
            context.textHighlight(startX, startY, rightEdge, startY + lineHeight, true);
            if (!(startY - lineHeight == endY || endY - lineHeight == startY)) {
                // Selection spans three or more lines
                context.textHighlight(leftEdge, startY + lineHeight, rightEdge, endY, true);
            }
            context.textHighlight(leftEdge, endY, endX, endY + lineHeight - 1, true);
        }
	}

    @Override
    public void setMaxLength(int maxLength) {
        editBox.setCharacterLimit(maxLength);
    }

    @Override
    public int getCursorPosition() {
        return editBox.cursor();
    }

	public int getLineCount() {
        return editBox.getLineCount();
	}

	public int getCurrentLineIndex() {
		return getLineIndex(getCursorPosition());
	}

	private int getLineIndex(int charIndex) {
		return (int) getValue()
			.substring(0, charIndex)
			.codePoints()
			.filter(point -> point == '\n')
			.count();
	}

	private int getLineStartBefore(int charIndex) {
		return getValue().lastIndexOf('\n', Math.max(charIndex, 0) - 1) + 1;
	}

    // Naming things is hard.
    public boolean isBeforeFirstNonWhitespaceCharacterInLine(int charIndex) {
        return getValue()
            .substring(getLineStartBefore(charIndex), charIndex)
            .chars()
            .allMatch(Character::isWhitespace);
    }

	public String getLine(int lineIndex) {
		var line = editBox.getLineView(lineIndex);
		return getValue().substring(line.beginIndex(), line.endIndex());
	}

	protected int getHorizontalScroll() {
		return horizontalScroll;
	}

	protected int getMaxHorizontalScroll() {
		return Math.max(0, Arrays.stream(getValue().split("\n"))
			.mapToInt(self.getFont()::width)
			.max()
			.orElse(0) + 8 - width);
	}

	protected boolean setHorizontalScroll(int horizontalScroll) {
		int previous = this.horizontalScroll;
		this.horizontalScroll = Mth.clamp(horizontalScroll, 0, getMaxHorizontalScroll());
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
		this.verticalScroll = Mth.clamp(verticalScroll, 0, getMaxVerticalScroll());
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
		int virtualX = getCharacterVirtualX(getCursorPosition());
		int virtualY = getCharacterVirtualY(getCursorPosition());

		setHorizontalScroll(Mth.clamp(horizontalScroll, virtualX - getInnerWidth(), virtualX));
		setVerticalScroll(Mth.clamp(verticalScroll, virtualY - getInnerHeight(), virtualY));
	}

	public int getLineHeight() {
		return lineHeight;
	}

	public void setLineHeight(int lineHeight) {
		this.lineHeight = lineHeight;
	}

    public int getCharacterVirtualX(int charIndex) {
		if (charIndex > getValue().length()) {
			return 0;
		}
		String line = getLine(getLineIndex(charIndex));

		int indexInLine = charIndex - getLineStartBefore(charIndex);
		if (indexInLine > line.length()) {
			indexInLine = line.length();
		}

		return self.getFont().width(line.substring(0, indexInLine));
	}

	public int getCharacterRealX(int charIndex) {
		return getInnerX() - horizontalScroll + getCharacterVirtualX(charIndex);
	}

	/**
	 * Gets the desired X position of the {@link CommandSuggestions} window.
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
	public int getScreenX(int charIndex) {
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
		if (charIndex > getValue().length()) {
			charIndex = getValue().length();
		}
		int lineIndex = getLineIndex(charIndex);

		return lineIndex * getLineHeight();
	}

	public int getCharacterRealY(int charIndex) {
		return getInnerY() - verticalScroll + getCharacterVirtualY(charIndex);
	}

	private int getInnerX() {
		return this.getX() + (isBordered() ? 4 : 0);
	}

	private int getInnerY() {
		return this.getY() + (isBordered() ? 4 : 0);
	}

	private int getInnerHeight() {
		return isBordered() ? this.height - 6 : this.height;
	}

    private static final Logger logger = LogManager.getLogger();

	@FunctionalInterface
	public interface SyntaxHighlighter {
        /**
         * A syntax highlighter that performs no highlighting.
         */
		SyntaxHighlighter NONE = text -> Arrays.stream(text.split("\n"))
            .map(line -> FormattedCharSequence.forward(line, Style.EMPTY))
            .toList();

		List<FormattedCharSequence> highlight(String text);
	}
}
