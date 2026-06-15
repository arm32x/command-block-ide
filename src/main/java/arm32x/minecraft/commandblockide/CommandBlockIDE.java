package arm32x.minecraft.commandblockide;

import arm32x.minecraft.commandblockide.mixinextensions.server.CommandFunctionExtension;
import arm32x.minecraft.commandblockide.payloads.ApplyFunctionPayload;
import arm32x.minecraft.commandblockide.server.command.EditFunctionCommand;
import arm32x.minecraft.commandblockide.server.function.FunctionIO;
import arm32x.minecraft.commandblockide.util.PacketMerger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CommandBlockIDE implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				EditFunctionCommand.register(dispatcher));

		final PacketMerger functionMerger = new PacketMerger();
		PayloadTypeRegistry.serverboundPlay().register(Packets.APPLY_FUNCTION, ApplyFunctionPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(Packets.APPLY_FUNCTION, (payload, context) -> {
			if (!context.player().permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))) {
				return;
			}
			Optional<FriendlyByteBuf> maybeMerged = Optional.empty();
			try {
				maybeMerged = functionMerger.append(payload.toBuf());
			} catch (PacketMerger.InvalidSplitPacketException e) {
				LOGGER.error("PacketMerger", e);
			}
			if (maybeMerged.isPresent()) {
				FriendlyByteBuf merged = maybeMerged.get();
				Identifier functionId = merged.readIdentifier();
				int lineCount = merged.readVarInt();
				String[] lines = new String[lineCount];
				for (int index = 0; index < lineCount; index++) {
					lines[index] = merged.readUtf(Integer.MAX_VALUE >> 2);
				}

				ServerPlayer player = context.player();
				MinecraftServer server = context.server();
				server.execute(() -> {
					Component feedbackMessage = FunctionIO.saveFunction(server, functionId, Arrays.asList(lines));
					player.sendSystemMessage(feedbackMessage);
				});
			}
		});
	}

	private static void updateFunctionLines(MinecraftServer server, Identifier functionId, List<String> lines) {
		Optional<CommandFunction<CommandSourceStack>> maybeFunction = server.getFunctions().get(functionId);
		maybeFunction.ifPresent(function -> ((CommandFunctionExtension)function).ide$setOriginalLines(lines));
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
