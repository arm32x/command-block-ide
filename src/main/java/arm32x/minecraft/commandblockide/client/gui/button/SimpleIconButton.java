package arm32x.minecraft.commandblockide.client.gui.button;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class SimpleIconButton extends IconButton {
	private boolean drawsBackground;
	private Identifier texture;
	private final Consumer<SimpleIconButton> pressAction;

	public SimpleIconButton(int x, int y, String iconName, Tooltip tooltip, Consumer<SimpleIconButton> pressAction) {
		this(x, y, iconName, tooltip, true, pressAction);
	}

	public SimpleIconButton(int x, int y, String iconName, Tooltip tooltip, boolean drawsBackground, Consumer<SimpleIconButton> pressAction) {
		super(x, y, 20, 20, 16, 16);
		this.drawsBackground = drawsBackground;
		this.texture = new Identifier("commandblockide", "textures/gui/icons/" + iconName + ".png");
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

	@Override
	public boolean drawsBackground() {
		return drawsBackground;
	}

	public void setDrawsBackground(boolean drawsBackground) {
		this.drawsBackground = drawsBackground;
	}

	@Override
	protected MutableText getNarrationMessage() {
		return getNarrationMessage(Text.empty());
	}
}
