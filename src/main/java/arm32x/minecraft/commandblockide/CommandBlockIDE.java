package arm32x.minecraft.commandblockide;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public final class CommandBlockIDE implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LOGGER.info("Initialized mod.");
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
