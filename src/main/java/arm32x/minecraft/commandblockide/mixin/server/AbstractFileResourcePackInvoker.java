package arm32x.minecraft.commandblockide.mixin.server;

import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractFileResourcePack.class)
public interface AbstractFileResourcePackInvoker {
	@Invoker("getFilename")
	static String invokeGetFilename(ResourceType type, Identifier id) {
		throw new AssertionError();
	}
}
