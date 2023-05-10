package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.update.DataCommandUpdateRequester;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MessageHandler.class)
public final class MessageHandlerMixin {
    @Shadow private @Final MinecraftClient client;

    @Inject(
        method = "onGameMessage(Lnet/minecraft/text/Text;Z)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        if (message.getContent() instanceof TranslatableTextContent content
            && content.getKey().equals("commands.data.block.query"))
        {
            boolean handled = DataCommandUpdateRequester.getInstance().handleFeedback(client, content);
            if (handled) {
                ci.cancel();
            }
        }
    }
}
