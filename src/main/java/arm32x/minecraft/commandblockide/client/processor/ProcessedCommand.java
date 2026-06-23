package arm32x.minecraft.commandblockide.client.processor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record ProcessedCommand(String command, StringMapping mapping) { }
