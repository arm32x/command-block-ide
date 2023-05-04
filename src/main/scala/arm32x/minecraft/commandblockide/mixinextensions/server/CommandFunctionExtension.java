package arm32x.minecraft.commandblockide.mixinextensions.server;

import java.util.List;

public interface CommandFunctionExtension {
	List<String> ide$getOriginalLines();
	void ide$setOriginalLines(List<String> lines);
}
