package arm32x.minecraft.commandblockide.client.gui.button;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import arm32x.minecraft.commandblockide.mixin.client.DrawContextAccessor;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class CommandBlockTypeButton extends IconButton implements Dirtyable {
	private CommandBlockBlockEntity.Type type = CommandBlockBlockEntity.Type.REDSTONE;
	private boolean conditional = false;

	private boolean dirty = false;

	public CommandBlockTypeButton(int x, int y) {
		super(x, y, 16, 16);
		updateTooltip();
	}

	@Override
	public void onPress(AbstractInput input) {
		if (input.hasShift()) {
			conditional = !conditional;
		} else {
			switch (type) {
				case REDSTONE -> type = CommandBlockBlockEntity.Type.AUTO;
				case AUTO -> type = CommandBlockBlockEntity.Type.SEQUENCE;
				case SEQUENCE -> type = CommandBlockBlockEntity.Type.REDSTONE;
			}
		}
		dirty = true;
		updateTooltip();
	}

	@Override
	public MutableText getNarrationMessage() {
		return getNarrationMessage(getTooltipText());
	}

	private Text getTooltipText() {
		StringBuilder keyBuilder = new StringBuilder("commandBlockIDE.type.");
		keyBuilder.append(type.name().toLowerCase());
		if (conditional) {
			keyBuilder.append("Conditional");
		}
		return Text.translatable(keyBuilder.toString());
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
		return Identifier.of("minecraft", idBuilder.toString());
	}

	@Override
	public void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
		var texture = getTexture();

		int color = active ? 0xFFFFFFFF : 0x7FFFFFFF;
		int shadowColor = 0x3F000000;

		if (active) {
			context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, getX() + 1, getY() + 1, 0, 0, iconWidth, iconHeight, 16, 64, shadowColor);
		}

        // The public version of drawTexturedQuad doesn't expose the color
        // parameter, which we need, so we have to use the private version.
		int x1 = getX(), x2 = x1 + 16, y1 = getY(), y2 = y1 + 16;
		float u1 = 0.0f, u2 = 1.0f, v1 = 0.0f, v2 = 0.25f;
		((DrawContextAccessor)context).invokeDrawTexturedQuad(RenderPipelines.GUI_TEXTURED, texture, x1, x2, y1, y2, u2, u1, v2, v1, color);
	}

	@Override
	public boolean isDirty() { return dirty; }

	private void updateTooltip() {
		setTooltip(Tooltip.of(getTooltipText()));
	}

	public CommandBlockBlockEntity.Type getBlockType() {
		return type;
	}

	public void setBlockType(CommandBlockBlockEntity.Type type) {
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
