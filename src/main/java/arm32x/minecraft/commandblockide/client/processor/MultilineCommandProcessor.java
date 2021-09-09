package arm32x.minecraft.commandblockide.client.processor;

import java.util.NavigableMap;
import java.util.TreeMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * Merges a multiline command into a single line.
 *
 * <p>All newlines in the input and the spaces that follow them are converted
 * into a single space in the output. Input is required to be indented with
 * spaces and have Unix line endings.</p>
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * execute
 *     as @a
 *     run
 *         say two spaces:  not merged
 * }</pre>
 * will be converted to:
 * <pre>{@code
 * execute as @a run say two spaces:  not merged
 * }</pre></p>
 */
@Environment(EnvType.CLIENT)
public final class MultilineCommandProcessor implements CommandProcessor {
	@Override
	public Pair<String, StringMapping> processCommand(String command) {
		StringBuilder builder = new StringBuilder();
		NavigableMap<Integer, Integer> map = new TreeMap<>();

		for (int index = 0; index < command.length(); index++) {
			char ch = command.charAt(index);
			if (ch == '\n') {
				builder.append(' ');
				try {
					while (command.charAt(index + 1) == ' ') {
						index++;
					}
				} catch (StringIndexOutOfBoundsException ignored) { }
				map.put(builder.length(), index + 1);
			} else {
				builder.append(ch);
			}
		}

		return new Pair<>(builder.toString(), new StringMapping(map));
	}

	private static @Nullable MultilineCommandProcessor instance = null;

	private MultilineCommandProcessor() { }

	public static MultilineCommandProcessor getInstance() {
		if (instance == null) {
			instance = new MultilineCommandProcessor();
		}
		return instance;
	}
}
