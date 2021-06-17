package arm32x.minecraft.commandblockide.client;

import arm32x.minecraft.commandblockide.Packets;
import arm32x.minecraft.commandblockide.client.gui.CommandFunctionIDEScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public final class CommandBlockIDEClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(Packets.EDIT_FUNCTION, (client, handler, buf, responseSender) -> {
			Identifier id = buf.readIdentifier();
			int lineCount = buf.readVarInt();
			client.execute(() -> {
				client.openScreen(new CommandFunctionIDEScreen(id, lineCount));
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

	private static final Logger LOGGER = LogManager.getLogger();
}
