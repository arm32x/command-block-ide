package arm32x.minecraft.commandblockide.util.CommandAutoFormatter;

public enum LineBreakType {
	NoBreak, //doesn't wrap line
	HardBreak, //always wraps line
	SoftBreakShort, //wraps if the current line length is above threshold
	SoftBreakLong,  //wraps if the current line length is above a longer threshold
}
