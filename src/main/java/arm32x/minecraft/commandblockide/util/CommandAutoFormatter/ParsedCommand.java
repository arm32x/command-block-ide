package arm32x.minecraft.commandblockide.util.CommandAutoFormatter;

import com.mojang.brigadier.ParseResults;
import net.minecraft.command.CommandSource;

public class ParsedCommand {
	public final String srcCommand;
	public final ParseResults<CommandSource> results;
	public final String leftovers;
	public long timeLastUsed;

	public ParsedCommand(String srcCommand, ParseResults<CommandSource> results) {
		this.srcCommand = srcCommand;
		this.results = results;
		leftovers = results.getReader().getRemaining();
	}
}
