package arm32x.minecraft.commandblockide.client.gui.button;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public final class CommandBlockTypeButton extends IconButton implements Dirtyable {
	private CommandBlockEntity.Mode type = CommandBlockEntity.Mode.REDSTONE;
	private boolean conditional = false;

	private boolean dirty = false;

	public CommandBlockTypeButton(int x, int y) {
		super(x, y, 16, 16);
		updateTooltip();
	}

	@Override
	public void onPress(InputWithModifiers input) {
		if (input.hasShiftDown()) {
			conditional = !conditional;
		} else {
			switch (type) {
				case REDSTONE -> type = CommandBlockEntity.Mode.AUTO;
				case AUTO -> type = CommandBlockEntity.Mode.SEQUENCE;
				case SEQUENCE -> type = CommandBlockEntity.Mode.REDSTONE;
			}
		}
		dirty = true;
		updateTooltip();
	}

	@Override
	public MutableComponent createNarrationMessage() {
		return wrapDefaultNarrationMessage(getTooltipText());
	}

	private Component getTooltipText() {
		StringBuilder keyBuilder = new StringBuilder("commandBlockIDE.type.");
		keyBuilder.append(type.name().toLowerCase());
		if (conditional) {
			keyBuilder.append("Conditional");
		}
		return Component.translatable(keyBuilder.toString());
	}

	@Override
	protected Identifier getTexture() {
		StringBuilder idBuilder = new StringBuilder("textures/block/");
		switch (type) {
			case REDSTONE:
				break;
			case AUTO:
				idBuilder.append("repeating_");
				break;
			case SEQUENCE:
				idBuilder.append("chain_");
				break;
		}
		idBuilder.append("command_block_");
		if (conditional) {
			idBuilder.append("conditional");
		} else {
			idBuilder.append("side");
		}
		idBuilder.append(".png");
		return Identifier.fromNamespaceAndPath("minecraft", idBuilder.toString());
	}

	@Override
	public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		var texture = getTexture();

		int color = active ? 0xFFFFFFFF : 0x7FFFFFFF;
		int shadowColor = 0x3F000000;

		if (active) {
			context.blit(RenderPipelines.GUI_TEXTURED, texture, getX() + 1, getY() + 1, 0, 0, iconWidth, iconHeight, 16, 64, shadowColor);
		}

		context.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), 0, 0, iconWidth, iconHeight, 16, 64, color);
	}

	@Override
	public boolean isDirty() { return dirty; }

	private void updateTooltip() {
		setTooltip(Tooltip.create(getTooltipText()));
	}

	public CommandBlockEntity.Mode getBlockType() {
		return type;
	}

	public void setBlockType(CommandBlockEntity.Mode type) {
		this.type = type;
		updateTooltip();
	}

	public boolean isConditional() {
		return conditional;
	}

	public void setConditional(boolean conditional) {
		this.conditional = conditional;
		updateTooltip();
	}
}
