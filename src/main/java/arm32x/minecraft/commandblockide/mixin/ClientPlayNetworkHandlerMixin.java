package arm32x.minecraft.commandblockide.mixin;

import arm32x.minecraft.commandblockide.gui.CommandBlockIDEScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public final class ClientPlayNetworkHandlerMixin {
	@Shadow private MinecraftClient client;

	@Inject(method = "onBlockEntityUpdate(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci, BlockPos blockPos, BlockEntity blockEntity, int i, boolean bl) {
		if (bl && client.currentScreen instanceof CommandBlockIDEScreen) {
			((CommandBlockIDEScreen)client.currentScreen).updateCommandBlock(blockPos);
		}
	}
}
