package arm32x.minecraft.commandblockide.client.gui.button;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import arm32x.minecraft.commandblockide.mixin.client.DrawContextAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public final class CommandBlockTypeButton extends IconButton implements Dirtyable {
	private CommandBlockBlockEntity.Type type = CommandBlockBlockEntity.Type.REDSTONE;
	private boolean conditional = false;

	private boolean dirty = false;

	public CommandBlockTypeButton(int x, int y) {
		super(x, y, 16, 16);
		updateTooltip();
	}

	@Override
	public void onPress() {
		if (Screen.hasShiftDown()) {
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
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		var texture = getTexture();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();

		int color = active ? 0xFFFFFFFF : 0x7FFFFFFF;
		int shadowColor = 0x3F000000;

		if (active) {
			context.drawTexture(RenderLayer::getGuiTextured, texture, getX() + 1, getY() + 1, 0, 0, iconWidth, iconHeight, 16, 64, shadowColor);
		}

		// To flip the texture, we pass u2, u1, v2, v1 instead of the usual
		// u1, u2, v1, v2. This is why we have to use context.drawTexturedQuad
		// instead of going through context.drawTexture.
		int x1 = getX(), x2 = x1 + 16, y1 = getY(), y2 = y1 + 16;
		float u1 = 0.0f, u2 = 1.0f, v1 = 0.0f, v2 = 0.25f;
		((DrawContextAccessor)context).invokeDrawTexturedQuad(RenderLayer::getGuiTextured, texture, x1, x2, y1, y2, u2, u1, v2, v1, color);

		RenderSystem.disableDepthTest();
		RenderSystem.disableBlend();
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
