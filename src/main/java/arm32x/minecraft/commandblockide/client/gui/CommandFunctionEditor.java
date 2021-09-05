package arm32x.minecraft.commandblockide.client.gui;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import arm32x.minecraft.commandblockide.client.storage.MultilineCommandStorage;
import arm32x.minecraft.commandblockide.mixinextensions.client.CommandSuggestorExtension;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class CommandFunctionEditor extends CommandEditor implements Dirtyable {
	private @Nullable String originalCommand;

	private boolean dirty = false;

	public CommandFunctionEditor(Screen screen, TextRenderer textRenderer, int x, int y, int width, int height, int index) {
		super(screen, textRenderer, x, y, width, height, 0, 0, index);

		CommandSuggestorExtension suggestorExtension = (CommandSuggestorExtension)suggestor;
		suggestorExtension.ide$setAllowComments(true);
		suggestorExtension.ide$setSlashForbidden(true);
	}

	void saveMultilineCommand(Identifier function) {
		MinecraftClient client = MinecraftClient.getInstance();
		String world = client.isInSingleplayer()
			? Objects.requireNonNull(client.getServer()).getSaveProperties().getLevelName()
			: Objects.requireNonNull(client.getCurrentServerEntry()).name;

		MultilineCommandStorage.getInstance().add(commandField.getText(), getSingleLineCommand(), client.isInSingleplayer(), world, function, index);
	}

	public void update(Identifier functionId, String command) {
		originalCommand = command;
		MinecraftClient client = MinecraftClient.getInstance();
		commandField.setText(MultilineCommandStorage.getInstance().getRobust(
			command,
			processor,
			client.isInSingleplayer(),
			client.isInSingleplayer()
				? Objects.requireNonNull(client.getServer()).getSaveProperties().getLevelName()
				: Objects.requireNonNull(client.getCurrentServerEntry()).name,
			functionId,
			index
		));

		suggestor.setWindowActive(commandField.isActive());
		suggestor.refresh();

		dirty = false;
		setLoaded(true);
	}

	@Override
	public void commandChanged(String newCommand) {
		if (!newCommand.equals(originalCommand)) {
			markDirty();
		}
		super.commandChanged(newCommand);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void markDirty() {
		this.dirty = true;
	}
}
