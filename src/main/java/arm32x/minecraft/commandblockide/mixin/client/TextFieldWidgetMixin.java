package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.gui.MultilineTextFieldWidget;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public final class TextFieldWidgetMixin {
	@SuppressWarnings("ConstantConditions")
	@Redirect(method = "write(Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;stripInvalidChars(Ljava/lang/String;)Ljava/lang/String;"))
	public String skipStrippingInvalidChars(String text) {
		if ((Object)this instanceof MultilineTextFieldWidget) {
			return text;
		} else {
			return SharedConstants.stripInvalidChars(text);
		}
	}
}
