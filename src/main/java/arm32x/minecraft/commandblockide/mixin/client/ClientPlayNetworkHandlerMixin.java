package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.gui.screen.CommandBlockIDEScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {
	protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	// Injecting into a lambda, who knows how stable this is...
	@Inject(method = "method_38542(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;Lnet/minecraft/block/entity/BlockEntity;)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, BlockEntity blockEntity, CallbackInfo ci) {
		if (blockEntity instanceof CommandBlockBlockEntity && client.currentScreen instanceof CommandBlockIDEScreen commandBlockIDEScreen) {
			commandBlockIDEScreen.update(blockEntity.getPos());
		}
	}
}
