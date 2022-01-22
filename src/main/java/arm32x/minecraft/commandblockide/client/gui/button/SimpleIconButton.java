package arm32x.minecraft.commandblockide.client.gui.button;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class SimpleIconButton extends IconButton {
	private boolean drawsBackground;
	private Identifier texture;
	private final @Nullable Screen screen;
	private List<Text> tooltip;
	private final Consumer<SimpleIconButton> pressAction;

	public SimpleIconButton(int x, int y, String iconName, @Nullable Screen screen, List<Text> tooltip, Consumer<SimpleIconButton> pressAction) {
		this(x, y, iconName, screen, tooltip, true, pressAction);
	}

	public SimpleIconButton(int x, int y, String iconName, @Nullable Screen screen, List<Text> tooltip, boolean drawsBackground, Consumer<SimpleIconButton> pressAction) {
		super(x, y, 20, 20, 16, 16);
		this.drawsBackground = drawsBackground;
		this.screen = screen;
		this.texture = new Identifier("commandblockide", "textures/gui/icons/" + iconName + ".png");
		this.tooltip = tooltip;
		this.pressAction = pressAction;
	}

	@Override
	public void onPress() {
		pressAction.accept(this);
	}

	@Override
	protected Identifier getTexture() {
		return texture;
	}

	public void setTexture(Identifier texture) {
		this.texture = texture;
	}

	public List<Text> getTooltip() {
		return tooltip;
	}

	public void setTooltip(List<Text> tooltip) {
		this.tooltip = tooltip;
	}

	@Override
	public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
		if (screen != null) {
			screen.renderOrderedTooltip(matrices, getTooltip().stream().map(Text::asOrderedText).collect(Collectors.toList()), mouseX, mouseY);
		}
	}

	@Override
	public boolean drawsBackground() {
		return drawsBackground;
	}

	public void setDrawsBackground(boolean drawsBackground) {
		this.drawsBackground = drawsBackground;
	}

	@Override
	protected MutableText getNarrationMessage() {
		return getNarrationMessage(getTooltip().stream().findFirst().orElse(LiteralText.EMPTY));
	}
}
