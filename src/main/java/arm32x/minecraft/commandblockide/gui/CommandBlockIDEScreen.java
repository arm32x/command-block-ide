package arm32x.minecraft.commandblockide.gui;

import arm32x.minecraft.commandblockide.gui.widget.WBorderPanel;
import arm32x.minecraft.commandblockide.gui.widget.WMarginPanel;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
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
	}

	public static final class Description extends LightweightGuiDescription {
		public Description() {
			setFullscreen(true);

			WMarginPanel root = new WMarginPanel();
			setRootPanel(root);
			root.setMargin(8);

			WBorderPanel main = new WBorderPanel();
			root.setChild(main);
			main.setSpacing(8);
			main.setSize(320, 200);

			WWidget center = new WWidget();
			main.add(center, WBorderPanel.Position.CENTER);

			WBox bottomPanel = new WBox(Axis.HORIZONTAL);
			main.add(bottomPanel, WBorderPanel.Position.PAGE_END);
			bottomPanel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
			bottomPanel.setSpacing(8);

			WButton doneButton = new WButton(ScreenTexts.DONE);
			bottomPanel.add(doneButton);
			doneButton.setSize(100, 20);
			doneButton.setOnClick(() -> LOGGER.info("Done button clicked."));

			WButton cancelButton = new WButton(ScreenTexts.CANCEL);
			bottomPanel.add(cancelButton);
			cancelButton.setSize(100, 20);
			cancelButton.setOnClick(() -> LOGGER.info("Cancel button clicked."));

			WButton applyAllButton = new WButton(new TranslatableText("commandBlockIDE.applyAll"));
			bottomPanel.add(applyAllButton);
			applyAllButton.setSize(100, 20);
			applyAllButton.setOnClick(() -> LOGGER.info("Apply all button clicked."));

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