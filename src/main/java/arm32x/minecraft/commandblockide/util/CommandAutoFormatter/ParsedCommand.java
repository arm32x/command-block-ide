package arm32x.minecraft.commandblockide.util.CommandAutoFormatter;

import com.mojang.brigadier.ParseResults;
import net.minecraft.client.network.ClientCommandSource;

public class ParsedCommand {
	public final String srcCommand;
	public final ParseResults<ClientCommandSource> results;
	public final String leftovers;
	public long timeLastUsed;

	public ParsedCommand(String srcCommand, ParseResults<ClientCommandSource> results) {
		this.srcCommand = srcCommand;
		this.results = results;
		leftovers = results.getReader().getRemaining();
	}
}
