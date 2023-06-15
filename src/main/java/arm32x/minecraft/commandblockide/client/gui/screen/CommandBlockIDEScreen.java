package arm32x.minecraft.commandblockide.client.gui.screen;

import arm32x.minecraft.commandblockide.client.CommandChainTracer;
import arm32x.minecraft.commandblockide.client.Dirtyable;
import arm32x.minecraft.commandblockide.client.gui.editor.CommandBlockEditor;
import arm32x.minecraft.commandblockide.client.gui.editor.CommandEditor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public final class CommandBlockIDEScreen extends CommandIDEScreen<CommandBlockEditor> implements Dirtyable {
	private final Map<BlockPos, CommandEditor> positionIndex = new HashMap<>();

	private final CommandBlockBlockEntity startingBlockEntity;
	private int startingIndex = -1;

	public CommandBlockIDEScreen(CommandBlockBlockEntity blockEntity) {
		super();
		startingBlockEntity = blockEntity;
	}

	@Override
	protected void firstInit() {
		assert client != null;
		CommandChainTracer tracer = new CommandChainTracer(client.world);

		Iterator<BlockPos> iterator = tracer.traceBackwards(startingBlockEntity.getPos()).iterator();
		BlockPos chainStart = startingBlockEntity.getPos();
		while (iterator.hasNext()) {
			chainStart = iterator.next();
		}

		addEditor(getBlockEntityAt(chainStart));
		for (BlockPos position : tracer.traceForwards(chainStart)) {
			addEditor(getBlockEntityAt(position));
		}

		BlockPos pos = startingBlockEntity.getPos();
		statusText = Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())
			.formatted(Formatting.GRAY)
			.asOrderedText();

		super.firstInit();
	}

	private void addEditor(CommandBlockBlockEntity blockEntity) {
		int index = editors.size();
		CommandBlockEditor editor = new CommandBlockEditor(this, textRenderer, 8, 20 * index + 8, width - 16, 16, blockEntity, index);
		addEditor(editor);
		positionIndex.put(blockEntity.getPos(), editor);
		if (blockEntity.equals(startingBlockEntity)) {
			startingIndex = index;
			setFocusedEditor(editor);
		} else {
			assert client != null && client.player != null;
			editor.requestUpdate(client.player);
		}
	}

	private CommandBlockBlockEntity getBlockEntityAt(BlockPos position) {
		assert client != null && client.world != null;
		BlockEntity blockEntity = client.world.getBlockEntity(position);
		if (blockEntity instanceof CommandBlockBlockEntity) {
			return (CommandBlockBlockEntity)blockEntity;
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
		assert client != null;
		ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
		assert networkHandler != null;
		editors.forEach(editor -> editor.save(networkHandler));
		super.save();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		for (CommandEditor editor : editors) {
			editor.lineNumberHighlighted = editor.index == startingIndex;
		}
		super.render(context, mouseX, mouseY, delta);
	}
}
