package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.gui.MultilineTextFieldWidget;
import arm32x.minecraft.commandblockide.client.processor.CommandProcessor;
import arm32x.minecraft.commandblockide.client.processor.StringMapping;
import arm32x.minecraft.commandblockide.mixinextensions.client.ChatInputSuggestorExtension;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
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
@Mixin(ChatInputSuggestor.class)
public final class ChatInputSuggestorMixin implements ChatInputSuggestorExtension {
	@Unique private static final int ide$SUGGESTOR_Y_OFFSET = 9;

	@Unique public boolean ide$allowComments = false;
	@Unique public boolean ide$slashForbidden = false;

	@Unique public @Nullable CommandProcessor ide$commandProcessor = null;
	@Unique private @Nullable StringMapping ide$mapping = null;

	@Shadow @Final TextFieldWidget textField;

	@Shadow private @Nullable ParseResults<CommandSource> parse;
	@Shadow private @Nullable CompletableFuture<Suggestions> pendingSuggestions;

	@Shadow private @Nullable ChatInputSuggestor.SuggestionWindow window;

	@ModifyConstant(
		method = { "show(Z)V", "renderMessages(Lnet/minecraft/client/gui/DrawContext;)V" },
		constant = @Constant(intValue = 72)
	)
	public int getY(int seventyTwo) {
		if (textField instanceof MultilineTextFieldWidget multiline) {
			if (pendingSuggestions != null) {
				@Nullable Suggestions suggestions = pendingSuggestions.getNow(null);
				if (suggestions != null && !suggestions.isEmpty()) {
					int charIndex = StringMapping.mapIndexOrAfter(ide$mapping, false, suggestions.getRange().getStart());
					return multiline.getCharacterRealY(charIndex) + ide$SUGGESTOR_Y_OFFSET;
				}
			}
			return multiline.getCharacterRealY(multiline.getText().length()) + ide$SUGGESTOR_Y_OFFSET;
		} else {
			return textField.getY() + textField.getHeight() + 2;
		}
	}

	@ModifyArg(
		method = {
			"show(Z)V",
			"showUsages(Lnet/minecraft/util/Formatting;)Z"
		},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;getCharacterX(I)I",
			ordinal = 0
		),
		index = 0
	)
	public int mapSuggestionIndex(int index) {
		return StringMapping.mapIndexOrAfter(ide$mapping, false, index);
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

	@Unique @Override
	public @Nullable StringMapping ide$getMapping() {
		return ide$mapping;
	}

	@Inject(method = "showCommandSuggestions()V", at = @At("HEAD"), cancellable = true)
	public void onShowCommandSuggestions(CallbackInfo ci) {
		if (ide$allowComments && textField.getText().startsWith("#")
			|| ide$mapping != null && ide$mapping.inverted().mapIndex(textField.getCursor()).isEmpty()) {
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

	// See above.
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

	// See above.
	@SuppressWarnings("InvalidInjectorMethodSignature")
	@ModifyVariable(method = "refresh()V", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
	public int onGetTextFieldCursor1(int cursor) {
		return StringMapping.mapIndexOrAfter(ide$mapping, true, cursor);
	}

	@ModifyArg(
		method = "showUsages(Lnet/minecraft/util/Formatting;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/brigadier/context/CommandContextBuilder;findSuggestionContext(I)Lcom/mojang/brigadier/context/SuggestionContext;",
			remap = false,
			ordinal = 0
		),
		index = 0
	)
	public int onGetTextFieldCursor2(int cursor) {
		return StringMapping.mapIndexOrAfter(ide$mapping, true, cursor);
	}
}
