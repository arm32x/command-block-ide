package arm32x.minecraft.commandblockide.mixin.client;

import arm32x.minecraft.commandblockide.client.gui.screen.CommandBlockIDEScreen;
import arm32x.minecraft.commandblockide.client.update.DataCommandUpdateRequester;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public final class ClientPlayNetworkHandlerMixin {
	@Shadow private @Final MinecraftClient client;

	@Inject(
		method = "onGameMessage(Lnet/minecraft/network/packet/s2c/play/GameMessageS2CPacket;)V",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;onGameMessage(Lnet/minecraft/network/message/MessageType;Lnet/minecraft/text/Text;)V"
		),
		locals = LocalCapture.CAPTURE_FAILHARD
	)
	public void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci, Registry<MessageType> registry, MessageType messageType) {
		boolean isSystemMessage = registry.getKey(messageType).orElseThrow().equals(MessageType.SYSTEM);
		if (isSystemMessage && packet.content().getContent() instanceof TranslatableTextContent message) {
			if (message.getKey().equals("commands.data.block.query")) {
				if (DataCommandUpdateRequester.getInstance().handleFeedback(client, message)) {
					ci.cancel();
				}
			}
		}
	}

	// Injecting into a lambda, who knows how stable this is...
	@Inject(method = "method_38542(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;Lnet/minecraft/block/entity/BlockEntity;)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, BlockEntity blockEntity, CallbackInfo ci) {
		if (blockEntity instanceof CommandBlockBlockEntity && client.currentScreen instanceof CommandBlockIDEScreen commandBlockIDEScreen) {
			commandBlockIDEScreen.update(blockEntity.getPos());
		}
	}

	private static final Logger LOGGER = LogManager.getLogger("ClientPlayNetworkHandlerMixin");
}
