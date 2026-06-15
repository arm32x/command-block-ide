package arm32x.minecraft.commandblockide.client.gui.button;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

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
		this.texture = Identifier.fromNamespaceAndPath("commandblockide", "textures/gui/icons/" + iconName + ".png");
		this.pressAction = pressAction;
	}

	@Override
	public void onPress(InputWithModifiers input) {
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
	protected MutableComponent createNarrationMessage() {
		return wrapDefaultNarrationMessage(Component.empty());
	}
}
