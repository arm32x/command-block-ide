package arm32x.minecraft.commandblockide.client;

import arm32x.minecraft.commandblockide.Packets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public final class CommandBlockIDEClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(Packets.EDIT_FUNCTION, ((client, handler, buf, responseSender) -> {
			LOGGER.error("Not yet implemented.");
		}));
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
