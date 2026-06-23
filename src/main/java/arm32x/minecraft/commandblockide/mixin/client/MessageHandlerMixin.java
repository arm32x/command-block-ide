package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.update.DataCommandUpdateRequester;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ChatListener.class)
public final class MessageHandlerMixin {
    @Shadow private @Final Minecraft minecraft;

    @Inject(
        method = "handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onGameMessage(Component message, boolean overlay, CallbackInfo ci) {
        if (message.getContents() instanceof TranslatableContents content
            && content.getKey().equals("commands.data.block.query"))
        {
            boolean handled = DataCommandUpdateRequester.getInstance().handleFeedback(minecraft, content);
            if (handled) {
                ci.cancel();
            }
        }
    }
}
