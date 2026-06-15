package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.gui.screen.CommandBlockIDEScreen;
import arm32x.minecraft.commandblockide.client.gui.screen.CommandIDEScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Shadow protected @Final Minecraft minecraft;

	@Inject(method = "openCommandBlock(Lnet/minecraft/world/level/block/entity/CommandBlockEntity;)V", at = @At("HEAD"), cancellable = true)
	public void openCommandBlockScreen(CommandBlockEntity commandBlock, CallbackInfo ci) {
        if (!minecraft.hasAltDown()) {
            if (!(minecraft.screen instanceof CommandIDEScreen)) {
                minecraft.setScreen(new CommandBlockIDEScreen(commandBlock));
            }
            ci.cancel();
        }
	}
}
