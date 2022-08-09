package arm32x.minecraft.commandblockide.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(EditBox.class)
public interface EditBoxAccessor {
    @Accessor("selectionEnd") void setSelectionEnd(int selectionEnd);
}
