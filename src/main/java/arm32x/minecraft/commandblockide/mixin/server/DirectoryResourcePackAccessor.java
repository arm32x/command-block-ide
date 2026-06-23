package arm32x.minecraft.commandblockide.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;
import net.minecraft.server.packs.PathPackResources;

@Mixin(PathPackResources.class)
public interface DirectoryResourcePackAccessor {
    @Accessor("root")
    Path getRoot();
}
