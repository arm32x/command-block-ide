package arm32x.minecraft.commandblockide.gui;

import arm32x.minecraft.commandblockide.CommandChainTracer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class CommandBlockIDEScreen extends Screen {
	private final List<CommandBlockEditor> editors = new ArrayList<>();
	private final Map<BlockPos, CommandBlockEditor> positionIndex = new HashMap<>();

	private final CommandBlockBlockEntity startingBlockEntity;
	private int startingIndex = -1;

	private ButtonWidget doneButton;
	private ButtonWidget applyAllButton;

	private int scrollOffset = 0, maxScrollOffset = 0;
	private static final double SCROLL_SENSITIVITY = 50.0;

	public CommandBlockIDEScreen(CommandBlockBlockEntity blockEntity) {
		super(LiteralText.EMPTY);
		startingBlockEntity = blockEntity;
	}

	@SuppressWarnings("CodeBlock2Expr")
	@Override
	protected void init() {
		assert client != null;
		client.keyboard.setRepeatEvents(true);

		doneButton = addButton(new ButtonWidget(this.width - 324, this.height - 28, 100, 20, ScreenTexts.DONE, (widget) -> {
			applyAll();
			onClose();
		}));
		doneButton.active = false;
		/* cancelButton = */ addButton(new ButtonWidget(this.width - 216, this.height - 28, 100, 20, ScreenTexts.CANCEL, (widget) -> {
			onClose();
		}));
		applyAllButton = addButton(new ButtonWidget(this.width - 108, this.height - 28, 100, 20, new TranslatableText("commandBlockIDE.applyAll"), (widget) -> {
			applyAll();
		}));
		applyAllButton.active = false;

		ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
		assert networkHandler != null;
		CommandChainTracer tracer = new CommandChainTracer(client.world);

		Iterator<BlockPos> iterator = tracer.traceBackwards(startingBlockEntity.getPos()).iterator();
		BlockPos chainStart = startingBlockEntity.getPos();
		while (iterator.hasNext()) {
			chainStart = iterator.next();
		}

		addCommandBlock(getBlockEntityAt(chainStart));
		for (BlockPos position : tracer.traceForwards(chainStart)) {
			addCommandBlock(getBlockEntityAt(position));
		}

		maxScrollOffset = Math.max((editors.size() * 20 - 8) - (height - 50), 0);
		setFocusedEditor(editors.get(startingIndex));
	}

	private void addCommandBlock(CommandBlockBlockEntity blockEntity) {
		int index = editors.size();
		CommandBlockEditor editor = new CommandBlockEditor(this, textRenderer, 24, 20 * index + 8, width - 32, 16, blockEntity, index);
		editors.add(editor);
		positionIndex.put(blockEntity.getPos(), editor);
		if (blockEntity.equals(startingBlockEntity)) {
			startingIndex = index;
			setFocusedEditor(editor);
		} else {
			assert client != null;
			editor.requestUpdate(Objects.requireNonNull(client.getNetworkHandler()));
		}
		addChild(editor);
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

	public void updateCommandBlock(BlockPos position) {
		CommandBlockEditor editor = positionIndex.get(position);
		if (editor != null) {
			editor.updateCommandBlock();
			doneButton.active = true;
			applyAllButton.active = true;
		}
	}

	public void applyAll() {
		assert client != null;
		ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
		assert networkHandler != null;
		for (CommandBlockEditor editor : editors) {
			editor.apply(networkHandler);
		}
	}

	@Override
	public boolean shouldCloseOnEsc() { return false; }

	@Override
	public void onClose() {
		assert client != null;
		client.keyboard.setRepeatEvents(false);
		super.onClose();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			Element element = getFocused();
			if (element == null) {
				onClose();
			} else {
				setFocused(null);
				if (element instanceof CommandBlockEditor) {
					((CommandBlockEditor)element).setFocused(false);
				}
			}
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			Element element = getFocused();
			if (element == null) {
				applyAll();
				onClose();
			} else {
				setFocused(null);
				if (element instanceof CommandBlockEditor) {
					((CommandBlockEditor)element).setFocused(false);
				}
			}
			return true;
		} else {
				return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	// This must be overridden because the superclass' implementation
	// short-circuits on success, which breaks text field focus.
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		Element focusedChild = null;
		for (Element child : children()) {
			if (child.mouseClicked(mouseX, mouseY, button) && focusedChild == null) {
				focusedChild = child;
			}
		}
		setFocused(focusedChild);
		if (button == 0) {
			setDragging(true);
		}
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		for (CommandBlockEditor editor : editors) {
			if (editor.mouseScrolled(mouseX, mouseY, amount)) return true;
		}
		if (maxScrollOffset != 0 && amount != 0 && mouseY < height - 36) {
			setScrollOffset(getScrollOffset() - (int)Math.round(amount * SCROLL_SENSITIVITY));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public void setScrollOffset(int offset) {
		scrollOffset = MathHelper.clamp(offset, 0, maxScrollOffset);
		for (int index = 0; index < editors.size(); index++) {
			CommandBlockEditor editor = editors.get(index);
			editor.setY(20 * index + 8 - scrollOffset);
		}
	}

	@Override
	public boolean changeFocus(boolean lookForwards) {
		Element element = getFocused();
		if (element != null) {
			for (int index = 0; index < editors.size(); index++) {
				if (element instanceof CommandBlockEditor && element.equals(editors.get(index))) {
					CommandBlockEditor editor;
					do {
						index = index + (lookForwards ? 1 : -1);
						if (index < 0) {
							index = editors.size() - 1;
						} else if (index >= editors.size()) {
							index = 0;
						}
						editor = editors.get(index);
					} while (!editor.isLoaded());
					((CommandBlockEditor)element).setFocused(false);
					setFocusedEditor(editor);
					return true;
				}
			}
		}
		CommandBlockEditor editor = editors.get(0);
		setFocusedEditor(editor);
		return true;
	}

	public void setFocusedEditor(CommandBlockEditor editor) {
		setFocused(editor);
		editor.setFocused(true);
		setScrollOffset(MathHelper.clamp(getScrollOffset(), 20 * editor.index - height + 62, 20 * editor.index));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		for (int index = 0; index < editors.size(); index++) {
			String lineNumber = String.valueOf(index + 1);
			textRenderer.draw(matrices, lineNumber, 20 - textRenderer.getWidth(lineNumber), 20 * index + 13 - getScrollOffset(), index == startingIndex ? 0xFFFFFFFF : 0x7FFFFFFF);

			CommandBlockEditor editor = editors.get(index);
			editor.render(matrices, mouseX, mouseY, delta);
		}
		for (CommandBlockEditor editor : editors) {
			// This is done in a separate loop to ensure it's rendered on top.
			editor.renderSuggestions(matrices, mouseX, mouseY);
		}
		super.render(matrices, mouseX, mouseY, delta);
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
