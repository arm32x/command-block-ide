package arm32x.minecraft.commandblockide.client.storage;

import arm32x.minecraft.commandblockide.client.CommandBlockIDEClient;
import arm32x.minecraft.commandblockide.client.processor.CommandProcessor;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.msgpack.core.*;

@Environment(EnvType.CLIENT)
public final class MultilineCommandStorage implements Serializable {
	private static @Nullable MultilineCommandStorage instance = null;

	public static MultilineCommandStorage getInstance() {
		if (instance == null) {
			instance = new MultilineCommandStorage();
		}
		return instance;
	}

	private MultilineCommandStorage() { }

	private final Map<ByteBuffer, String> commands = new HashMap<>();
	private final Map<CommandBlockLocation, byte[]> blocks = new HashMap<>();
	private final Map<CommandFunctionLocation, byte[]> functions = new HashMap<>();

	public void add(String multiline, String singleLine, boolean isSingleplayer, String world, BlockPos pos) {
		if (!singleLine.isBlank()) {
			byte[] singleLineHash = hash(singleLine);
			commands.put(ByteBuffer.wrap(singleLineHash), multiline);
			blocks.put(new CommandBlockLocation(isSingleplayer, world, pos), singleLineHash);
		}
	}

	public void add(String multiline, String singleLine, boolean isSingleplayer, String world, Identifier function, int lineIndex) {
		if (!singleLine.isBlank()) {
			byte[] singleLineHash = hash(singleLine);
			commands.put(ByteBuffer.wrap(singleLineHash), multiline);
			functions.put(new CommandFunctionLocation(isSingleplayer, world, function, lineIndex), singleLineHash);
		}
	}

	public Optional<String> get(byte[] singleLineHash) {
		return Optional.ofNullable(commands.get(ByteBuffer.wrap(singleLineHash)));
	}

	public Optional<String> get(boolean isSingleplayer, String world, BlockPos pos) {
		var singleLineHash = Optional.ofNullable(blocks.get(new CommandBlockLocation(isSingleplayer, world, pos)));
		return singleLineHash.map(h -> commands.get(ByteBuffer.wrap(h)));
	}

	public Optional<String> get(boolean isSingleplayer, String world, Identifier function, int lineIndex) {
		var singleLineHash = Optional.ofNullable(functions.get(new CommandFunctionLocation(isSingleplayer, world, function, lineIndex)));
		return singleLineHash.map(h -> commands.get(ByteBuffer.wrap(h)));
	}

	public String getRobust(String singleLine, CommandProcessor processor, boolean isSingleplayer, String world, BlockPos pos) {
		return getRobust(singleLine, processor, () -> get(isSingleplayer, world, pos));
	}

	public String getRobust(String singleLine, CommandProcessor processor, boolean isSingleplayer, String world, Identifier function, int lineIndex) {
		return getRobust(singleLine, processor, () -> get(isSingleplayer, world, function, lineIndex));
	}

	private String getRobust(String singleLine, CommandProcessor processor, Supplier<Optional<String>> fallbackSource) {
		byte[] singleLineHash = hash(singleLine);
		var multiline = get(singleLineHash);
		if (multiline.isPresent()) {
			return multiline.get();
		} else {
			multiline = fallbackSource.get();
			if (multiline.isPresent() && processor.processCommand(multiline.get()).getLeft().equals(singleLine)) {
				return multiline.get();
			}
		}
		return singleLine;
	}


	public void remove(boolean isSingleplayer, String world, BlockPos pos) {
		var location = new CommandBlockLocation(isSingleplayer, world, pos);
		byte[] singleLineHash = blocks.get(location);
		blocks.remove(location);
		commands.remove(ByteBuffer.wrap(singleLineHash));
	}

	public void remove(boolean isSingleplayer, String world, Identifier function, int lineIndex) {
		var location = new CommandFunctionLocation(isSingleplayer, world, function, lineIndex);
		byte[] singleLineHash = functions.get(location);
		functions.remove(location);
		commands.remove(ByteBuffer.wrap(singleLineHash));
	}

