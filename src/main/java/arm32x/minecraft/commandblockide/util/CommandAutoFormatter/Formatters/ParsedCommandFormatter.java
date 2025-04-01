package arm32x.minecraft.commandblockide.util.CommandAutoFormatter.Formatters;

import arm32x.minecraft.commandblockide.util.CommandAutoFormatter.CommandFormatPreferences;
import arm32x.minecraft.commandblockide.util.CommandAutoFormatter.CommandParser;
import arm32x.minecraft.commandblockide.util.CommandAutoFormatter.ParsedCommand;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;
import java.util.List;

public abstract class ParsedCommandFormatter extends TextFormatter {
	private final ParsedCommand parse;
	public ParsedCommandFormatter(String txt, CommandFormatPreferences preferences) {
		super(txt, preferences);
		parse = CommandParser.parse(txt);
	}

	@Override
	public String format(String txt, CommandFormatPreferences preferences) {
		var parse = CommandParser.parse(txt);
		if(parse!=null){
			String result = formatParsed();
			return (result.trim() +'\n'+ parse.leftovers).trim();
		}else{
			return txt;
		}
	}

	public abstract String formatParsed();


	public static List<List<ParsedCommandNode<?>>> getAllNodeLists(ParseResults<CommandSource> parse) {
		List<List<ParsedCommandNode<?>>> childList = new ArrayList<>();
		var context = parse.getContext();

		while (context != null) {
			List<ParsedCommandNode<?>> nodeList = new ArrayList<>(context.getNodes());
			context = context.getChild();
			if (!nodeList.isEmpty()) {
				childList.add(nodeList);
			}
		}

		return childList;
	}

	public static List<ParsedCommandNode<?>> getAllNodes(ParseResults<CommandSource> parse){
		List<ParsedCommandNode<?>> nodeList = new ArrayList<>();
		for(var list: getAllNodeLists(parse)){
			nodeList.addAll(list);
		}
		return nodeList;
	}

	public List<List<ParsedCommandNode<?>>> getAllNodeLists() {
		return getAllNodeLists(parse.results);
	}

	public List<ParsedCommandNode<?>> getAllNodes(){
		return getAllNodes(parse.results);
	}




	public boolean isBracesTypeNode(CommandNode<?> node) {
		if (node instanceof ArgumentCommandNode<?, ?> argNode) {
			switch (argNode.getName()){
				case "nbt" -> {
					return preferences.formatNbt;
				}
				case "targets" -> {
					return preferences.formatTargetSelectors;
				}
				case "item" ->{
					return preferences.formatItemComponents;
				}
			}
		}
		return false;
	}

	public String formatNodes(List<ParsedCommandNode<?>> clause) {
		StringBuilder sb = new StringBuilder();
		for (ParsedCommandNode<?> node : clause) {
			StringRange range = node.getRange();
			String nodeStr = range.get(txt);

			if (isBracesTypeNode(node.getNode()) && nodeStr.length() > preferences.shortNbtElementLen) {
				BracesFormatter b = new BracesFormatter(nodeStr, preferences);
				String formattedNode = b.format();
				sb.append('\n');
				sb.append(formattedNode);
				lastWrap = b.lastWrap+range.getStart();
			} else {
				//most nodes
				if (range.getStart() > (1 + lastWrap)) {
					sb.append(' ');
				}

				sb.append(nodeStr);
			}
		}

		return sb.toString();
	}
}
