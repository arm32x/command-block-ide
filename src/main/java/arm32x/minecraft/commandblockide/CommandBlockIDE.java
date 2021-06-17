package arm32x.minecraft.commandblockide;

import arm32x.minecraft.commandblockide.server.command.EditFunctionCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public final class CommandBlockIDE implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			EditFunctionCommand.register(dispatcher);
		});
	}
}

