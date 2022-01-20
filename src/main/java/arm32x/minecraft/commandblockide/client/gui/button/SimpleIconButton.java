package arm32x.minecraft.commandblockide.client.gui.button;

import java.util.function.Consumer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class SimpleIconButton extends IconButton {
	private boolean drawsBackground;
	private Identifier texture;
	private @Nullable Text tooltip;
	private final Consumer<SimpleIconButton> pressAction;

	public SimpleIconButton(int x, int y, String iconName, @Nullable Text tooltip, Consumer<SimpleIconButton> pressAction) {
		this(x, y, iconName, tooltip, true, pressAction);
	}

	public SimpleIconButton(int x, int y, String iconName, @Nullable Text tooltip, boolean drawsBackground, Consumer<SimpleIconButton> pressAction) {
		super(x, y, 16, 16);
		this.drawsBackground = drawsBackground;
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

	public @Nullable Text getTooltip() {
		return tooltip;
	}

	public void setTooltip(@Nullable Text tooltip) {
		this.tooltip = tooltip;
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
		return getNarrationMessage(tooltip);
	}
}
