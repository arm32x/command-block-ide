package arm32x.minecraft.commandblockide.mixin.client;

import com.mojang.brigadier.ParseResults;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.command.CommandSource;
import net.minecraft.text.OrderedText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(ChatInputSuggestor.class)
public interface ChatInputSuggestorAccessor {
	@Invoker OrderedText invokeProvideRenderText(String original, int firstCharacterIndex);
}
