package arm32x.minecraft.commandblockide.gui.legacy;

import arm32x.minecraft.commandblockide.Dirtyable;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public final class CommandBlockTypeButton extends DynamicTexturedButton implements Dirtyable {
	public CommandBlockBlockEntity.Type type = CommandBlockBlockEntity.Type.REDSTONE;
	public boolean conditional = false;

	private final Screen screen;

	private boolean dirty = false;

	public CommandBlockTypeButton(Screen screen, int x, int y) {
		super(x, y, 16, 16, 0, 0, 16, 64);
		this.screen = screen;
	}

	@Override
	public void onPress() {
		if (Screen.hasShiftDown()) {
			conditional = !conditional;
		} else {
			switch (type) {
				case REDSTONE:
					type = CommandBlockBlockEntity.Type.AUTO;
					break;
				case AUTO:
					type = CommandBlockBlockEntity.Type.SEQUENCE;
					break;
				case SEQUENCE:
					type = CommandBlockBlockEntity.Type.REDSTONE;
					break;
			}
		}
		markDirty();
	}

	@Override
	public Text getMessage() {
		StringBuilder keyBuilder = new StringBuilder("commandBlockIDE.type.");
		keyBuilder.append(type.name().toLowerCase());
		if (conditional) {
			keyBuilder.append("Conditional");
		}
		return new TranslatableText(keyBuilder.toString());
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
		MinecraftClient client = MinecraftClient.getInstance();
		client.getTextureManager().bindTexture(getTexture());

		RenderSystem.color4f(1.0f, 1.0f, 1.0f, active ? 1.0f : 0.5f);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();

		// Drawing must be done manually in order to flip the texture upside-down.
		Matrix4f matrix = matrices.peek().getModel();
		float x0 = (float)x, x1 = x0 + 16, y0 = (float)y, y1 = y0 + 16, z = getZOffset();
		float u0 = 0.0f, u1 = 1.0f, v0 = 0.0f, v1 = 0.25f;
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(matrix, x0, y1, z).texture(u1, v0).next();
		bufferBuilder.vertex(matrix, x1, y1, z).texture(u0, v0).next();
		bufferBuilder.vertex(matrix, x1, y0, z).texture(u0, v1).next();
		bufferBuilder.vertex(matrix, x0, y0, z).texture(u1, v1).next();
		bufferBuilder.end();
		RenderSystem.enableAlphaTest();
		BufferRenderer.draw(bufferBuilder);

		if (isHovered()) {
			renderToolTip(matrices, mouseX, mouseY);
		}
	}

	@Override
	public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
		screen.renderOrderedTooltip(matrices, Collections.singletonList(getMessage().asOrderedText()), mouseX, mouseY);
	}

	@Override
	public boolean isDirty() { return dirty; }

	@Override
	public void markDirty() { dirty = true; }
}
