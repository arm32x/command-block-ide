package arm32x.minecraft.commandblockide.client.processor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Tuple;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface CommandProcessor {
	/**
	 * Process a command into a version that is compatible with Minecraft's
	 * command system.
	 *
	 * @return A {@link Tuple} containing the processed command and a mapping
	 *         from the processed command back to the original.
	 */
	Tuple<String, StringMapping> processCommand(String command);
}
