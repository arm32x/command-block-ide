package arm32x.minecraft.commandblockide.gui;

import arm32x.minecraft.commandblockide.Dirtyable;
import arm32x.minecraft.commandblockide.extensions.CommandSuggestorExtension;
import arm32x.minecraft.commandblockide.update.DataCommandUpdateRequester;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CommandBlockExecutor;

@Environment(EnvType.CLIENT)
public final class CommandBlockEditor extends Container implements Dirtyable, Drawable, Element {
	private int x, y;
	public final int width, height;

	public final int index;

	private final CommandBlockBlockEntity blockEntity;
	private final TextRenderer textRenderer;

	private final TextFieldWidget commandField;
	private final TextFieldWidget lastOutputField;
	private final CommandSuggestor suggestor;
	private final CommandBlockTypeButton typeButton;
	private final CommandBlockAutoButton autoButton;
	private final CommandBlockTrackOutputButton trackOutputButton;

	private boolean loaded = false;
	private boolean dirty = false;

	public CommandBlockEditor(Screen screen, TextRenderer textRenderer, int x, int y, int width, int height, CommandBlockBlockEntity blockEntity, int index) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.index = index;
		this.blockEntity = blockEntity;
		this.textRenderer = textRenderer;

		commandField = addChild(new TextFieldWidget(textRenderer, x + 41, y + 1, width - 66, height - 2, new TranslatableText("advMode.command").append(new TranslatableText("commandBlockIDE.narrator.editorIndex", index + 1))) {
			@Override
			protected MutableText getNarrationMessage() {
				return super.getNarrationMessage().append(suggestor.getNarration());
			}
		});
		commandField.setEditable(false);
		commandField.setMaxLength(32500);

		lastOutputField = new TextFieldWidget(textRenderer, commandField.x, commandField.y, commandField.getWidth(), commandField.getHeight(), new TranslatableText("advMode.previousOutput").append(new TranslatableText("commandBlockIDE.narrator.editorIndex", index + 1)));
		lastOutputField.setEditable(false);
		lastOutputField.setMaxLength(32500);
		lastOutputField.setText(new TranslatableText("commandBlockIDE.unloaded").getString());
		lastOutputField.visible = false;

		suggestor = new CommandSuggestor(MinecraftClient.getInstance(), screen, commandField, textRenderer, true, true, 0, 16, false, Integer.MIN_VALUE);
		suggestor.refresh();

		commandField.setChangedListener((text) -> {
			if (!text.equals(blockEntity.getCommandExecutor().getCommand())) {
				markDirty();
			}
			suggestor.refresh();
		});

		//noinspection ConstantConditions
		((CommandSuggestorExtension)suggestor).ide$setY(commandField.y + commandField.getHeight() + 2);

		typeButton = addButton(new CommandBlockTypeButton(screen, x, y));
		typeButton.type = blockEntity.getCommandBlockType();
		typeButton.active = false;

		autoButton = addButton(new CommandBlockAutoButton(screen, x + 20, y));
		autoButton.auto = typeButton.type == CommandBlockBlockEntity.Type.SEQUENCE;
		autoButton.active = false;

		trackOutputButton = addButton(new CommandBlockTrackOutputButton(screen, x + width - 20, y));
		trackOutputButton.trackingOutput = true;
		trackOutputButton.active = false;
	}

	public void updateCommandBlock() {
		CommandBlockExecutor executor = blockEntity.getCommandExecutor();
		commandField.setText(executor.getCommand());
		typeButton.type = blockEntity.getCommandBlockType();
		typeButton.conditional = blockEntity.isConditionalCommandBlock();
		autoButton.auto = blockEntity.isAuto();
		trackOutputButton.trackingOutput = executor.isTrackingOutput();

		String lastOutput = executor.getLastOutput().getString();
		if (lastOutput.equals("")) {
			lastOutput = new TranslatableText("commandBlockIDE.lastOutput.none").getString();
		}
		lastOutputField.setText(lastOutput);

		this.commandField.setEditable(true);
		typeButton.active = true;
		autoButton.active = true;
		trackOutputButton.active = true;
		suggestor.setWindowActive(commandField.isActive());
		suggestor.refresh();

		dirty = false;
		loaded = true;
	}

	public void requestUpdate(ClientPlayNetworkHandler networkHandler) {
		DataCommandUpdateRequester.getInstance().requestUpdate(networkHandler, blockEntity);
	}

	public void apply(ClientPlayNetworkHandler networkHandler) {
		if (loaded && Stream.<Dirtyable>of(this, typeButton, autoButton, trackOutputButton).anyMatch(Dirtyable::isDirty)) {
			CommandBlockExecutor executor = blockEntity.getCommandExecutor();
			networkHandler.sendPacket(new UpdateCommandBlockC2SPacket(
				new BlockPos(executor.getPos()),
				commandField.getText(),
				typeButton.type,
				trackOutputButton.trackingOutput,
				typeButton.conditional,
				autoButton.auto
			));
			executor.shouldTrackOutput(trackOutputButton.trackingOutput);
			if (!trackOutputButton.trackingOutput) {
				executor.setLastOutput(null);
			}
		}
	}

	public void swapWith(CommandBlockEditor other) {
		String command = commandField.getText();
		commandField.setText(other.commandField.getText());
		other.commandField.setText(command);

		CommandBlockBlockEntity.Type type = typeButton.type;
		typeButton.type = other.typeButton.type;
		other.typeButton.type = type;

		boolean conditional = typeButton.conditional;
		typeButton.conditional = other.typeButton.conditional;
		other.typeButton.conditional = conditional;

		boolean auto = autoButton.auto;
		autoButton.auto = other.autoButton.auto;
		other.autoButton.auto = auto;

		boolean trackingOutput = trackOutputButton.trackingOutput;
		trackOutputButton.trackingOutput = other.trackOutputButton.trackingOutput;
		other.trackOutputButton.trackingOutput = trackingOutput;
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

	public boolean isLoaded() { return loaded; }

	@Override
	public boolean isDirty() { return dirty; }

	@Override
	public void markDirty() { dirty = true; }

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (loaded) {
			if (trackOutputButton.isMouseOver(mouseX, mouseY)) {
				commandField.visible = false;
				lastOutputField.visible = true;
				lastOutputField.render(matrices, mouseX, mouseY, delta);
			} else {
				commandField.visible = true;
				lastOutputField.visible = false;
				commandField.render(matrices, mouseX, mouseY, delta);
			}
		} else {
			textRenderer.draw(matrices, new TranslatableText("commandBlockIDE.unloaded"), commandField.x, y + 5, 0x7FFFFFFF);
		}
		super.render(matrices, mouseX, mouseY, delta);
	}

	public void renderSuggestions(MatrixStack matrices, int mouseX, int mouseY) {
		if (commandField.isActive()) {
			matrices.push();
			matrices.translate(0.0, 0.0, 50.0);
			suggestor.render(matrices, mouseX, mouseY);
			matrices.pop();
		}
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
 		lastOutputField.y = commandField.y;
		((CommandSuggestorExtension)suggestor).ide$setY(commandField.y + commandField.getHeight() + 2);
		suggestor.refresh();

		typeButton.y = y;
		autoButton.y = y;
		trackOutputButton.y = y;
	}
}

