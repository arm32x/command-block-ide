package arm32x.minecraft.commandblockide.gui.legacy;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public abstract class DynamicTexturedButton extends AbstractPressableButtonWidget {
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
		MinecraftClient client = MinecraftClient.getInstance();
		client.getTextureManager().bindTexture(getTexture());

		RenderSystem.color4f(1.0f, 1.0f, 1.0f, active ? 1.0f : 0.5f);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		drawTexture(matrices, x, y, (float)u, (float)v, width, height, textureWidth, textureHeight);
		if (this.isHovered()) {
			this.renderToolTip(matrices, mouseX, mouseY);
		}
	}
}
