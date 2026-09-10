package arm32x.minecraft.commandblockide.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.MultilineTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(MultilineTextField.class)
public interface EditBoxAccessor {
    @Accessor("selectCursor")
    int getSelectionEnd();

    @Accessor("selectCursor")
    void setSelectionEnd(int selectionEnd);
}
