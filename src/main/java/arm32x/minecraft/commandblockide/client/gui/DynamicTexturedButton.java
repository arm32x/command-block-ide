package arm32x.minecraft.commandblockide.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public abstract class DynamicTexturedButton extends PressableWidget {
	private final int u, v, textureWidth, textureHeight;

	public DynamicTexturedButton(int x, int y, int width, int height, int u, int v, int textureWidth, int textureHeight) {
		super(x, y, width, height, LiteralText.EMPTY);
		this.u = u;
		this.v = v;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	protected abstract Identifier getTexture();

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.setShaderTexture(0, getTexture());

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		if (active) {
			RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 0.25f);
			drawTexture(matrices, x + 1, y + 1, (float)u, (float)v, width, height, textureWidth, textureHeight);
		}
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, active ? 1.0f : 0.5f);
		drawTexture(matrices, x, y, (float)u, (float)v, width, height, textureWidth, textureHeight);
		if (this.isHovered()) {
			this.renderTooltip(matrices, mouseX, mouseY);
		}
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
}
