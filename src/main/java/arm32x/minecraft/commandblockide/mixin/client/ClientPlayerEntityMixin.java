package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.gui.CommandBlockIDEScreen;
import arm32x.minecraft.commandblockide.client.gui.CommandIDEScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public final class ClientPlayerEntityMixin {
	@Shadow protected @Final MinecraftClient client;

	/**
	 * @author ARM32
	 * @reason This could have been done with a cancellable inject, but I would
	 *         have to rewrite the entire method anyway.
	 */
	@Overwrite
	public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
		if (!(client.currentScreen instanceof CommandIDEScreen)) {
			client.setScreen(new CommandBlockIDEScreen(commandBlock));
		}
	}
}
