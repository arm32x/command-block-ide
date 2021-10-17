package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.gui.MultilineTextFieldWidget;
import arm32x.minecraft.commandblockide.client.processor.CommandProcessor;
import arm32x.minecraft.commandblockide.client.processor.StringMapping;
import arm32x.minecraft.commandblockide.mixinextensions.client.CommandSuggestorExtension;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(CommandSuggestor.class)
public final class CommandSuggestorMixin implements CommandSuggestorExtension {
	@Unique public boolean ide$allowComments = false;
	@Unique public boolean ide$slashForbidden = false;

	@Unique public @Nullable CommandProcessor ide$commandProcessor = null;
	@Unique private @Nullable StringMapping ide$mapping = null;

	@Shadow @Final TextFieldWidget textField;

	@Shadow private @Nullable CompletableFuture<Suggestions> pendingSuggestions;

	@ModifyConstant(method = { "showSuggestions(Z)V", "render(Lnet/minecraft/client/util/math/MatrixStack;II)V" }, constant = @Constant(intValue = 72))
	public int getY(int seventyTwo) {
		if (textField instanceof MultilineTextFieldWidget multiline
			&& pendingSuggestions != null) {
			@Nullable Suggestions suggestions = pendingSuggestions.getNow(null);
			if (suggestions != null) {
				return multiline.getCharacterY(suggestions.getRange().getStart());
			}
		}
		return textField.y + textField.getHeight() + 2;
	}

	@Unique @Override
	public void ide$setAllowComments(boolean allowComments) {
		ide$allowComments = allowComments;
	}

	@Unique @Override
	public void ide$setSlashForbidden(boolean slashForbidden) {
		ide$slashForbidden = slashForbidden;
	}

	@Unique @Override
	public CommandProcessor ide$getCommandProcessor() {
		return ide$commandProcessor;
	}

	@Unique @Override
	public void ide$setCommandProcessor(CommandProcessor processor) {
		ide$commandProcessor = processor;
	}

	@Inject(method = "show()V", at = @At("HEAD"), cancellable = true)
	public void onShow(CallbackInfo ci) {
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

	@ModifyVariable(method = "refresh()V", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
	private boolean onCheckForSlash(boolean bl) {
		return !ide$slashForbidden && bl;
	}

	@ModifyVariable(method = "refresh()V", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
	public String onGetCommand(String command) {
		if (ide$commandProcessor != null) {
			var processed = ide$commandProcessor.processCommand(command);
			ide$mapping = processed.getRight();
			return processed.getLeft();
		} else {
			return command;
		}
	}

	@ModifyVariable(method = "refresh()V", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
	public int onGetTextFieldCursor(int cursor) {
		if (ide$mapping != null) {
			OptionalInt index;
			do {
				index = ide$mapping.inverted().mapIndex(cursor++);
			} while (index.isEmpty());
			return index.getAsInt();
		} else {
			return cursor;
		}
	}
}
