package arm32x.minecraft.commandblockide.client.gui.screen;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import arm32x.minecraft.commandblockide.client.gui.ToolbarSeparator;
import arm32x.minecraft.commandblockide.client.gui.button.SimpleIconButton;
import arm32x.minecraft.commandblockide.client.gui.editor.CommandEditor;
import arm32x.minecraft.commandblockide.client.storage.MultilineCommandStorage;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public abstract class CommandIDEScreen<E extends CommandEditor>
		extends Screen
		implements Dirtyable {

	protected final List<E> editors = new ArrayList<>();

	protected int combinedEditorHeight = Integer.MAX_VALUE;

	private boolean initialized = false;

	private SimpleIconButton saveButton;

	private int scrollOffset = 0;
	private int maxScrollOffset = Integer.MAX_VALUE;

	public static final double SCROLL_SENSITIVITY = 50.0;

	private boolean draggingScrollbar = false;

	private double mouseYAtScrollbarDragStart = 0;

	private int scrollOffsetAtScrollbarDragStart = 0;

	protected @Nullable Component statusText = null;

	private int statusTextX = 0;

	public CommandIDEScreen() {
		super(Component.empty());
	}

	@Override
	protected void init() {
		statusTextX = addToolbarWidgets(
				List.of(
						saveButton =
								new SimpleIconButton(
										0,
										0,
										"save",
										Tooltip.create(
												Component.translatable(
														"commandBlockIDE.save"
												)
										),
										b -> save()
								),
						new ToolbarSeparator()
				)
		);

		// Done button
		addRenderableWidget(
				Button.builder(
								CommonComponents.GUI_DONE,
								b -> {
									save();
									onClose();
								}
						)
						.pos(width - 216, height - 28)
						.size(100, 20)
						.build()
		);

		// Cancel button
		addRenderableWidget(
				Button.builder(
								CommonComponents.GUI_CANCEL,
								b -> onClose()
						)
						.pos(width - 108, height - 28)
						.size(100, 20)
						.build()
		);

		if (!initialized) {
			firstInit();
			initialized = true;
		} else {
			initAfterFirst();
		}
	}

	protected void firstInit() {
		setLoaded(false);

		repositionEditors();

		maxScrollOffset =
				Math.max(
						combinedEditorHeight - (height - 50),
						0
				);

		setScrollOffset(getScrollOffset());

		MultilineCommandStorage.load();
	}

	protected void initAfterFirst() {
		for (CommandEditor editor : editors) {
			addWidget(editor);
			editor.setWidth(width - 16);
		}

		maxScrollOffset =
				Math.max(
						combinedEditorHeight - (height - 50),
						0
				);

		GuiEventListener element = getFocused();

		if (element instanceof CommandEditor) {
			setFocusedEditor((CommandEditor) element);
		}
	}

	protected void addEditor(E editor) {
		editor.setHeightChangedListener(height -> {
			repositionEditors();

			setScrollOffset(
					Math.min(
							scrollOffset,
							combinedEditorHeight - 20
					)
			);
		});

		editors.add(editor);
		addWidget(editor);
	}

	public void save() {
		MultilineCommandStorage.save();
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		if (handleSpecialKey(input)) {
			return true;
		} else if (getFocused() != null) {
			return getFocused().keyPressed(input);
		} else {
			return false;
		}
	}

	private boolean handleSpecialKey(KeyEvent input) {
		if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
			GuiEventListener focused = getFocused();

			if (focused == null) {
				onClose();
				return true;
			}

			if (focused instanceof CommandEditor editor) {
				if (editor.isSuggestorActive()) {
					editor.setSuggestorActive(false);
					return true;
				} else {
					editor.setFocused(false);
				}
			}

			setFocused(null);
			return true;

		} else if (
				input.key() == GLFW.GLFW_KEY_ENTER
						|| input.key() == GLFW.GLFW_KEY_KP_ENTER
		) {
			GuiEventListener focused = getFocused();

			if (focused == null) {
				save();
				onClose();
				return true;
			}

			if (
					input.hasControlDown()
							&& focused instanceof CommandEditor editor
			) {
				if (editor.isSuggestorActive()) {
					editor.setSuggestorActive(false);
					return true;
				} else {
					editor.setFocused(false);
				}

				setFocused(null);
				return true;
			}

			return false;

		} else if (
				input.key() == GLFW.GLFW_KEY_UP
						&& input.hasControlDown()
						|| input.key() == GLFW.GLFW_KEY_TAB
						&& input.hasControlDown()
						&& input.hasShiftDown()
		) {
			changeFocus(false);
			return true;

		} else if (
				input.key() == GLFW.GLFW_KEY_DOWN
						&& input.hasControlDown()
						|| input.key() == GLFW.GLFW_KEY_TAB
						&& input.hasControlDown()
						&& !input.hasShiftDown()
		) {
			changeFocus(true);
			return true;

		} else if (
				input.key() == GLFW.GLFW_KEY_S
						&& input.hasControlDown()
		) {
			saveButton.playDownSound(
					Minecraft.getInstance().getSoundManager()
			);

			save();
			return true;

		} else {
			return false;
		}
	}

	@Override
	public boolean mouseClicked(
			MouseButtonEvent click,
			boolean doubled
	) {
		if (
				click.x() > width - 4
						&& click.button() == 0
		) {
			int virtualHeight = maxScrollOffset + height;

			int scrollbarHeight =
					Math.round(
							(float) height
									/ virtualHeight
									* height
					);

			int scrollbarPosition =
					Math.round(
							(float) getScrollOffset()
									/ height
									* scrollbarHeight
					);

			if (
					click.y() >= scrollbarPosition
							&& click.y()
							<= scrollbarPosition
							+ scrollbarHeight
			) {
				setDragging(true);

				draggingScrollbar = true;

				mouseYAtScrollbarDragStart = click.y();

				scrollOffsetAtScrollbarDragStart =
						getScrollOffset();

			} else if (click.y() < scrollbarPosition) {
				setScrollOffset(
						(int) Math.round(
								getScrollOffset()
										- SCROLL_SENSITIVITY * 5
						)
				);

			} else if (
					click.y()
							> scrollbarPosition
							+ scrollbarHeight
			) {
				setScrollOffset(
						(int) Math.round(
								getScrollOffset()
										+ SCROLL_SENSITIVITY * 5
						)
				);
			}

			return true;
		}

		GuiEventListener focusedChild = null;

		for (GuiEventListener child : children()) {
			if (
					child.mouseClicked(click, doubled)
							&& focusedChild == null
			) {
				focusedChild = child;
			}
		}

		setFocused(focusedChild);

		if (click.button() == 0) {
			setDragging(true);
		}

		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent click) {
		if (
				click.button() == 0
						&& draggingScrollbar
		) {
			draggingScrollbar = false;
			return true;
		} else {
			return super.mouseReleased(click);
		}
	}

	@Override
	public boolean mouseDragged(
			MouseButtonEvent click,
			double offsetX,
			double offsetY
	) {
		if (
				click.button() == 0
						&& draggingScrollbar
		) {
			int virtualHeight = maxScrollOffset + height;

			int scrollbarHeight =
					Math.round(
							(float) height
									/ virtualHeight
									* height
					);

			int scrollOffsetDelta =
					(int) Math.round(
							(
									click.y()
											- mouseYAtScrollbarDragStart
							)
									/ scrollbarHeight
									* height
					);

			setScrollOffset(
					scrollOffsetAtScrollbarDragStart
							+ scrollOffsetDelta
			);

			return true;
		} else {
			return super.mouseDragged(
					click,
					offsetX,
					offsetY
			);
		}
	}

	@Override
	public boolean mouseScrolled(
			double mouseX,
			double mouseY,
			double horizontalAmount,
			double verticalAmount
	) {
		for (CommandEditor editor : editors) {
			if (
					editor.mouseScrolled(
							mouseX,
							mouseY,
							horizontalAmount,
							verticalAmount
					)
			) {
				return true;
			}
		}

		if (
				verticalAmount != 0
						&& mouseY < height - 36
		) {
			setScrollOffset(
					getScrollOffset()
							- (int) Math.round(
							verticalAmount
									* SCROLL_SENSITIVITY
					)
			);

			return true;
		}

		return super.mouseScrolled(
				mouseX,
				mouseY,
				horizontalAmount,
				verticalAmount
		);
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public void setScrollOffset(int offset) {
		int effectiveMaxScrollOffset =
				Math.max(
						scrollOffset,
						maxScrollOffset
				);

		scrollOffset =
				Mth.clamp(
						offset,
						0,
						effectiveMaxScrollOffset
				);

		repositionEditors();
	}

	protected void repositionEditors() {
		int heightAccumulator = 8;

		for (CommandEditor editor : editors) {
			editor.setY(
					heightAccumulator - scrollOffset
			);

			heightAccumulator +=
					editor.getHeight() + 4;
		}

		combinedEditorHeight =
				heightAccumulator - 12;

		maxScrollOffset =
				Math.max(
						combinedEditorHeight
								- (height - 50),
						0
				);
	}

	private int addToolbarWidgets(
			List<AbstractWidget> widgets
	) {
		int x = 8;

		for (AbstractWidget widget : widgets) {
			widget.setX(x);
			widget.setY(height - 28);

			x += widget.getWidth() + 4;

			addRenderableWidget(widget);
		}

		return x;
	}

	private void changeFocus(boolean lookForwards) {
		GuiEventListener element = getFocused();

		if (element == null) {
			CommandEditor editor = editors.get(0);
			setFocusedEditor(editor);
			return;
		}

		for (int index = 0; index < editors.size(); index++) {
			if (
					element instanceof CommandEditor
							&& element.equals(editors.get(index))
			) {
				CommandEditor editor;

				do {
					index =
							index
									+ (
									lookForwards
											? 1
											: -1
							);

					if (index < 0) {
						index = editors.size() - 1;
					} else if (
							index >= editors.size()
					) {
						index = 0;
					}

					editor = editors.get(index);

				} while (!editor.isLoaded());

				element.setFocused(false);
				setFocusedEditor(editor);

				return;
			}
		}
	}

	public void setFocusedEditor(CommandEditor editor) {
		setFocused(editor);
		editor.setFocused(true);

		repositionEditors();

		int top =
				editor.getY()
						+ scrollOffset;

		int bottom =
				top + editor.getHeight();

		setScrollOffset(
				Mth.clamp(
						getScrollOffset(),
						bottom - height + 36,
						top - 8
				)
		);
	}

	@Override
	public void extractBackground(
			GuiGraphicsExtractor context,
			int mouseX,
			int mouseY,
			float delta
	) {
		super.extractBackground(context, mouseX, mouseY, delta);
	}

	@Override
	public void extractRenderState(
			GuiGraphicsExtractor context,
			int mouseX,
			int mouseY,
			float delta
	) {
		// Render the command editors manually because they are registered
		// with addWidget() for input handling, not as renderable children.
		for (CommandEditor editor : editors) {
			editor.extractRenderState(
					context,
					mouseX,
					mouseY,
					delta
			);
		}

		// Suggestions need to be rendered after the editors so that they
		// appear on top of the command fields/buttons.
		for (CommandEditor editor : editors) {
			editor.renderSuggestions(
					context,
					mouseX,
					mouseY
			);
		}

		if (maxScrollOffset > 0) {
			int virtualHeight =
					maxScrollOffset + height;

			int scrollbarHeight =
					Math.round(
							(float) height
									/ virtualHeight
									* height
					);

			int scrollbarPosition =
					Math.round(
							(float) getScrollOffset()
									/ height
									* scrollbarHeight
					);

			context.fill(
					width - 3,
					scrollbarPosition + 1,
					width - 1,
					scrollbarPosition
							+ scrollbarHeight
							- 1,
					0x3FFFFFFF
			);
		}

		// Render normal Screen widgets such as Save, Done and Cancel.
		super.extractRenderState(
				context,
				mouseX,
				mouseY,
				delta
		);

		if (statusText != null) {
			int x = statusTextX + 5;
			int y = height - 22;

			int statusTextWidth =
					font.width(statusText);

			context.fill(
					x - 2,
					y - 2,
					x + statusTextWidth + 2,
					y + 9 + 2,
					0x7F000000
			);

			context.text(
					font,
					statusText,
					statusTextX + 5,
					height - 22,
					0xFFFFFFFF,
					false
			);
		}
	}

	@Override
	public boolean isDirty() {
		return editors.stream()
				.anyMatch(Dirtyable::isDirty);
	}

	public boolean isLoaded() {
		return saveButton.active;
	}

	protected void setLoaded(boolean loaded) {
		saveButton.active = loaded;
	}
}