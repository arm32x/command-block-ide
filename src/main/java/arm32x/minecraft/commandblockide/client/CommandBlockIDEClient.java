package arm32x.minecraft.commandblockide.client;

import arm32x.minecraft.commandblockide.Packets;
import arm32x.minecraft.commandblockide.client.gui.CommandFunctionIDEScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class CommandBlockIDEClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(Packets.EDIT_FUNCTION, (client, handler, buf, responseSender) -> {
			Identifier id = buf.readIdentifier();
			int lineCount = buf.readVarInt();
			client.execute(() -> {
				client.setScreen(new CommandFunctionIDEScreen(id, lineCount));
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(Packets.UPDATE_FUNCTION_COMMAND, (client, handler, buf, responseSender) -> {
			int index = buf.readVarInt();
			String line = buf.readString();
			client.execute(() -> {
				if (client.currentScreen instanceof CommandFunctionIDEScreen) {
					((CommandFunctionIDEScreen)client.currentScreen).update(index, line);
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
		MinecraftClient.getInstance().setScreen(new FatalErrorScreen(
			new TranslatableText(currentAction != null ? "commandBlockIDE.errorWithContext" : "commandBlockIDE.error", currentAction),
			new LiteralText(ex.toString())
		));
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
