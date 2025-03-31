package arm32x.minecraft.commandblockide.util.CommandAutoFormatter.Formatters;

import arm32x.minecraft.commandblockide.util.CommandAutoFormatter.CommandFormatPreferences;
import arm32x.minecraft.commandblockide.util.CommandAutoFormatter.LineBreakType;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;

import java.util.List;


public class ExecuteFormatter extends ParsedCommandFormatter {
	public ExecuteFormatter(String txt, CommandFormatPreferences preferences) {
		super(txt, preferences);
	}

	public String formatParsed() {
		StringBuilder sb = new StringBuilder();

		for (List<ParsedCommandNode<?>> clause : getAllNodeLists()) {
			ParsedCommandNode<?> firstNode = clause.getFirst();

			LineBreakType breakType = preferences.getBreakType(firstNode.getNode(), true);

			StringRange firstRange = firstNode.getRange();
			int linePriorLen = firstRange.getStart() - (lastWrap + 1);
			int thisLineLen = 1 + clause.getLast().getRange().getEnd() - firstRange.getStart();

			if (breakType == LineBreakType.SoftBreakLong) {
				if (linePriorLen > preferences.longWrapLen || (linePriorLen > 0 && thisLineLen > preferences.longWrapLen))
					breakType = LineBreakType.HardBreak;
			} else if (breakType == LineBreakType.SoftBreakShort) {
				if (linePriorLen > preferences.shortWrapLen ||
						(preferences.shortWrapPreemptively && linePriorLen > 0 && thisLineLen > preferences.shortWrapLen))
					breakType = LineBreakType.HardBreak;
			}

			if (breakType == LineBreakType.HardBreak) {
				sb.append('\n');
				lastWrap = firstRange.getStart() - 1;
			}

			sb.append(formatNodes(clause));
		}

		return sb.toString();
	}
}
