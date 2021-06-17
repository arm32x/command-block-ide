package arm32x.minecraft.commandblockide.client.gui;

import net.minecraft.util.Identifier;

public final class CommandFunctionIDEScreen extends CommandIDEScreen {
	private final Identifier functionId;
	@SuppressWarnings("FieldMayBeFinal") private int lineCount;

	public CommandFunctionIDEScreen(Identifier functionId, int lineCount) {
		this.functionId = functionId;
		this.lineCount = lineCount;
	}

	@Override
	protected void firstInit() {
		for (int index = 0; index < lineCount; index++) {
			CommandFunctionEditor editor = new CommandFunctionEditor(this, textRenderer, 8, 20 * index + 8, width - 16, 16, index);
			if (index == 0) {
				setFocusedEditor(editor);
			}
			addEditor(editor);
		}
	}

	public void update(int index, String command) {
		CommandEditor editor = editors.get(index);
		if (editor instanceof CommandFunctionEditor) {
			((CommandFunctionEditor)editor).update(command);
		}
	}

	@Override
	public void apply() {
		// TODO
	}
}
