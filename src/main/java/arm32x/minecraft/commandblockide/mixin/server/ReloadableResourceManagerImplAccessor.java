package arm32x.minecraft.commandblockide.mixin.server;

import java.util.Map;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ReloadableResourceManagerImpl.class)
public interface ReloadableResourceManagerImplAccessor {
	@Accessor Map<String, NamespaceResourceManager> getNamespaceManagers();
}
