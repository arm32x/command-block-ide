package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.gui.CommandBlockIDEScreen;
import arm32x.minecraft.commandblockide.client.gui.CommandIDEScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
	@Shadow protected @Final MinecraftClient client;

	@Inject(method = "openCommandBlockScreen(Lnet/minecraft/block/entity/CommandBlockBlockEntity;)V", at = @At("HEAD"), cancellable = true)
	public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock, CallbackInfo ci) {
		if (!Screen.hasAltDown()) {
			if (!(client.currentScreen instanceof CommandIDEScreen)) {
				client.setScreen(new CommandBlockIDEScreen(commandBlock));
			}
			ci.cancel();
		}
	}
}
