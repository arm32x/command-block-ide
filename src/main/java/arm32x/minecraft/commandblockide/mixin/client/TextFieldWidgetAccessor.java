package arm32x.minecraft.commandblockide.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor {
	@Accessor boolean getDrawsBackground();
	@Accessor boolean isEditable();
	@Accessor int getEditableColor();
	@Accessor int getUneditableColor();
	@Accessor TextRenderer getTextRenderer();
	@Accessor long getLastSwitchFocusTime();
	@Accessor boolean isFocusUnlocked();

	@Invoker int invokeGetMaxLength();
}
