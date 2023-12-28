package arm32x.minecraft.commandblockide.client.gui.editor;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import arm32x.minecraft.commandblockide.client.gui.Container;
import arm32x.minecraft.commandblockide.client.gui.MultilineTextFieldWidget;
import arm32x.minecraft.commandblockide.client.processor.CommandProcessor;
import arm32x.minecraft.commandblockide.client.processor.MultilineCommandProcessor;
import arm32x.minecraft.commandblockide.client.processor.StringMapping;
import arm32x.minecraft.commandblockide.mixin.client.ChatInputSuggestorAccessor;
import arm32x.minecraft.commandblockide.mixinextensions.client.ChatInputSuggestorExtension;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public abstract class CommandEditor extends Container implements Dirtyable {
	private final int x;
	private int y;
	private int width;
	private int height;

	private final int leftPadding, rightPadding;

	public final int index;
	public boolean lineNumberHighlighted = false;

	protected final TextRenderer textRenderer;

	protected final MultilineTextFieldWidget commandField;
	protected final ChatInputSuggestor suggestor;
	protected final CommandProcessor processor = MultilineCommandProcessor.getInstance();

	private boolean suggestorActive = false;

	private boolean loaded = false;

	protected @Nullable IntConsumer heightChangedListener = null;

	@SuppressWarnings("ConstantConditions")
	public CommandEditor(Screen screen, TextRenderer textRenderer, int x, int y, int width, int height, int leftPadding, int rightPadding, int index) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.leftPadding = leftPadding;
		this.rightPadding = rightPadding;
		this.index = index;
		this.textRenderer = textRenderer;

		commandField = addSelectableChild(new MultilineTextFieldWidget(
			textRenderer,
			x + leftPadding + 20 + 1, y + 1,
			width - leftPadding - rightPadding - 20 - 2, height - 2,
			Text.translatable("advMode.command")
				.append(Text.translatable("commandBlockIDE.narrator.editorIndex", index + 1))
		) {
			@Override
			protected MutableText getNarrationMessage() {
				return super.getNarrationMessage().append(suggestor.getNarration());
			}
		});
		commandField.setEditable(false);
		commandField.setMaxLength(Integer.MAX_VALUE);

		suggestor = new ChatInputSuggestor(MinecraftClient.getInstance(), screen, commandField, textRenderer, true, true, 0, 16, false, Integer.MIN_VALUE);
		((ChatInputSuggestorExtension)suggestor).ide$setCommandProcessor(processor);
		suggestor.refresh();

		commandField.setChangedListener(this::commandChanged);
		commandField.setCursorChangeListener(suggestor::refresh);
		commandField.setSyntaxHighlighter((text) -> {
			var parse = ((ChatInputSuggestorAccessor)suggestor).getParse();
			if (parse != null) {
				return highlight(parse, text, processor.processCommand(text).getRight());
			} else {
				// The command hasn't been parsed yet, so we show it without
				// highlighting. I haven't ever seen this in game, though.
				return MultilineTextFieldWidget.SyntaxHighlighter.NONE.highlight(text);
			}
		});
	}

	public void commandChanged(String newCommand) {
		suggestor.refresh();
		setHeight(commandField.getLineCount() * commandField.getLineHeight() + 4);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (handleSpecialKey(keyCode)) {
			return true;
		} else if (isSuggestorActive() && suggestor.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (commandField.keyPressed(keyCode, scanCode, modifiers)) {
			// Movement commands such as arrow keys should hide the suggestion
			// window since it's likely the user will want to move up or down.
			setSuggestorActive(false);
			return true;
		} else {
			return false;
		}
	}

    private boolean handleSpecialKey(int keyCode) {
		if (
			keyCode == GLFW.GLFW_KEY_TAB
				&& !isSuggestorActive()
				&& !commandField.isBeforeFirstNonWhitespaceCharacterInLine(commandField.getCursor())
		) {
			setSuggestorActive(true);
			suggestor.refresh();
			// Immediately trigger completion without using Mixin by
			// simulating a key press. The scancode and modifiers arguments
			// are never used.
			return suggestor.keyPressed(GLFW.GLFW_KEY_TAB, -1, 0);
		} else if (keyCode == GLFW.GLFW_KEY_SPACE && Screen.hasControlDown()) {
			setSuggestorActive(true);
			suggestor.show(true);
			return true;
		}
		// The Escape key is handled in CommandIDEScreen, not here.
		return false;
    }

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (super.charTyped(chr, modifiers)) {
			// The if statement ensures that only valid characters will trigger
			// the suggestions box.
			setSuggestorActive(true);
			suggestor.refresh();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean result = suggestor.mouseClicked(mouseX, mouseY, button)
			|| super.mouseClicked(mouseX, mouseY, button);
		suggestor.setWindowActive(false);
		return result;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return suggestor.mouseScrolled(Screen.hasShiftDown() ? 0 : verticalAmount)
			|| super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void setFocused(boolean focused) {
		setFocused(commandField);
		commandField.setFocused(focused);
		suggestor.setWindowActive(false);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderLineNumber(context);
		if (isLoaded()) {
			renderCommandField(context, mouseX, mouseY, delta);
		} else {
			context.drawText(textRenderer, Text.translatable("commandBlockIDE.unloaded"), commandField.getX(), y + 5, 0x7FFFFFFF, false);
		}
		super.render(context, mouseX, mouseY, delta);
	}

	protected void renderLineNumber(DrawContext context) {
		String lineNumber = String.valueOf(index + 1);
		// Manually draw shadow because the existing functions don’t let you set the color.
		context.drawText(textRenderer, lineNumber, x + 17 - textRenderer.getWidth(lineNumber), y + 5, 0x3F000000, false);
		context.drawText(textRenderer, lineNumber, x + 16 - textRenderer.getWidth(lineNumber), y + 4, lineNumberHighlighted ? 0xFFFFFFFF : 0x7FFFFFFF, false);
	}

	protected void renderCommandField(DrawContext context, int mouseX, int mouseY, float delta) {
		commandField.visible = true;
		commandField.render(context, mouseX, mouseY, delta);
	}

	public void renderSuggestions(DrawContext context, int mouseX, int mouseY) {
		if (commandField.isActive()) {
			var matrices = context.getMatrices();
			matrices.push();
			matrices.translate(0.0, 0.0, 50.0);
			suggestor.render(context, mouseX, mouseY);
			matrices.pop();
		}
	}

	public String getSingleLineCommand() {
		return processor.processCommand(commandField.getText()).getLeft();
	}

	public boolean isLoaded() {
		return loaded;
	}

	@SuppressWarnings("SameParameterValue")
	protected void setLoaded(boolean loaded) {
		this.loaded = loaded;
		this.commandField.setEditable(loaded);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
 		this.y = y;

 		commandField.setY(y + 1);
		suggestor.refresh();

	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;

		commandField.setWidth(width - leftPadding - rightPadding - 20 - 2);

		suggestor.refresh();
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		boolean changed = height != this.height;
		this.height = height;

		commandField.setHeight(height - 2);

		suggestor.refresh();

		if (changed) {
			onHeightChange(height);
		}
	}

	public boolean isSuggestorActive() {
		return suggestorActive;
	}

	public void setSuggestorActive(boolean suggestorActive) {
		suggestor.setWindowActive(suggestorActive);
		this.suggestorActive = suggestorActive;
	}

	protected void onHeightChange(int height) {
		if (heightChangedListener != null) {
			heightChangedListener.accept(height);
		}
	}

	public void setHeightChangedListener(@Nullable IntConsumer listener) {
		heightChangedListener = listener;
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		builder.put(NarrationPart.TITLE, Text.translatable("narration.edit_box", commandField.getText()));
	}

	protected static List<OrderedText> highlight(ParseResults<CommandSource> parse, String text, StringMapping mapping) {
        // The ranges of text in the single-line command containing each
        // argument that should be highlighted.
		List<StringRange> ranges = parse
            .getContext()
            .getLastChild()
            .getArguments()
            .values()
            .stream()
			.map(ParsedArgument::getRange)
			.toList();

        // This is the index that the command parser stopped at. Everything at
        // or after this index is a parse error.
        int mappedParseStopIndex = mapping.mapIndexOrAfter(parse.getReader().getCursor());

		List<OrderedText> highlightedLines = new ArrayList<>();

		int startIndex = 0;
		while (startIndex <= text.length()) {
			// Find the end of the current line (exclusive)
			int endIndex = text.indexOf('\n', startIndex);
			if (endIndex == -1) {
				endIndex = text.length();
			}

			int start = startIndex;
			int end = endIndex;
			highlightedLines.add(visitor -> {
				charLoop:
				for (int index = start; index < end; index++) {
					int codePoint = text.codePointAt(index);
					// It's possible for codePointAt to return a low surrogate
					// if we ask for the second byte of a surrogate pair.
					if (codePoint < Character.MAX_VALUE && Character.isSurrogate((char)codePoint)) {
						continue;
					}

					OptionalInt maybeMappedIndex = mapping.inverted().mapIndex(index);
					if (maybeMappedIndex.isEmpty()) {
						if (!visitor.accept(index, COMMENT_STYLE, codePoint)) {
                            return false;
                        }
						continue;
					}
					int mappedIndex = maybeMappedIndex.getAsInt();

					for (int rangeIndex = 0; rangeIndex < ranges.size(); rangeIndex++) {
						var range = ranges.get(rangeIndex);
                        if (range.getStart() <= mappedIndex && mappedIndex < range.getEnd()) {
                            Style style = ARGUMENT_STYLES.get(rangeIndex % ARGUMENT_STYLES.size());
                            if (!visitor.accept(index, style, codePoint)) {
                                return false;
                            }
                            continue charLoop;
                        }
                    }

                    Style style = index >= mappedParseStopIndex ? ERROR_STYLE : INFO_STYLE;
                    if (!visitor.accept(index, style, codePoint)) {
                        return false;
                    }
				}
                return true;
			});

			startIndex = endIndex + 1;
		}

		return highlightedLines;
	}

	private static final List<Style> ARGUMENT_STYLES = Stream.of(
		Formatting.AQUA,
		Formatting.YELLOW,
		Formatting.GREEN,
		Formatting.LIGHT_PURPLE,
		Formatting.GOLD
	).map(Style.EMPTY::withColor).toList();

	private static final Style INFO_STYLE = Style.EMPTY.withColor(Formatting.GRAY);
	private static final Style ERROR_STYLE = Style.EMPTY.withColor(Formatting.RED);
	private static final Style COMMENT_STYLE = Style.EMPTY.withColor(Formatting.DARK_GRAY);
}
