package arm32x.minecraft.commandblockide.mixin.client;

import java.util.function.Predicate;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor {
	@Accessor int getSelectionStart();
	@Accessor int getSelectionEnd();
	@Accessor int getMaxLength();
	@Accessor String getText();
	@Accessor Predicate<String> getTextPredicate();

	@Invoker void invokeOnChanged(String newText);
}
