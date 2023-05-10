package arm32x.minecraft.commandblockide.mixin.server;

import net.minecraft.resource.ResourceFinder;
import net.minecraft.server.function.FunctionLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FunctionLoader.class)
public interface FunctionLoaderAccessor {
    // Although it is possible to get by with String.format and avoid this
    // accessor, this should make the code more resilient to changes in
    // Minecraft (or at least alert me if they happen).
    @Accessor("FINDER")
    static ResourceFinder getResourceFinder() {
        throw new AssertionError("Mixin should replace this");
    }
}
