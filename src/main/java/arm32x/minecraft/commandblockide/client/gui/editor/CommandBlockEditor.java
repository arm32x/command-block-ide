package arm32x.minecraft.commandblockide.client.gui.editor;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import arm32x.minecraft.commandblockide.client.gui.button.CommandBlockAutoButton;
import arm32x.minecraft.commandblockide.client.gui.button.CommandBlockTrackOutputButton;
import arm32x.minecraft.commandblockide.client.gui.button.CommandBlockTypeButton;
import arm32x.minecraft.commandblockide.client.storage.MultilineCommandStorage;
import arm32x.minecraft.commandblockide.client.update.DataCommandUpdateRequester;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BaseCommandBlock;

public final class CommandBlockEditor extends CommandEditor {
	private final CommandBlockEntity blockEntity;

	private final EditBox lastOutputField;

	private final CommandBlockTypeButton typeButton;
	private final CommandBlockAutoButton autoButton;
	private final CommandBlockTrackOutputButton trackOutputButton;

	private boolean commandFieldDirty = false;

	public CommandBlockEditor(Screen screen, Font textRenderer, int x, int y, int width, int height, CommandBlockEntity blockEntity, int index) {
		super(screen, textRenderer, x, y, width, height, 40, 20, index);
		this.blockEntity = blockEntity;

		commandField.setMaxLength(32500);

		lastOutputField = new EditBox(
			textRenderer,
			commandField.getX(), commandField.getY(),
			commandField.getWidth(), commandField.getHeight(),
			Component.translatable("advMode.previousOutput")
				.append(Component.translatable("commandBlockIDE.narrator.editorIndex", index + 1))
		);
		lastOutputField.setEditable(false);
		lastOutputField.setMaxLength(32500);
		lastOutputField.setValue(Component.translatable("commandBlockIDE.unloaded").getString());
		lastOutputField.visible = false;

		typeButton = addDrawableChild(new CommandBlockTypeButton(x + 20, y));
		typeButton.setBlockType(blockEntity.getMode());
		typeButton.active = false;

		autoButton = addDrawableChild(new CommandBlockAutoButton(x + 40, y));
		autoButton.setAuto(typeButton.getBlockType() == CommandBlockEntity.Mode.SEQUENCE);
		autoButton.active = false;

		trackOutputButton = addDrawableChild(new CommandBlockTrackOutputButton(x + width - 16, y));
		trackOutputButton.setTrackingOutput(true);
		trackOutputButton.active = false;
	}

	public void save(ClientPacketListener networkHandler) {
		if (isLoaded() && isDirty()) {
			BaseCommandBlock executor = blockEntity.getCommandBlock();
			networkHandler.send(new ServerboundSetCommandBlockPacket(
				blockEntity.getBlockPos(),
				getSingleLineCommand(),
				typeButton.getBlockType(),
				trackOutputButton.isTrackingOutput(),
				typeButton.isConditional(),
				autoButton.isAuto()
			));
			executor.setTrackOutput(trackOutputButton.isTrackingOutput());
			if (!trackOutputButton.isTrackingOutput()) {
				executor.setLastOutput(null);
			}
			saveMultilineCommand();
		}
	}

	private void saveMultilineCommand() {
		Minecraft client = Minecraft.getInstance();
		String world = client.isLocalServer()
			? Objects.requireNonNull(client.getSingleplayerServer()).getWorldData().getLevelName()
			: Objects.requireNonNull(client.getCurrentServer()).name;

		MultilineCommandStorage.getInstance().add(commandField.getValue(), getSingleLineCommand(), client.isLocalServer(), world, blockEntity.getBlockPos());
	}

	public void update() {
		BaseCommandBlock executor = blockEntity.getCommandBlock();
		Minecraft client = Minecraft.getInstance();
		commandField.setValue(MultilineCommandStorage.getInstance().getRobust(
			executor.getCommand(),
			processor,
			client.isLocalServer(),
			client.isLocalServer()
				? Objects.requireNonNull(client.getSingleplayerServer()).getWorldData().getLevelName()
				: Objects.requireNonNull(client.getCurrentServer()).name,
				blockEntity.getBlockPos()
		));
		typeButton.setBlockType(blockEntity.getMode());
		typeButton.setConditional(blockEntity.isConditional());
		autoButton.setAuto(blockEntity.isAutomatic());
		trackOutputButton.setTrackingOutput(executor.isTrackOutput());

		String lastOutput = executor.getLastOutput().getString();
		if (lastOutput.isEmpty()) {
			lastOutput = Component.translatable("commandBlockIDE.lastOutput.none").getString();
		}
		lastOutputField.setValue(lastOutput);

		suggestor.setAllowSuggestions(commandField.canConsumeInput());
		suggestor.updateCommandInfo();

		commandFieldDirty = false;
		setLoaded(true);
	}

	public void requestUpdate(LocalPlayer player) {
		DataCommandUpdateRequester.getInstance().requestUpdate(player, blockEntity);
	}

	@Override
	public void commandChanged(String newCommand) {
		if (!newCommand.equals(blockEntity.getCommandBlock().getCommand())) {
			commandFieldDirty = true;
		}
		super.commandChanged(newCommand);
	}

	@Override
	protected void renderCommandField(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (trackOutputButton.isMouseOver(mouseX, mouseY)) {
			commandField.visible = false;
			lastOutputField.visible = true;
			lastOutputField.render(context, mouseX, mouseY, delta);
		} else {
			commandField.visible = true;
			lastOutputField.visible = false;
			commandField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	public void setLoaded(boolean loaded) {
		super.setLoaded(loaded);
		typeButton.active = loaded;
		autoButton.active = loaded;
		trackOutputButton.active = loaded;
	}

	@Override
	public boolean isDirty() {
		return commandFieldDirty
			|| Stream.<Dirtyable>of(typeButton, autoButton, trackOutputButton).anyMatch(Dirtyable::isDirty);
	}

	@Override
	public void setY(int y) {
		super.setY(y);

		lastOutputField.setY(commandField.getY());

		typeButton.setY(y);
		autoButton.setY(y);
		trackOutputButton.setY(y);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);

		lastOutputField.setWidth(commandField.getWidth());

		trackOutputButton.setX(getX() + width - 16);
	}
}
