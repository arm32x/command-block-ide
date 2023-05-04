package arm32x.minecraft.commandblockide;

import arm32x.minecraft.commandblockide.mixin.server.DirectoryResourcePackAccessor;
import arm32x.minecraft.commandblockide.mixin.server.FunctionLoaderAccessor;
import arm32x.minecraft.commandblockide.mixinextensions.server.CommandFunctionExtension;
import arm32x.minecraft.commandblockide.server.command.EditFunctionCommand;
import arm32x.minecraft.commandblockide.util.PacketMerger;
import com.mojang.serialization.DataResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import net.minecraft.util.PathUtil;
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

		// Convert the function ID ('some_datapack:some_function') to a resource
		// path ('some_datapack:functions/some_function.mcfunction').
		var resourceFinder = FunctionLoaderAccessor.getResourceFinder();
		var functionResourcePath = resourceFinder.toResourcePath(functionId);

		// Figure out which resource pack the function is in.
		var resourceManager = server.getResourceManager();
		var functionResource = resourceManager.getResource(functionResourcePath);
		if (functionResource.isEmpty()) {
			// Error saving function '...': Not found in any datapack.
			return Text.translatable("commandBlockIDE.saveFunction.failed.noResourcePack", functionId);
		}
		var pack = functionResource.get().getPack();

		// Only directory-based resource packs are supported.
		if (pack instanceof ZipResourcePack) {
			return Text.translatable("commandBlockIDE.saveFunction.failed.zipNotSupported", functionId).formatted(Formatting.RED);
		} else if (!(pack instanceof DirectoryResourcePack)) {
			return Text.translatable("commandBlockIDE.saveFunction.failed.packClassNotSupported", functionId, pack.getClass().getSimpleName()).formatted(Formatting.RED);
		}
		var directoryPack = (DirectoryResourcePack)pack;

		// Get the path to the function resource in the filesystem.
		DataResult<Path> pathResult = getFilesystemPathOfResource(directoryPack, ResourceType.SERVER_DATA, functionResourcePath);
		if (pathResult.result().isEmpty()) {
			String errorMessage = pathResult.error().get().message();
			// Error saving function '...': Invalid path '...': ...
			return Text.translatable("commandBlockIDE.saveFunction.failed.invalidPath", functionId, functionResourcePath, errorMessage);
		}
		Path path = pathResult.result().get();

		// Replace the content of the mcfunction file.
		try {
			Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("IO exception occurred while saving function '" + functionId.toString() + "':", e);
			return Text.translatable("commandBlockIDE.saveFunction.failed.ioException", functionId).formatted(Formatting.RED);
		}

		updateFunctionLines(server, functionId, lines);
		return Text.translatable("commandBlockIDE.saveFunction.success.file", functionId);
	}

	/**
	 * Determines the filesystem path to a resource in a directory-based
	 * resource pack.
	 *
	 * <p>This code implements the same logic as DirectoryResourcePack.open,
	 * except without opening the file at the end. If an error occurs, it is
	 * returned as a {@link DataResult}.</p>
	 *
	 * <p>Since {@code DataResult} is a part of the open-source DataFixerUpper,
	 * it should be more stable between updates than other Minecraft code.</p>
	 *
	 * @param pack The resource pack containing the resource.
	 * @param resourceType Whether the resource is from a client-side resource
	 *                     pack or server-side datapack.
	 * @param resourcePath The path to the resource inside the resource pack.
	 * @return A filesystem path to the same resource as {@code resourcePath}.
	 */
	@SuppressWarnings("SameParameterValue")
	private static DataResult<Path> getFilesystemPathOfResource(DirectoryResourcePack pack, ResourceType resourceType, Identifier resourcePath) {
		Path root = ((DirectoryResourcePackAccessor)pack).getRoot();
		Path namespaceDir = root.resolve(resourceType.getDirectory()).resolve(resourcePath.getNamespace());

		return PathUtil.split(resourcePath.getPath())
			.map(segments -> PathUtil.getPath(namespaceDir, segments));
	}

	private static void updateFunctionLines(MinecraftServer server, Identifier functionId, List<String> lines) {
		Optional<CommandFunction> maybeFunction = server.getCommandFunctionManager().getFunction(functionId);
		maybeFunction.ifPresent(function -> ((CommandFunctionExtension)function).ide$setOriginalLines(lines));
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
