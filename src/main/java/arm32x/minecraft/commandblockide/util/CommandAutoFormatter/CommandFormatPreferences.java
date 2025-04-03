package arm32x.minecraft.commandblockide.util.CommandAutoFormatter;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.HashMap;
import java.util.Map;

public class CommandFormatPreferences {
	private static CommandFormatPreferences instance;
	private static final String[] defaultHardBreakKeywords = {"run", "store"};
	private static final String[] defaultShortSoftBreakKeywords = {"as", "on", "if", "unless"};

	public int shortWrapLen = 20;
	public int longWrapLen = 100;
	public int shortNbtElementLen = 64; //must be nonnegative
	public int nbtLineWrapLen = longWrapLen;
	public String indentationStage = "  ";        //when indenting nested items, use this string

	public boolean shortWrapPreemptively = false; //wrap if a line isn't past the threshold, but would be if not wrapped
	public boolean wrapAfterBigNbtElements = true;
	public boolean formatTargetSelectors = true;    //formats selectors like @e[tag=testTag,nbt={data:{someValue:1}}]
	public boolean formatNbt = true;                 //formats NBT
	public boolean formatItemComponents = true;      //formats item components like stick[minecraft:custom_data={someValue:{someQuantity:1}}]
	public boolean preventLoneOpenBrace = true;      //prevents lone opening braces from having their own line

	private Map<String, LineBreakType> executeKeywordMap;

	public static CommandFormatPreferences getPreferences() {
		if (instance == null) {
			instance = new CommandFormatPreferences();
		}
		return instance;
	}

	private CommandFormatPreferences() {
		executeKeywordMap = defaultKeywordMap();
	}

	private static HashMap<String, LineBreakType> defaultKeywordMap() {
		HashMap<String, LineBreakType> ret = new HashMap<>();
		for (String keyword : defaultShortSoftBreakKeywords) {
			ret.put(keyword, LineBreakType.SoftBreakShort);
		}
		for (String keyword : defaultHardBreakKeywords) {
			ret.put(keyword, LineBreakType.HardBreak);
		}
		return ret;
	}


	public LineBreakType getBreakType(CommandNode<?> node, boolean firstInClause) {
		if (firstInClause && node instanceof LiteralCommandNode<?> literalNode) {
			return executeKeywordMap.getOrDefault(literalNode.getLiteral(), LineBreakType.SoftBreakLong);
		}
		return LineBreakType.NoBreak;
	}
}
