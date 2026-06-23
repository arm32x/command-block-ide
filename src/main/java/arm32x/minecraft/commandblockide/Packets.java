package arm32x.minecraft.commandblockide;

import arm32x.minecraft.commandblockide.payloads.ApplyFunctionPayload;
import arm32x.minecraft.commandblockide.payloads.EditFunctionPayload;
import arm32x.minecraft.commandblockide.payloads.UpdateFunctionCommandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public final class Packets {
	// Namespace
	private static final String NAMESPACE = "commandblockide";

	// Client to Server
	public static final Type<ApplyFunctionPayload> APPLY_FUNCTION = new Type<>(Identifier.parse(NAMESPACE + ":apply_function"));

	// Server to Client
	public static final Type<EditFunctionPayload> EDIT_FUNCTION = new Type<>(Identifier.parse(NAMESPACE + ":edit_function"));
	public static final Type<UpdateFunctionCommandPayload> UPDATE_FUNCTION_COMMAND = new Type<>(Identifier.parse(NAMESPACE + ":update_function_command"));
}
