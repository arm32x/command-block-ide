package arm32x.minecraft.commandblockide.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EditBox.class)
public interface EditBoxAccessor {
	@Accessor int getTextColor();
	@Accessor int getTextColorUneditable();
	@Accessor Font getFont();
	@Accessor long getFocusedTime();
	@Accessor boolean isCanLoseFocus();

	@Invoker int invokeGetMaxLength();
	@Invoker boolean invokeIsEditable();

	@Accessor("SPRITES") static WidgetSprites getTextures() { throw new AssertionError(); };
}
