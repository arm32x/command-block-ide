package arm32x.minecraft.commandblockide.mixin.server;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.ServerFunctionLibrary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerFunctionLibrary.class)
public interface FunctionLoaderAccessor {
    // Although it is possible to get by with String.format and avoid this
    // accessor, this should make the code more resilient to changes in
    // Minecraft (or at least alert me if they happen).
    @Accessor("LISTER")
    static FileToIdConverter getResourceFinder() {
        throw new AssertionError("Mixin should replace this");
    }
}
