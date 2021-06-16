package arm32x.minecraft.commandblockide.gui;

import arm32x.minecraft.commandblockide.gui.widget.WBorderPanel;
import arm32x.minecraft.commandblockide.gui.widget.WCommandEditor;
import arm32x.minecraft.commandblockide.gui.widget.WMarginPanel;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CommandBlockIDEScreen extends CottonClientScreen {
	public CommandBlockIDEScreen(CommandBlockBlockEntity commandBlock) {
		super(new Description());
		((Description)description).screen = this;
	}

	public static final class Description extends LightweightGuiDescription {
		private CommandBlockIDEScreen screen;

		@SuppressWarnings("CodeBlock2Expr")
		private Description() {
			setFullscreen(true);

			WMarginPanel root = new WMarginPanel();
			setRootPanel(root);
			root.setMargin(8);

			WBorderPanel main = new WBorderPanel();
			root.setChild(main);
			main.setSpacing(8);
			main.setSize(320, 200);

			WBox editors = new WBox(Axis.VERTICAL);
			main.add(editors, WBorderPanel.Position.CENTER);
			editors.setSpacing(8);

			WCommandEditor editor = new WCommandEditor(1) { };
			editors.add(editor, 500, 20);

			WBox bottomPanel = new WBox(Axis.HORIZONTAL);
			main.add(bottomPanel, WBorderPanel.Position.BOTTOM);
			bottomPanel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
			bottomPanel.setSpacing(8);

			WButton doneButton = new WButton(ScreenTexts.DONE);
			bottomPanel.add(doneButton);
			doneButton.setSize(100, 20);
			doneButton.setOnClick(() -> {
				screen.onClose();
			});

			WButton cancelButton = new WButton(ScreenTexts.CANCEL);
			bottomPanel.add(cancelButton);
			cancelButton.setSize(100, 20);
			cancelButton.setOnClick(() -> {
				screen.onClose();
			});

			WButton applyAllButton = new WButton(new TranslatableText("commandBlockIDE.applyAll"));
			bottomPanel.add(applyAllButton);
			applyAllButton.setSize(100, 20);
			applyAllButton.setOnClick(() -> { });

			main.validate(this);
		}
	}

	@Override
	public void init() {
		super.init();
		WPanel root = description.getRootPanel();
		if (root != null) {
			LOGGER.info("Laying out root panel of size {}x{}.", root.getWidth(), root.getHeight());
			root.layout();
		}
	}

	private static final Logger LOGGER = LogManager.getLogger();
}