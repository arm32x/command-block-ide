package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.mixinextensions.client.ChatInputSuggestorExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ChatInputSuggestor.class)
public final class ChatInputSuggestorMixin implements ChatInputSuggestorExtension {
	@Unique public int ide$y = 72;
	@Unique public boolean ide$allowComments = false;
	@Unique public boolean ide$slashForbidden = false;

	@Shadow @Final TextFieldWidget textField;

	@ModifyConstant(
		method = { "show(Z)V", "renderMessages(Lnet/minecraft/client/util/math/MatrixStack;)V" },
		constant = @Constant(intValue = 72)
	)
	public int getY(int seventyTwo) {
		return ide$y;
	}

	@Unique @Override
	public void ide$setY(int y) {
		ide$y = y;
	}

	@Unique @Override
	public void ide$setAllowComments(boolean allowComments) {
		ide$allowComments = allowComments;
	}

	@Unique @Override
	public void ide$setSlashForbidden(boolean slashForbidden) {
		ide$slashForbidden = slashForbidden;
	}

	@Inject(method = "showCommandSuggestions()V", at = @At("HEAD"), cancellable = true)
	public void onShowCommandSuggestions(CallbackInfo ci) {
		if (ide$allowComments && textField.getText().startsWith("#")) {
			ci.cancel();
		}
	}

	@Inject(method = "provideRenderText(Ljava/lang/String;I)Lnet/minecraft/text/OrderedText;", at = @At("HEAD"), cancellable = true)
	public void onProvideRenderText(String original, int firstCharacterIndex, CallbackInfoReturnable<OrderedText> cir) {
		if (ide$allowComments && textField.getText().startsWith("#")) {
			cir.setReturnValue(OrderedText.styledForwardsVisitedString(original, Style.EMPTY.withColor(Formatting.DARK_GRAY)));
		}
	}

	// The IntelliJ Minecraft Development plugin seems to think the method
	// signature is wrong when in reality it works just fine.
	@SuppressWarnings("InvalidInjectorMethodSignature")
	@ModifyVariable(method = "refresh()V", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
	private boolean onCheckForSlash(boolean bl) {
		return !ide$slashForbidden && bl;
	}
}
