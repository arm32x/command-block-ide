package arm32x.minecraft.commandblockide.client.gui.button;

import arm32x.minecraft.commandblockide.client.Dirtyable;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.render.BufferBuilder.BuiltBuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public final class CommandBlockTypeButton extends IconButton implements Dirtyable {
	public CommandBlockBlockEntity.Type type = CommandBlockBlockEntity.Type.REDSTONE;
	public boolean conditional = false;

	private final Screen screen;

	private boolean dirty = false;

	public CommandBlockTypeButton(Screen screen, int x, int y) {
		super(x, y, 16, 16);
		this.screen = screen;
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
	}

	@Override
	public MutableText getNarrationMessage() {
		return getNarrationMessage(getTooltip());
	}

	private Text getTooltip() {
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
		return new Identifier("minecraft", idBuilder.toString());
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.setShaderTexture(0, getTexture());

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		if (active) {
			RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 0.25f);
			drawTexture(matrices, x + 1, y + 1, 0.0f, 0.0f, width, height, 16, 64);
		}
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, active ? 1.0f : 0.5f);

		// Drawing must be done manually in order to flip the texture upside-down.
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		float x0 = (float)x, x1 = x0 + 16, y0 = (float)y, y1 = y0 + 16, z = getZOffset();
		float u0 = 0.0f, u1 = 1.0f, v0 = 0.0f, v1 = 0.25f;
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(matrix, x0, y1, z).texture(u1, v0).next();
		bufferBuilder.vertex(matrix, x1, y1, z).texture(u0, v0).next();
		bufferBuilder.vertex(matrix, x1, y0, z).texture(u0, v1).next();
		bufferBuilder.vertex(matrix, x0, y0, z).texture(u1, v1).next();
		var builtBuffer = bufferBuilder.end();
		BufferRenderer.drawWithShader(builtBuffer);

		if (isHovered()) {
			renderTooltip(matrices, mouseX, mouseY);
		}
	}

	@Override
	public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
		screen.renderOrderedTooltip(matrices, Collections.singletonList(getTooltip().asOrderedText()), mouseX, mouseY);
	}

	@Override
	public boolean isDirty() { return dirty; }
}
