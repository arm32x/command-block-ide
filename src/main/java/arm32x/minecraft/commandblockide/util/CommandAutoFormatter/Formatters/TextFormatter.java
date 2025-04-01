package arm32x.minecraft.commandblockide.util.CommandAutoFormatter.Formatters;

import arm32x.minecraft.commandblockide.util.CommandAutoFormatter.CommandFormatPreferences;

public abstract class TextFormatter {
	protected int lastWrap;
	protected CommandFormatPreferences preferences;
	protected String txt;

	public TextFormatter(String txt, CommandFormatPreferences preferences){
		this.txt=txt;
		this.preferences=preferences;
		lastWrap=-1;
	}

	public abstract String format(String txt, CommandFormatPreferences preferences);
	public String format(){
		return format(txt,preferences);
	}
}
