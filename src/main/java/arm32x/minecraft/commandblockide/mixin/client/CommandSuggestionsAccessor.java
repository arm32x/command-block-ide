package arm32x.minecraft.commandblockide.mixin.client;

import com.mojang.brigadier.ParseResults;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(CommandSuggestions.class)
public interface CommandSuggestionsAccessor {
	@Accessor ParseResults<ClientSuggestionProvider> getCurrentParse();
}
