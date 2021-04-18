package arm32x.minecraft.commandblockide.mixin;

import arm32x.minecraft.commandblockide.gui.CommandBlockIDEScreen;
import arm32x.minecraft.commandblockide.update.DataCommandUpdateRequester;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	@Inject(method = "onGameMessage(Lnet/minecraft/network/packet/s2c/play/GameMessageS2CPacket;)V", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
	public void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
		if (packet.getSenderUuid().equals(Util.NIL_UUID) && packet.getLocation() == MessageType.SYSTEM && packet.getMessage() instanceof TranslatableText) {
			TranslatableText message = (TranslatableText)packet.getMessage();
			if (message.getKey().equals("commands.data.block.query")) {
				if (DataCommandUpdateRequester.getInstance().handleFeedback(client, message)) {
					ci.cancel();
				}
			}
		}
	}

	@Inject(method = "onBlockEntityUpdate(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci, BlockPos blockPos, BlockEntity blockEntity, int i, boolean bl) {
		if (bl && client.currentScreen instanceof CommandBlockIDEScreen) {
			((CommandBlockIDEScreen)client.currentScreen).updateCommandBlock(blockPos);
		}
	}

	private static final Logger LOGGER = LogManager.getLogger("ClientPlayNetworkHandlerMixin");
}
