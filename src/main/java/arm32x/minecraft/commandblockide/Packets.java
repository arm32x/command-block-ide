package arm32x.minecraft.commandblockide;

import arm32x.minecraft.commandblockide.payloads.ApplyFunctionPayload;
import arm32x.minecraft.commandblockide.payloads.EditFunctionPayload;
import arm32x.minecraft.commandblockide.payloads.UpdateFunctionCommandPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public final class Packets {
	// Namespace
	private static final String NAMESPACE = "commandblockide";

	// Client to Server
	public static final Id<ApplyFunctionPayload> APPLY_FUNCTION = new Id<>(Identifier.of(NAMESPACE + ":apply_function"));

	// Server to Client
	public static final Id<EditFunctionPayload> EDIT_FUNCTION = new Id<>(Identifier.of(NAMESPACE + ":edit_function"));
	public static final Id<UpdateFunctionCommandPayload> UPDATE_FUNCTION_COMMAND = new Id<>(Identifier.of(NAMESPACE + ":update_function_command"));
}
