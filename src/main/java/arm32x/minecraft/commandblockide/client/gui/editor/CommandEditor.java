package arm32x.minecraft.commandblockide.client.gui.editor;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import arm32x.minecraft.commandblockide.client.gui.Container;
import arm32x.minecraft.commandblockide.client.gui.MultilineTextFieldWidget;
import arm32x.minecraft.commandblockide.client.processor.CommandProcessor;
import arm32x.minecraft.commandblockide.client.processor.MultilineCommandProcessor;
import arm32x.minecraft.commandblockide.client.processor.StringMapping;
import arm32x.minecraft.commandblockide.mixin.client.CommandSuggestionsAccessor;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
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

    protected final Font textRenderer;

    protected final MultilineTextFieldWidget commandField;
    protected final CommandSuggestions suggestor;
    protected final CommandProcessor processor = MultilineCommandProcessor.getInstance();

    private boolean suggestorActive = false;

    private boolean loaded = false;

    protected @Nullable IntConsumer heightChangedListener = null;

    @SuppressWarnings("ConstantConditions")
    public CommandEditor(Screen screen, Font textRenderer, int x, int y, int width, int height, int leftPadding, int rightPadding, int index) {
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
                x + leftPadding + 20, y,
                width - leftPadding - rightPadding - 20, height,
                Component.translatable("advMode.command")
                        .append(Component.translatable("commandBlockIDE.narrator.editorIndex", index + 1))
        ) {
            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(suggestor.getNarrationMessage());
            }
        });
        commandField.setEditable(false);
        commandField.setMaxLength(Integer.MAX_VALUE);

        suggestor = new CommandSuggestions(Minecraft.getInstance(), screen, commandField, textRenderer, true, true, 0, 16, false, Integer.MIN_VALUE);
        ((ChatInputSuggestorExtension) suggestor).ide$setCommandProcessor(processor);
        suggestor.updateCommandInfo();

        commandField.setResponder(this::commandChanged);
        commandField.setCursorChangeListener(suggestor::updateCommandInfo);
        commandField.setSyntaxHighlighter((text) -> {
            var parse = ((CommandSuggestionsAccessor) suggestor).getCurrentParse();
            if (parse != null) {
                return highlight(parse, text, processor.processCommand(text).getB());
            } else {
                // The command hasn't been parsed yet, so we show it without
                // highlighting. I haven't ever seen this in game, though.
                return MultilineTextFieldWidget.SyntaxHighlighter.NONE.highlight(text);
            }
        });
    }

    public void commandChanged(String newCommand) {
        suggestor.updateCommandInfo();
        setHeight(commandField.getLineCount() * commandField.getLineHeight() + 4);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (handleSpecialKey(input)) {
            return true;
        } else if (isSuggestorActive() && suggestor.keyPressed(input)) {
            return true;
        } else if (commandField.keyPressed(input)) {
            // Movement commands such as arrow keys should hide the suggestion
            // window since it's likely the user will want to move up or down.
            setSuggestorActive(false);
            return true;
        } else {
            return false;
        }
    }

    private boolean handleSpecialKey(KeyEvent input) {
        if (
                input.key() == GLFW.GLFW_KEY_TAB
                        && !isSuggestorActive()
                        && !commandField.isBeforeFirstNonWhitespaceCharacterInLine(commandField.getCursorPosition())
        ) {
            setSuggestorActive(true);
            suggestor.updateCommandInfo();
            // Immediately trigger completion without using Mixin by
            // simulating a key press. The scancode and modifiers arguments
            // are never used.
            return suggestor.keyPressed(new KeyEvent(GLFW.GLFW_KEY_TAB, -1, 0));
        } else if (input.key() == GLFW.GLFW_KEY_SPACE && input.hasControlDown()) {
            setSuggestorActive(true);
            suggestor.showSuggestions(true);
            return true;
        }
        // The Escape key is handled in CommandIDEScreen, not here.
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (super.charTyped(input)) {
            // The if statement ensures that only valid characters will trigger
            // the suggestions box.
            setSuggestorActive(true);
            suggestor.updateCommandInfo();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        boolean result = suggestor.mouseClicked(click)
                || super.mouseClicked(click, doubled);
        suggestor.setAllowSuggestions(false);
        return result;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return suggestor.mouseScrolled(verticalAmount)
                || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void setFocused(boolean focused) {
        setFocused(commandField);
        commandField.setFocused(focused);
        suggestor.setAllowSuggestions(false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        extractLineNumber(context);
        if (isLoaded()) {
            extractCommandField(context, mouseX, mouseY, delta);
        } else {
            context.text(textRenderer, Component.translatable("commandBlockIDE.unloaded"), commandField.getX(), y + 5, 0x7FFFFFFF);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    protected void extractLineNumber(GuiGraphicsExtractor context) {
        String lineNumber = String.valueOf(index + 1);
        // Manually draw shadow because the existing functions don’t let you set the color.
        context.text(textRenderer, lineNumber, x + 17 - textRenderer.width(lineNumber), y + 5, 0x3F000000);
        context.text(textRenderer, lineNumber, x + 16 - textRenderer.width(lineNumber), y + 4, lineNumberHighlighted ? 0xFFFFFFFF : 0x7FFFFFFF);
    }

    protected void extractCommandField(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        commandField.visible = true;
        commandField.extractRenderState(context, mouseX, mouseY, delta);
    }

    public void extractSuggestions(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if (commandField.canConsumeInput()) {
            suggestor.extractRenderState(context, mouseX, mouseY);
        }
    }

    public String getSingleLineCommand() {
        return processor.processCommand(commandField.getValue()).getA();
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

        commandField.setY(y);
        suggestor.updateCommandInfo();

    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;

        commandField.setWidth(width - leftPadding - rightPadding - 20);

        suggestor.updateCommandInfo();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        boolean changed = height != this.height;
        this.height = height;

        commandField.setHeight(height);

        suggestor.updateCommandInfo();

        if (changed) {
            onHeightChange(height);
        }
    }

    public boolean isSuggestorActive() {
        return suggestorActive;
    }

    public void setSuggestorActive(boolean suggestorActive) {
        suggestor.setAllowSuggestions(suggestorActive);
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
    public void updateNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, Component.translatable("narration.edit_box", commandField.getValue()));
    }

    protected static List<FormattedCharSequence> highlight(ParseResults<ClientSuggestionProvider> parse, String text, StringMapping mapping) {
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

        List<FormattedCharSequence> highlightedLines = new ArrayList<>();

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
                    if (codePoint < Character.MAX_VALUE && Character.isSurrogate((char) codePoint)) {
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
            ChatFormatting.AQUA,
            ChatFormatting.YELLOW,
            ChatFormatting.GREEN,
            ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.GOLD
    ).map(Style.EMPTY::withColor).toList();

    private static final Style INFO_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private static final Style ERROR_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
    private static final Style COMMENT_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
}
