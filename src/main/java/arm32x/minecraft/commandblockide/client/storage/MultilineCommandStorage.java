package arm32x.minecraft.commandblockide.client.storage;

import arm32x.minecraft.commandblockide.client.processor.CommandProcessor;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class MultilineCommandStorage {
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
}
