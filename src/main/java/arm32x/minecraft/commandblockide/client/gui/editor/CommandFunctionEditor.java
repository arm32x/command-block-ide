package arm32x.minecraft.commandblockide.client.gui.editor;

import arm32x.minecraft.commandblockide.mixinextensions.client.ChatInputSuggestorExtension;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

public final class CommandFunctionEditor extends CommandEditor {
	private @Nullable String originalCommand;

	private boolean dirty = false;

	public CommandFunctionEditor(Screen screen, TextRenderer textRenderer, int x, int y, int width, int height, int index) {
		super(screen, textRenderer, x, y, width, height, 0, 0, index);

		ChatInputSuggestorExtension suggestorExtension = (ChatInputSuggestorExtension)suggestor;
		suggestorExtension.ide$setAllowComments(true);
		suggestorExtension.ide$setSlashForbidden(true);
	}

	public void update(String command) {
		originalCommand = command;
		commandField.setText(command);

		suggestor.setWindowActive(commandField.isActive());
		suggestor.refresh();

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