	public static void save() {
		MultilineCommandStorage instance = getInstance();
		try (MessagePacker packer = MessagePack.newDefaultPacker(new FileOutputStream(getFile()))) {
			packer.packMapHeader(instance.commands.size());
			for (var entry : instance.commands.entrySet()) {
				byte[] hash = entry.getKey().array();
				packer.packBinaryHeader(hash.length);
				packer.writePayload(hash);

				String command = entry.getValue();
				packer.packString(command);
			}

			packer.packMapHeader(instance.blocks.size());
			for (var entry : instance.blocks.entrySet()) {
				var location = entry.getKey();
				packer.packArrayHeader(5);
				packer.packBoolean(location.isSingleplayer);
				packer.packString(location.world);
				packer.packInt(location.pos.getX());
				packer.packInt(location.pos.getY());
				packer.packInt(location.pos.getZ());

				byte[] hash = entry.getValue();
				packer.packBinaryHeader(hash.length);
				packer.writePayload(hash);
			}

			packer.packMapHeader(instance.functions.size());
			for (var entry : instance.functions.entrySet()) {
				var location = entry.getKey();
				packer.packArrayHeader(4);
				packer.packBoolean(location.isSingleplayer);
				packer.packString(location.world);
				packer.packString(location.function.toString());
				packer.packInt(location.lineIndex);
			}
		} catch (IOException ex) {
			CommandBlockIDEClient.showErrorScreen(ex, "saving multiline commands");
		}
	}

	public static void load() {
		if (!getFile().exists()) {
			return;
		}

		var instance = new MultilineCommandStorage();
		try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(getFile()))) {
			int commandsSize = unpacker.unpackMapHeader();
			for (int index = 0; index < commandsSize; index++) {
				byte[] hash = unpacker.readPayload(unpacker.unpackBinaryHeader());
				String command = unpacker.unpackString();
				instance.commands.put(ByteBuffer.wrap(hash), command);
			}

			int blocksSize = unpacker.unpackMapHeader();
			for (int index = 0; index < blocksSize; index++) {
				int length = unpacker.unpackArrayHeader();
				if (length != 5) {
					throw new Exception("Expected array of length 5, got length " + length);
				}
				boolean isSingleplayer = unpacker.unpackBoolean();
				String world = unpacker.unpackString();
				int x = unpacker.unpackInt();
				int y = unpacker.unpackInt();
				int z = unpacker.unpackInt();
				var location = new CommandBlockLocation(isSingleplayer, world, new BlockPos(x, y, z));

				byte[] hash = unpacker.readPayload(unpacker.unpackBinaryHeader());
				instance.blocks.put(location, hash);
			}

			int functionsSize = unpacker.unpackMapHeader();
			for (int index = 0; index < functionsSize; index++) {
				int length = unpacker.unpackArrayHeader();
				if (length != 4) {
					throw new Exception("Expected array of length 4, got length " + length);
				}
				boolean isSingleplayer = unpacker.unpackBoolean();
				String world = unpacker.unpackString();
				var identifier = new Identifier(unpacker.unpackString());
				int lineIndex = unpacker.unpackInt();
				var location = new CommandFunctionLocation(isSingleplayer, world, identifier, lineIndex);

				byte[] hash = unpacker.readPayload(unpacker.unpackBinaryHeader());
				instance.functions.put(location, hash);
			}

			MultilineCommandStorage.instance = instance;
		} catch (Exception ex) {
			CommandBlockIDEClient.showErrorScreen(ex, "loading multiline commands");
		}
	}

	private static File getFile() {
		return new File(MinecraftClient.getInstance().runDirectory, "commandblockide.bin");
	}

	public static byte[] hash(String string) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return digest.digest(string.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static record CommandBlockLocation(boolean isSingleplayer, String world, BlockPos pos) { }
	private static record CommandFunctionLocation(boolean isSingleplayer, String world, Identifier function, int lineIndex) { }

	private static final Logger LOGGER = LogManager.getLogger();
}
