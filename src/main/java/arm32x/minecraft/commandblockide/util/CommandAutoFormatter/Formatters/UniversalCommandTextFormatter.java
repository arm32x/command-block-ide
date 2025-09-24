package arm32x.minecraft.commandblockide.util.CommandAutoFormatter.Formatters;

import arm32x.minecraft.commandblockide.util.CommandAutoFormatter.CommandFormatPreferences;

public class UniversalCommandTextFormatter extends ParsedCommandFormatter {
	public UniversalCommandTextFormatter(String txt, CommandFormatPreferences preferences) {
		super(txt, preferences);
	}

	@Override
	public String formatParsed() {
		return formatNodes(getAllNodes());
	}
}