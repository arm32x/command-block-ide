package arm32x.minecraft.commandblockide.client.gui.editor;

import arm32x.minecraft.commandblockide.client.gui.Container;
import arm32x.minecraft.commandblockide.mixinextensions.client.CommandSuggestorExtension;
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
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public abstract class CommandEditor extends Container implements Drawable, Element {
	@SuppressWarnings("FieldMayBeFinal")
	private int x, y, width, height;
	private final int leftPadding, rightPadding;

	public int index;
	public boolean lineNumberHighlighted = false;

	protected final TextRenderer textRenderer;

	protected final TextFieldWidget commandField;
	protected final CommandSuggestor suggestor;

	private boolean loaded = false;

	public CommandEditor(Screen screen, TextRenderer textRenderer, int x, int y, int width, int height, int leftPadding, int rightPadding, int index) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.leftPadding = leftPadding;
		this.rightPadding = rightPadding;
		this.index = index;
		this.textRenderer = textRenderer;

		commandField = addSelectableChild(new TextFieldWidget(textRenderer, x + leftPadding + 20 + 1, y + 1, width - leftPadding - rightPadding - 20 - 2, height - 2, new TranslatableText("advMode.command").append(new TranslatableText("commandBlockIDE.narrator.editorIndex", index + 1))) {
			@Override
			protected MutableText getNarrationMessage() {
				return super.getNarrationMessage().append(suggestor.getNarration());
			}
		});
		commandField.setEditable(false);
		commandField.setMaxLength(Integer.MAX_VALUE);

		suggestor = new CommandSuggestor(MinecraftClient.getInstance(), screen, commandField, textRenderer, true, true, 0, 16, false, Integer.MIN_VALUE);
		suggestor.refresh();

		commandField.setChangedListener(this::commandChanged);

		//noinspection ConstantConditions
		((CommandSuggestorExtension)suggestor).ide$setY(commandField.y + commandField.getHeight() + 2);

	}

	public void commandChanged(String newCommand) {
		suggestor.refresh();
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
		return suggestor.mouseScrolled(amount);
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

	public String getCommand() {
		return commandField.getText();
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

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		builder.put(NarrationPart.TITLE, new TranslatableText("narration.edit_box", commandField.getText()));
	}
}

