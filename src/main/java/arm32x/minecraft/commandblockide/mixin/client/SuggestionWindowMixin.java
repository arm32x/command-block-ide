package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.gui.MultilineTextFieldWidget;
import arm32x.minecraft.commandblockide.client.processor.StringMapping;
import arm32x.minecraft.commandblockide.mixinextensions.client.ChatInputSuggestorExtension;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandSuggestions.SuggestionsList.class)
public abstract class SuggestionWindowMixin {
    @Shadow(aliases = { "field_21615" }) private @Final CommandSuggestions this$0;

    @Redirect(
        method = "useSuggestion()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/suggestion/Suggestion;apply(Ljava/lang/String;)Ljava/lang/String;",
            remap = false
        )
    )
    private String applySuggestion(Suggestion instance, String input) {
        StringMapping mapping = ((ChatInputSuggestorExtension)this$0).ide$getMapping();
        int start = StringMapping.mapIndexOrAfter(mapping, false, instance.getRange().getStart());
        int end = StringMapping.mapIndexOrAfter(mapping, false, instance.getRange().getEnd());

        if (start == 0 && end == input.length()) {
            return instance.getText();
        }
        StringBuilder result = new StringBuilder();
        if (start > 0) {
            result.append(input, 0, start);
        }
        result.append(instance.getText());
        if (end < input.length()) {
            result.append(input, end, input.length());
        }
        return result.toString();
    }

    @Redirect(
        method = "useSuggestion()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/context/StringRange;getStart()I",
            remap = false
        )
    )
    private int getMappedStart(StringRange instance) {
        StringMapping mapping = ((ChatInputSuggestorExtension)this$0).ide$getMapping();
        return StringMapping.mapIndexOrAfter(mapping, false, instance.getStart());
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/EditBox;isBordered()Z"
        )
    )
    public boolean getTextFieldDrawsBackground(EditBox textField) {
        // This will result in the suggestor moving left 1 pixel.
        return !(textField instanceof MultilineTextFieldWidget) && textField.isBordered();
    }
}
