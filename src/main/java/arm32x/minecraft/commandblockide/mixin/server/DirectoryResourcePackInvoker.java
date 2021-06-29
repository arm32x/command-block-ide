package arm32x.minecraft.commandblockide.mixin.server;

import java.io.File;
import net.minecraft.resource.DirectoryResourcePack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DirectoryResourcePack.class)
public interface DirectoryResourcePackInvoker {
	// Note to self: Naming an invoker the same as the method it invokes causes
	// infinite recursion.
	@Invoker("getFile")
	@Nullable File invokeGetFile(String name);
}
