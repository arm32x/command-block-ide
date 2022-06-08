package arm32x.minecraft.commandblockide.client.gui.editor;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import arm32x.minecraft.commandblockide.client.gui.button.CommandBlockAutoButton;
import arm32x.minecraft.commandblockide.client.gui.button.CommandBlockTrackOutputButton;
import arm32x.minecraft.commandblockide.client.gui.button.CommandBlockTypeButton;
import arm32x.minecraft.commandblockide.client.update.DataCommandUpdateRequester;
import java.util.stream.Stream;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CommandBlockExecutor;

public final class CommandBlockEditor extends CommandEditor {
	private CommandBlockBlockEntity blockEntity;

	private final TextFieldWidget lastOutputField;

	private final CommandBlockTypeButton typeButton;
	private final CommandBlockAutoButton autoButton;
	private final CommandBlockTrackOutputButton trackOutputButton;

	private boolean commandFieldDirty = false;

	public CommandBlockEditor(Screen screen, TextRenderer textRenderer, int x, int y, int width, int height, CommandBlockBlockEntity blockEntity, int index) {
		super(screen, textRenderer, x, y, width, height, 40, 20, index);
		this.blockEntity = blockEntity;

		commandField.setMaxLength(32500);

		lastOutputField = new TextFieldWidget(
			textRenderer,
			commandField.x, commandField.y,
			commandField.getWidth(), commandField.getHeight(),
			Text.translatable("advMode.previousOutput")
				.append(Text.translatable("commandBlockIDE.narrator.editorIndex", index + 1))
		);
		lastOutputField.setEditable(false);
		lastOutputField.setMaxLength(32500);
		lastOutputField.setText(Text.translatable("commandBlockIDE.unloaded").getString());
		lastOutputField.visible = false;

		typeButton = addDrawableChild(new CommandBlockTypeButton(screen, x + 20, y));
		typeButton.type = blockEntity.getCommandBlockType();
		typeButton.active = false;

		autoButton = addDrawableChild(new CommandBlockAutoButton(screen, x + 40, y));
		autoButton.auto = typeButton.type == CommandBlockBlockEntity.Type.SEQUENCE;
		autoButton.active = false;

		trackOutputButton = addDrawableChild(new CommandBlockTrackOutputButton(screen, x + width - 16, y));
		trackOutputButton.trackingOutput = true;
		trackOutputButton.active = false;
	}

	public void save(ClientPlayNetworkHandler networkHandler) {
		if (isLoaded() && isDirty()) {
			CommandBlockExecutor executor = blockEntity.getCommandExecutor();
			networkHandler.sendPacket(new UpdateCommandBlockC2SPacket(
				new BlockPos(executor.getPos()),
				commandField.getText(),
				typeButton.type,
				trackOutputButton.trackingOutput,
				typeButton.conditional,
				autoButton.auto
			));
			executor.setTrackOutput(trackOutputButton.trackingOutput);
			if (!trackOutputButton.trackingOutput) {
				executor.setLastOutput(null);
			}
		}
	}

	public void update() {
		CommandBlockExecutor executor = blockEntity.getCommandExecutor();
		commandField.setText(executor.getCommand());
		typeButton.type = blockEntity.getCommandBlockType();
		typeButton.conditional = blockEntity.isConditionalCommandBlock();
		autoButton.auto = blockEntity.isAuto();
		trackOutputButton.trackingOutput = executor.isTrackingOutput();

		String lastOutput = executor.getLastOutput().getString();
		if (lastOutput.equals("")) {
			lastOutput = Text.translatable("commandBlockIDE.lastOutput.none").getString();
		}
		lastOutputField.setText(lastOutput);

		suggestor.setWindowActive(commandField.isActive());
		suggestor.refresh();

		commandFieldDirty = false;
		setLoaded(true);
	}

	public void requestUpdate(ClientPlayerEntity player) {
		DataCommandUpdateRequester.getInstance().requestUpdate(player, blockEntity);
	}

	@Override
	public void commandChanged(String newCommand) {
		if (!newCommand.equals(blockEntity.getCommandExecutor().getCommand())) {
			commandFieldDirty = true;
		}
		super.commandChanged(newCommand);
	}

	@Override
	protected void renderCommandField(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (trackOutputButton.isMouseOver(mouseX, mouseY)) {
			commandField.visible = false;
			lastOutputField.visible = true;
			lastOutputField.render(matrices, mouseX, mouseY, delta);
		} else {
			commandField.visible = true;
			lastOutputField.visible = false;
			commandField.render(matrices, mouseX, mouseY, delta);
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

		lastOutputField.y = commandField.y;

		typeButton.y = y;
		autoButton.y = y;
		trackOutputButton.y = y;
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);

		lastOutputField.setWidth(commandField.getWidth());

		trackOutputButton.x = getX() + width - 20;
	}
}
