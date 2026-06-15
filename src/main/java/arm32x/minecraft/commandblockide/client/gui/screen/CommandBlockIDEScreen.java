package arm32x.minecraft.commandblockide.client.gui.screen;

import arm32x.minecraft.commandblockide.client.CommandChainTracer;
import arm32x.minecraft.commandblockide.client.gui.editor.CommandBlockEditor;
import arm32x.minecraft.commandblockide.client.gui.editor.CommandEditor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;

public final class CommandBlockIDEScreen extends CommandIDEScreen<CommandBlockEditor> {
	private final Map<BlockPos, CommandEditor> positionIndex = new HashMap<>();

	private final CommandBlockEntity startingBlockEntity;
	private int startingIndex = -1;

	public CommandBlockIDEScreen(CommandBlockEntity blockEntity) {
		super();
		startingBlockEntity = blockEntity;
	}

	@Override
	protected void firstInit() {
		assert minecraft != null;
		CommandChainTracer tracer = new CommandChainTracer(minecraft.level);

		Iterator<BlockPos> iterator = tracer.traceBackwards(startingBlockEntity.getBlockPos()).iterator();
		BlockPos chainStart = startingBlockEntity.getBlockPos();
		while (iterator.hasNext()) {
			chainStart = iterator.next();
		}

		addEditor(getBlockEntityAt(chainStart));
		for (BlockPos position : tracer.traceForwards(chainStart)) {
			addEditor(getBlockEntityAt(position));
		}

		BlockPos pos = startingBlockEntity.getBlockPos();
		statusText = Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())
			.withStyle(ChatFormatting.GRAY);

		super.firstInit();
	}

	private void addEditor(CommandBlockEntity blockEntity) {
		int index = editors.size();
		CommandBlockEditor editor = new CommandBlockEditor(this, font, 8, 20 * index + 8, width - 16, 16, blockEntity, index);
		addEditor(editor);
		positionIndex.put(blockEntity.getBlockPos(), editor);
		if (blockEntity.equals(startingBlockEntity)) {
			startingIndex = index;
			setFocusedEditor(editor);
		} else {
			assert minecraft != null && minecraft.player != null;
			editor.requestUpdate(minecraft.player);
		}
	}

	private CommandBlockEntity getBlockEntityAt(BlockPos position) {
		assert minecraft != null && minecraft.level != null;
		BlockEntity blockEntity = minecraft.level.getBlockEntity(position);
		if (blockEntity instanceof CommandBlockEntity) {
			return (CommandBlockEntity)blockEntity;
		} else {
			throw new RuntimeException("No command block at position.");
		}
	}

	public void update(BlockPos position) {
		if (positionIndex.get(position) instanceof CommandBlockEditor editor) {
			editor.update();
			setLoaded(true);
			if (getFocused() == editor) {
				setFocusedEditor(editor);
			}
		}
	}

	@Override
	public void save() {
		assert minecraft != null;
		ClientPacketListener networkHandler = minecraft.getConnection();
		assert networkHandler != null;
		editors.forEach(editor -> editor.save(networkHandler));
		super.save();
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		for (CommandEditor editor : editors) {
			editor.lineNumberHighlighted = editor.index == startingIndex;
		}
		super.render(context, mouseX, mouseY, delta);
	}
}
