package arm32x.minecraft.commandblockide;

import arm32x.minecraft.commandblockide.mixin.server.AbstractFileResourcePackInvoker;
import arm32x.minecraft.commandblockide.mixin.server.DirectoryResourcePackInvoker;
import arm32x.minecraft.commandblockide.mixinextensions.server.CommandFunctionExtension;
import arm32x.minecraft.commandblockide.server.command.EditFunctionCommand;
import arm32x.minecraft.commandblockide.util.PacketMerger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
					Text feedbackMessage = saveFunction(server, functionId, Arrays.asList(lines));
					player.sendMessage(feedbackMessage);
				});
			}
		});
	}

	private static Text saveFunction(MinecraftServer server, Identifier functionId, List<String> lines) {
		// TODO: Make saving functions use a CompletableFuture so errors can be
		//       properly shown to the user.
		Identifier functionResourceId = new Identifier(functionId.getNamespace(), String.format("functions/%s.mcfunction", functionId.getPath()));

		ResourceManager resourceManager = server.getResourceManager();
		if (!(resourceManager instanceof ReloadableResourceManagerImpl resourceManagerImpl)) {
			return Text.translatable("commandBlockIDE.saveFunction.failed.resourceManager", functionId).formatted(Formatting.RED);
		}

		Optional<ResourcePack> maybePack = resourceManager.streamResourcePacks().filter(p -> p.contains(ResourceType.SERVER_DATA, functionResourceId)).findFirst();
		if (maybePack.isEmpty()) {
			return Text.translatable("commandBlockIDE.saveFunction.failed.noResourcePack", functionId).formatted(Formatting.RED);
		}

		ResourcePack pack = maybePack.get();

		if (pack instanceof DirectoryResourcePack directoryPack) {
			File file = ((DirectoryResourcePackInvoker)directoryPack).invokeGetFile(AbstractFileResourcePackInvoker.invokeGetFilename(ResourceType.SERVER_DATA, functionResourceId));
			try {
				Files.write(file.toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("IO exception occurred while saving function '" + functionId.toString() + "':", e);
				return Text.translatable("commandBlockIDE.saveFunction.failed.ioException", functionId).formatted(Formatting.RED);
			}
			updateFunctionLines(server, functionId, lines);
			return Text.translatable("commandBlockIDE.saveFunction.success.file", functionId);
		} else if (pack instanceof ZipResourcePack) {
			return Text.translatable("commandBlockIDE.saveFunction.failed.zipNotSupported", functionId).formatted(Formatting.RED);
		} else {
			return Text.translatable("commandBlockIDE.saveFunction.failed.packClassNotSupported", functionId, pack.getClass().getSimpleName()).formatted(Formatting.RED);
		}
	}

	private static void updateFunctionLines(MinecraftServer server, Identifier functionId, List<String> lines) {
		Optional<CommandFunction> maybeFunction = server.getCommandFunctionManager().getFunction(functionId);
		maybeFunction.ifPresent(function -> ((CommandFunctionExtension)function).ide$setOriginalLines(lines));
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
