package arm32x.minecraft.commandblockide.client.processor;

import arm32x.minecraft.commandblockide.util.StringMapping;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Pair;

@Environment(EnvType.CLIENT)
public interface CommandProcessor {
	/**
	 * Process a command into a version that is compatible with Minecraft's
	 * command system.
	 *
	 * @return A {@link Pair} containing the processed command and a mapping
	 *         from the processed command back to the original.
	 */
	Pair<String, StringMapping> processCommand(String command);
}
