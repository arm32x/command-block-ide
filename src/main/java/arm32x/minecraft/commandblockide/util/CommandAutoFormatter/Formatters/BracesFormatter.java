package arm32x.minecraft.commandblockide.util.CommandAutoFormatter.Formatters;

import arm32x.minecraft.commandblockide.util.CommandAutoFormatter.CommandFormatPreferences;

import java.util.HashMap;

public class BracesFormatter extends TextFormatter {

	public BracesFormatter(String txt, CommandFormatPreferences preferences) {
		super(txt, preferences);
	}

	@Override
	public String format(String str, CommandFormatPreferences preferences) {
		HashMap<Integer, Integer> breaks = new HashMap<>();
		String s = limitedWhitespaceStrip(str);

		int ret = formatElement(s, preferences, breaks, 0, 0);

		if (ret >= 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s.length(); i++) {
				indentationLogic:
				if (breaks.containsKey(i)) {
					if ("{[".contains(s.substring(i, i + 1)) && i > 0) {
						if ((preferences.preventLoneOpenBrace) && breaks.containsKey(i + 1)) {
							break indentationLogic;
						}
					}
					sb.append('\n');
					sb.append(preferences.indentationStage.repeat(Math.max(0, breaks.get(i))));
				}
				sb.append(s.charAt(i));
			}
			return sb.toString();
		} else {
			return str;
		}
	}

	//returns length of element (excluding braces)
	//returns -1 if failed
	//	str: entire string being formatted
	//	preferences: format preferences
	//	output: map of index -> indentations to insert before that character
	//	index: to start formatting from
	//	depth: recursion depth AKA nesting depth
	private int formatElement(String str, CommandFormatPreferences preferences, HashMap<Integer, Integer> output, int index, int depth) {
		if (index >= str.length()) {
			return -1;
		}

		char strBoundaryChar = 0;
		char endChr = 0;
		switch (str.charAt(index)) {
			case '{' -> endChr = '}';
			case '[' -> endChr = ']';
		}
		if (endChr != 0) {
			depth++;
		}

		for (int i = index + 1; i < str.length(); i++) {
			char c = str.charAt(i);
			if (strBoundaryChar == 0) {
				switch (c) {
					case '\"':
					case '\'':
						strBoundaryChar = c;
						break;

					case '{':
					case '[':
						//recursive case
						int subLen = formatElement(str, preferences, output, i, depth);
						if (subLen < 0) {
							return -1;
						}

						if ((i + subLen - lastWrap) > preferences.nbtLineWrapLen) { //length wrap
							int lastComma = str.lastIndexOf(",", i);
							int wrapIndex = lastComma + 1;
							if (wrapIndex < index)
								wrapIndex = i;
							if (lastWrap < i && !output.containsKey(wrapIndex) && wrapIndex > index) {
								lastWrap = wrapIndex;
								output.put(lastWrap, depth);
							}
						}

						i += subLen + 1;
						break;

					case '}':
					case ']':
						if (c == endChr) {
							int thisLen = (i - 1) - index;
							boolean splitting = thisLen > preferences.shortNbtElementLen;
							if (splitting) {
								output.put(index + 1, depth);
								output.put(i, depth - 1);
							}
							if (preferences.wrapAfterBigNbtElements && thisLen > preferences.nbtLineWrapLen) {
								if (str.length() > i + 1)
									if (str.charAt(i + 1) == ',')
										output.put(i + 2, depth - 1);
							}
							return thisLen;
						} else {
							return -1;
						}
				}
			} else {
				//currently in string
				switch (c) {
					case '\\':
						i++;
						break;
					case '\"':
					case '\'':
						if (c == strBoundaryChar) {
							strBoundaryChar = 0;
						}
				}
			}
		}

		return str.length() - index;
	}

	private static String limitedWhitespaceStrip(String str) {
		str = str.trim() + " ";
		boolean charEscaped = false;
		StringBuilder sb = new StringBuilder();

		char strBoundaryChar = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			boolean cWhitespace = Character.isWhitespace(c);

			sb.append(c);

			if (!charEscaped) {
				if (strBoundaryChar == 0) {
					//not in string
					switch (c) {
						case '\"':
						case '\'':
							strBoundaryChar = c;
							break;
						default:
							if (cWhitespace) {
								sb.deleteCharAt(sb.length() - 1);
							}
					}
				} else {
					//in string
					switch (c) {
						case '\"':
						case '\'':
							if (c == strBoundaryChar) {
								strBoundaryChar = 0;
							}
							break;
						case '\\':
							charEscaped = true;
							continue;
					}
				}
			}

			charEscaped = false;
		}
		return sb.toString();
	}
}
