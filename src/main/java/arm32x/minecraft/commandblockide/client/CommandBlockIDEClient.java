package arm32x.minecraft.commandblockide.client;

import arm32x.minecraft.commandblockide.Packets;
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
			LOGGER.info("{}: {} lines", id, lineCount);
			LOGGER.error("Not yet implemented.");
		});
		ClientPlayNetworking.registerGlobalReceiver(Packets.UPDATE_FUNCTION_COMMAND, (client, handler, buf, responseSender) -> {
			int index = buf.readVarInt();
			String line = buf.readString();
			LOGGER.info("{}: {}", index, line);
			LOGGER.error("Not yet implemented.");
		});
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
