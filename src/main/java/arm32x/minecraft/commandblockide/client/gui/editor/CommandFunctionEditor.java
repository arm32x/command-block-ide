package arm32x.minecraft.commandblockide.client.gui.editor;

import arm32x.minecraft.commandblockide.client.storage.MultilineCommandStorage;
import arm32x.minecraft.commandblockide.mixinextensions.client.ChatInputSuggestorExtension;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class CommandFunctionEditor extends CommandEditor {
	private @Nullable String originalCommand;

	private boolean dirty = false;

	public CommandFunctionEditor(Screen screen, Font textRenderer, int x, int y, int width, int height, int index) {
		super(screen, textRenderer, x, y, width, height, 0, 0, index);

		ChatInputSuggestorExtension suggestorExtension = (ChatInputSuggestorExtension)suggestor;
		suggestorExtension.ide$setAllowComments(true);
		suggestorExtension.ide$setSlashForbidden(true);
	}

	public void saveMultilineCommand(Identifier function) {
		Minecraft client = Minecraft.getInstance();
		String world = client.isLocalServer()
			? Objects.requireNonNull(client.getSingleplayerServer()).getWorldData().getLevelName()
			: Objects.requireNonNull(client.getCurrentServer()).name;

		MultilineCommandStorage.getInstance().add(commandField.getValue(), getSingleLineCommand(), client.isLocalServer(), world, function, index);
	}

	public void update(Identifier functionId, String command) {
		originalCommand = command;
		Minecraft client = Minecraft.getInstance();
		commandField.setValue(MultilineCommandStorage.getInstance().getRobust(
			command,
			processor,
			client.isLocalServer(),
			client.isLocalServer()
				? Objects.requireNonNull(client.getSingleplayerServer()).getWorldData().getLevelName()
				: Objects.requireNonNull(client.getCurrentServer()).name,
			functionId,
			index
		));

		suggestor.setAllowSuggestions(commandField.canConsumeInput());
		suggestor.updateCommandInfo();

		dirty = false;
		setLoaded(true);
	}

	@Override
	public void commandChanged(String newCommand) {
		if (!newCommand.equals(originalCommand)) {
			dirty = true;
		}
		super.commandChanged(newCommand);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
}
