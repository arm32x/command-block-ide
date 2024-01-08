package arm32x.minecraft.commandblockide;

import arm32x.minecraft.commandblockide.mixinextensions.server.CommandFunctionExtension;
import arm32x.minecraft.commandblockide.server.command.EditFunctionCommand;
import arm32x.minecraft.commandblockide.server.function.FunctionIO;
import arm32x.minecraft.commandblockide.util.PacketMerger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CommandBlockIDE implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			EditFunctionCommand.register(dispatcher);
		});

		final PacketMerger functionMerger = new PacketMerger();
		ServerPlayNetworking.registerGlobalReceiver(Packets.APPLY_FUNCTION, (server, player, handler, buf, responseSender) -> {
			Optional<PacketByteBuf> maybeMerged = Optional.empty();
			try {
				maybeMerged = functionMerger.append(buf);
			} catch (PacketMerger.InvalidSplitPacketException e) {
				e.printStackTrace();
			}
			if (maybeMerged.isPresent()) {
				PacketByteBuf merged = maybeMerged.get();
				Identifier functionId = merged.readIdentifier();
				int lineCount = merged.readVarInt();
				String[] lines = new String[lineCount];
				for (int index = 0; index < lineCount; index++) {
					lines[index] = merged.readString(Integer.MAX_VALUE >> 2);
				}

				server.execute(() -> {
					Text feedbackMessage = FunctionIO.saveFunction(server, functionId, Arrays.asList(lines));
					player.sendMessage(feedbackMessage);
				});
			}
		});
	}

	private static void updateFunctionLines(MinecraftServer server, Identifier functionId, List<String> lines) {
		Optional<CommandFunction<ServerCommandSource>> maybeFunction = server.getCommandFunctionManager().getFunction(functionId);
		maybeFunction.ifPresent(function -> ((CommandFunctionExtension)function).ide$setOriginalLines(lines));
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
