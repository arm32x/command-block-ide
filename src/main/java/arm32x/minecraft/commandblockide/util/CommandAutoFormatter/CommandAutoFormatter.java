package arm32x.minecraft.commandblockide.util.CommandAutoFormatter;

import arm32x.minecraft.commandblockide.util.CommandAutoFormatter.Formatters.ExecuteFormatter;
import arm32x.minecraft.commandblockide.util.CommandAutoFormatter.Formatters.UniversalCommandTextFormatter;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;

public class CommandAutoFormatter {
	public static String format(String cmd) {
		return format(cmd, CommandFormatPreferences.getPreferences());
	}

	public static String format(String srcCommand, CommandFormatPreferences preferences) {
		String cmd = srcCommand;
		if(!cmd.isEmpty())
			if(cmd.charAt(0)=='/')
				cmd=cmd.substring(1);
		var parse = CommandParser.parse(cmd);
		if(parse!=null) {
			var nodes = parse.results.getContext().getNodes();
			if (nodes.isEmpty())
				return null;
			if (nodes.getFirst().getNode() instanceof LiteralCommandNode<CommandSource> literalNode) {
				switch (literalNode.getLiteral()) {
					case "execute":
						return new ExecuteFormatter(cmd, preferences).format();
				}
			}
		}
		return new UniversalCommandTextFormatter(cmd,preferences).format();
	}
}
