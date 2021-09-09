package arm32x.minecraft.commandblockide.client.gui;

import arm32x.minecraft.commandblockide.client.processor.CommandProcessor;
import arm32x.minecraft.commandblockide.client.processor.MultilineCommandProcessor;
import arm32x.minecraft.commandblockide.client.processor.StringMapping;
import arm32x.minecraft.commandblockide.mixin.client.CommandSuggestorAccessor;
import arm32x.minecraft.commandblockide.mixinextensions.client.CommandSuggestorExtension;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class CommandEditor extends Container implements Drawable, Element {
	@SuppressWarnings("FieldMayBeFinal")
	private int x, y, width, height;
	private final int leftPadding, rightPadding;

	public int index;
	public boolean lineNumberHighlighted = false;

	protected final TextRenderer textRenderer;

	protected final MultilineTextFieldWidget commandField;
	protected final CommandSuggestor suggestor;
	protected final CommandProcessor processor = MultilineCommandProcessor.getInstance();

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

		commandField = addSelectableChild(new MultilineTextFieldWidget(textRenderer, x + leftPadding + 20 + 1, y + 1, width - leftPadding - rightPadding - 20 - 2, height - 2, new TranslatableText("advMode.command").append(new TranslatableText("commandBlockIDE.narrator.editorIndex", index + 1))) {
			@Override
			protected MutableText getNarrationMessage() {
				return super.getNarrationMessage().append(suggestor.getNarration());
			}
		});
		commandField.setEditable(false);
		commandField.setMaxLength(Integer.MAX_VALUE);

		suggestor = new CommandSuggestor(MinecraftClient.getInstance(), screen, commandField, textRenderer, true, true, 0, 16, false, Integer.MIN_VALUE);
		((CommandSuggestorExtension)suggestor).ide$setY(commandField.y + commandField.getHeight() + 2);
		((CommandSuggestorExtension)suggestor).ide$setCommandProcessor(processor);
		suggestor.refresh();

		commandField.setChangedListener(this::commandChanged);
		commandField.setRenderTextProvider((original, firstCharacterIndex) -> {
			assert firstCharacterIndex == 0;
			var parse = ((CommandSuggestorAccessor)suggestor).getParseResults();
			if (parse != null) {
				return highlight(parse, original, processor.processCommand(original).getRight());
			} else {
				return ((CommandSuggestorAccessor)suggestor).invokeProvideRenderText(original, firstCharacterIndex);
			}
		});
	}

	public void commandChanged(String newCommand) {
		suggestor.refresh();
		setHeight(commandField.getLineCount() * commandField.getLineHeight() + 4);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return suggestor.keyPressed(keyCode, scanCode, modifiers)
			|| super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean result = suggestor.mouseClicked(mouseX, mouseY, button)
			|| super.mouseClicked(mouseX, mouseY, button);
		suggestor.setWindowActive(commandField.isActive());
		suggestor.refresh();
		return result;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return suggestor.mouseScrolled(amount)
			|| super.mouseScrolled(mouseX, mouseY, amount);
	}

	public void setFocused(boolean focused) {
		setFocused(commandField);
		commandField.setTextFieldFocused(focused);
		suggestor.setWindowActive(commandField.isActive());
		suggestor.refresh();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderLineNumber(matrices);
		if (isLoaded()) {
			renderCommandField(matrices, mouseX, mouseY, delta);
		} else {
			textRenderer.draw(matrices, new TranslatableText("commandBlockIDE.unloaded"), commandField.x, y + 5, 0x7FFFFFFF);
		}
		super.render(matrices, mouseX, mouseY, delta);
	}

	protected void renderLineNumber(MatrixStack matrices) {
		String lineNumber = String.valueOf(index + 1);
		// Manually draw shadow because the existing functions don’t let you set the color.
		textRenderer.draw(matrices, lineNumber, x + 17 - textRenderer.getWidth(lineNumber), y + 5, 0x3F000000);
		textRenderer.draw(matrices, lineNumber, x + 16 - textRenderer.getWidth(lineNumber), y + 4, lineNumberHighlighted ? 0xFFFFFFFF : 0x7FFFFFFF);
	}

	protected void renderCommandField(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		commandField.visible = true;
		commandField.render(matrices, mouseX, mouseY, delta);
	}

	public void renderSuggestions(MatrixStack matrices, int mouseX, int mouseY) {
		if (commandField.isActive()) {
			matrices.push();
			matrices.translate(0.0, 0.0, 50.0);
			suggestor.render(matrices, mouseX, mouseY);
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

 		commandField.y = y + 1;
		((CommandSuggestorExtension)suggestor).ide$setY(commandField.y + commandField.getHeight() + 2);
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
		builder.put(NarrationPart.TITLE, new TranslatableText("narration.edit_box", commandField.getText()));
	}

	protected static OrderedText highlight(ParseResults<CommandSource> parse, String multiline, StringMapping mapping) {
		List<StringRange> ranges = parse.getContext().getLastChild().getArguments().values().stream()
			.map(ParsedArgument::getRange)
			.toList();

		return visitor -> {
			var index = new AtomicInteger();
			while (index.get() < multiline.length()) {
				int codePoint = multiline.codePointAt(index.get());
				Style style = IntStream.range(0, ranges.size())
					.filter(i -> {
						StringRange range = ranges.get(i);
						OptionalInt mappedIndex = mapping.inverted().mapIndex(index.get());
						return mappedIndex.isPresent()
							&& range.getStart() <= mappedIndex.getAsInt()
							&& mappedIndex.getAsInt() < range.getEnd();
					})
					.mapToObj(i -> ARGUMENT_STYLES.get(i % ARGUMENT_STYLES.size()))
					.findFirst()
					.orElseGet(() -> {
						OptionalInt endIndex = mapping.mapIndex(parse.getReader().getCursor());
						if (endIndex.isPresent() && index.get() >= endIndex.getAsInt()) {
							return ERROR_STYLE;
						} else if (mapping.inverted().mapIndex(index.get()).isPresent()) {
							return INFO_STYLE;
						} else {
							return COMMENT_STYLE;
						}
					});
				if (!visitor.accept(index.get(), style, codePoint)) {
					return false;
				}
				index.getAndAdd(Character.charCount(codePoint));
			}
			return true;
		};
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

