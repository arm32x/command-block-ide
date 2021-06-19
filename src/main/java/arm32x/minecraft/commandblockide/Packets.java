package arm32x.minecraft.commandblockide;

import net.minecraft.util.Identifier;

public final class Packets {
	// Client to Server
	public static final Identifier APPLY_FUNCTION = new Identifier("commandblockide", "apply_function");

	// Server to Client
	public static final Identifier EDIT_FUNCTION = new Identifier("commandblockide", "edit_function");
	public static final Identifier UPDATE_FUNCTION_COMMAND = new Identifier("commandblockide", "update_function_command");
}
