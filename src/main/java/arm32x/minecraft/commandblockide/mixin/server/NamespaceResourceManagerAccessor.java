package arm32x.minecraft.commandblockide.mixin.server;

import java.util.List;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.ResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NamespaceResourceManager.class)
public interface NamespaceResourceManagerAccessor {
	@Accessor List<ResourcePack> getPackList();
}
