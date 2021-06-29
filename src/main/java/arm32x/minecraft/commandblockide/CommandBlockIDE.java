package arm32x.minecraft.commandblockide;

import arm32x.minecraft.commandblockide.mixin.server.*;
import arm32x.minecraft.commandblockide.server.command.EditFunctionCommand;
import arm32x.minecraft.commandblockide.util.PacketMerger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class CommandBlockIDE implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
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

				saveFunction(server, functionId, lines);
			}
		});
	}

	private static void saveFunction(MinecraftServer server, Identifier functionId, String[] lines) {
		Identifier functionResourceId = new Identifier(functionId.getNamespace(), String.format("functions/%s.mcfunction", functionId.getPath()));

		// Mixin accessor bullshit.
		ServerResourceManager serverResourceManager = ((MinecraftServerAccessor)server).getServerResourceManager();
		ResourceManager resourceManager = serverResourceManager.getResourceManager();
		if (!(resourceManager instanceof ReloadableResourceManagerImpl)) {
			// TODO: Handle errors using Exceptions or something.
			LOGGER.error("Error saving function '{}': Resource manager was not the correct type.", functionId);
			return;
		}
		ReloadableResourceManagerImpl resourceManagerImpl = (ReloadableResourceManagerImpl)resourceManager;
		Map<String, NamespaceResourceManager> namespaceResourceManagers = ((ReloadableResourceManagerImplAccessor)resourceManagerImpl).getNamespaceManagers();
		@Nullable NamespaceResourceManager namespaceResourceManager = namespaceResourceManagers.get(functionId.getNamespace());
		if (namespaceResourceManager == null) {
			LOGGER.error("Error saving function '{}': Resource manager has no namespace '{}'.", functionId, functionId.getNamespace());
			return;
		}
		List<ResourcePack> packList = ((NamespaceResourceManagerAccessor)namespaceResourceManager).getPackList();

		Optional<ResourcePack> maybePack = packList.stream()
			.filter(p -> p.contains(ResourceType.SERVER_DATA, functionResourceId))
			.findFirst();

		if (!maybePack.isPresent()) {
			LOGGER.error("Error saving function '{}': Not found in any resource pack.", functionId);
			return;
		}

		ResourcePack pack = maybePack.get();

		if (pack instanceof DirectoryResourcePack) {
			DirectoryResourcePack directoryPack = (DirectoryResourcePack)pack;
			File file = ((DirectoryResourcePackInvoker)directoryPack).invokeGetFile(AbstractFileResourcePackInvoker.invokeGetFilename(ResourceType.SERVER_DATA, functionResourceId));
			try {
				Files.write(file.toPath(), Arrays.asList(lines), StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("Error saving function '" + functionId.toString() + "':", e);
				return;
			}
			LOGGER.info("Saved function '{}' to file '{}'.", functionId, file.getPath());
		} else {
			LOGGER.error("Error saving function '{}': Resource pack type '{}' not supported.", functionId, pack.getClass().getSimpleName());
		}
	}

	private static final Logger LOGGER = LogManager.getLogger();
}

