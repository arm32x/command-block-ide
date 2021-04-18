package arm32x.minecraft.commandblockide.mixin;

import arm32x.minecraft.commandblockide.gui.CommandBlockIDEScreen;
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
	 */
	@Overwrite
	public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
		if (!(client.currentScreen instanceof CommandBlockIDEScreen)) {
			client.openScreen(new CommandBlockIDEScreen(commandBlock));
		}
	}
}
