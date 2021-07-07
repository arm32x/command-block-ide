package arm32x.minecraft.commandblockide.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public abstract class CommandIDEScreen extends Screen {
	protected final List<CommandEditor> editors = new ArrayList<>();
	private boolean initialized = false;

	private ButtonWidget doneButton;
	private ButtonWidget applyButton;

	private int scrollOffset = 0, maxScrollOffset = Integer.MAX_VALUE;
	public static final double SCROLL_SENSITIVITY = 50.0;

	private boolean draggingScrollbar = false;
	private double mouseYAtScrollbarDragStart = 0;
	private int scrollOffsetAtScrollbarDragStart = 0;

	public CommandIDEScreen() {
		super(LiteralText.EMPTY);
	}

	@SuppressWarnings("CodeBlock2Expr")
	@Override
	protected void init() {
		assert client != null;
		client.keyboard.setRepeatEvents(true);

		doneButton = addDrawableChild(new ButtonWidget(this.width - 324, this.height - 28, 100, 20, ScreenTexts.DONE, (widget) -> {
			apply();
			onClose();
		}));
		/* cancelButton = */ addDrawableChild(new ButtonWidget(this.width - 216, this.height - 28, 100, 20, ScreenTexts.CANCEL, (widget) -> {
			onClose();
		}));
		applyButton = addDrawableChild(new ButtonWidget(this.width - 108, this.height - 28, 100, 20, new TranslatableText("commandBlockIDE.apply"), (widget) -> {
			apply();
		}));

		if (!initialized) {
			firstInit();
			initialized = true;
		} else {
			initAfterFirst();
		}
	}

	protected void firstInit() {
		setLoaded(false);

		maxScrollOffset = Math.max((editors.size() * 20 - 8) - (height - 50), 0);
		// Make sure the scroll offset is in range.
		setScrollOffset(getScrollOffset());
	}

	protected void initAfterFirst() {
		for (CommandEditor editor : editors) {
			addSelectableChild(editor);
			editor.setWidth(width - 16);
		}

		maxScrollOffset = Math.max((editors.size() * 20 - 8) - (height - 50), 0);
		Element element = getFocused();
		if (element instanceof CommandEditor) {
			setFocusedEditor((CommandEditor)element);
		}
	}

	protected void addEditor(CommandEditor editor) {
		editors.add(editor);
		addSelectableChild(editor);
	}

	public abstract void apply();

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
				if (element instanceof CommandEditor) {
					((CommandEditor)element).setFocused(false);
				}
			}
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			Element element = getFocused();
			if (element == null) {
				apply();
				onClose();
			} else {
				setFocused(null);
				if (element instanceof CommandEditor) {
					((CommandEditor)element).setFocused(false);
				}
			}
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_TAB && !hasControlDown()) {
			Element focused = getFocused();
			if (focused != null && focused.keyPressed(keyCode, scanCode, modifiers)) {
				return true;
			} else {
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		} else {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	// This must be overridden because the superclass' implementation
	// short-circuits on success, which breaks text field focus.
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseX > width - 4 && button == 0) {
			setDragging(true);
			draggingScrollbar = true;
			mouseYAtScrollbarDragStart = mouseY;
			scrollOffsetAtScrollbarDragStart = getScrollOffset();
			return true;
		}

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
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0 && draggingScrollbar) {
			draggingScrollbar = false;
			return true;
		} else {
			return super.mouseReleased(mouseX, mouseY, button);
		}
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (button == 0 && draggingScrollbar) {
			int virtualHeight = maxScrollOffset + height;
			int scrollbarHeight = Math.round((float)height / virtualHeight * height);
			int scrollOffsetDelta = (int)Math.round((mouseY - mouseYAtScrollbarDragStart) / scrollbarHeight * height);
			setScrollOffset(scrollOffsetAtScrollbarDragStart + scrollOffsetDelta);
			return true;
		} else {
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		for (CommandEditor editor : editors) {
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
			CommandEditor editor = editors.get(index);
			editor.setY(20 * index + 8 - scrollOffset);
		}
	}

	@Override
	public boolean changeFocus(boolean lookForwards) {
		Element element = getFocused();
		if (element != null) {
			for (int index = 0; index < editors.size(); index++) {
				if (element instanceof CommandEditor && element.equals(editors.get(index))) {
					CommandEditor editor;
					do {
						index = index + (lookForwards ? 1 : -1);
						if (index < 0) {
							index = editors.size() - 1;
						} else if (index >= editors.size()) {
							index = 0;
						}
						editor = editors.get(index);
					} while (!editor.isLoaded());
					((CommandEditor)element).setFocused(false);
					setFocusedEditor(editor);
					return true;
				}
			}
		}
		CommandEditor editor = editors.get(0);
		setFocusedEditor(editor);
		return true;
	}

	public void setFocusedEditor(CommandEditor editor) {
		setFocused(editor);
		editor.setFocused(true);
		setScrollOffset(MathHelper.clamp(getScrollOffset(), 20 * editor.index - height + 62, 20 * editor.index));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);

		for (CommandEditor editor : editors) {
			editor.render(matrices, mouseX, mouseY, delta);
		}
		for (CommandEditor editor : editors) {
			// This is done in a separate loop to ensure it's rendered on top.
			editor.renderSuggestions(matrices, mouseX, mouseY);
		}

		if (maxScrollOffset > 0) {
			int virtualHeight = maxScrollOffset + height;
			int scrollbarHeight = Math.round((float)height / virtualHeight * height);
			int scrollbarPosition = Math.round((float)getScrollOffset() / height * scrollbarHeight);
			fill(matrices, width - 3, scrollbarPosition + 1, width - 1, scrollbarPosition + scrollbarHeight - 1, 0x3FFFFFFF);
		}

		matrices.push();
		matrices.translate(0.0, 0.0, 10.0);
		super.render(matrices, mouseX, mouseY, delta);
		matrices.pop();
	}

	public boolean isLoaded() {
		return doneButton.active && applyButton.active;
	}

	protected void setLoaded(boolean loaded) {
		doneButton.active = loaded;
		applyButton.active = loaded;
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
