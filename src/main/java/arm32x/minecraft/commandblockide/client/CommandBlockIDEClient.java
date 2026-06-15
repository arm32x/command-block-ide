package arm32x.minecraft.commandblockide.client;

import arm32x.minecraft.commandblockide.Packets;
import arm32x.minecraft.commandblockide.client.gui.screen.CommandFunctionIDEScreen;
import arm32x.minecraft.commandblockide.payloads.EditFunctionPayload;
import arm32x.minecraft.commandblockide.payloads.UpdateFunctionCommandPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class CommandBlockIDEClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PayloadTypeRegistry.clientboundPlay().register(Packets.EDIT_FUNCTION, EditFunctionPayload.CODEC);
		ClientPlayNetworking.registerGlobalReceiver(Packets.EDIT_FUNCTION, (payload, context) -> {
			Minecraft client = context.client();
			client.execute(() -> client.setScreen(new CommandFunctionIDEScreen(payload.id(), payload.lineCount())));
		});
		PayloadTypeRegistry.clientboundPlay().register(Packets.UPDATE_FUNCTION_COMMAND, UpdateFunctionCommandPayload.CODEC);
		ClientPlayNetworking.registerGlobalReceiver(Packets.UPDATE_FUNCTION_COMMAND, (payload, context) -> {
			Minecraft client = context.client();
			client.execute(() -> {
				if (client.screen instanceof CommandFunctionIDEScreen ide) {
					ide.update(payload.index(), payload.line());
				}
			});
		});
	}

	public static void showErrorScreen(Exception ex, @Nullable String currentAction) {
		if (currentAction != null) {
			LOGGER.error("Error screen shown while " + currentAction + ":", ex);
		} else {
			LOGGER.error("Error screen shown:", ex);
		}
		Minecraft.getInstance().setScreen(new ErrorScreen(
			Component.translatable(currentAction != null ? "commandBlockIDE.errorWithContext" : "commandBlockIDE.error", currentAction),
			Component.literal(ex.toString())
		));
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
