package arm32x.minecraft.commandblockide.mixin.server;

import arm32x.minecraft.commandblockide.mixinextensions.server.CommandFunctionExtension;
import com.mojang.brigadier.CommandDispatcher;
import java.util.List;

import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandFunction.class)
public final class CommandFunctionMixin implements CommandFunctionExtension {
	@Unique
	private List<String> ide$originalLines;

	@Override
	public List<String> ide$getOriginalLines() {
		return ide$originalLines;
	}

	@Override
	public void ide$setOriginalLines(List<String> lines) {
		ide$originalLines = lines;
	}

	// TODO - This function still produces a MixinException. Anyone need to look into this
	@Inject(
		method = "create",
		at = @At(
			value = "RETURN",
			remap = false
		)
	)
	private static <T extends AbstractServerCommandSource<T>> void create(Identifier id, CommandDispatcher<T> dispatcher, T source, List<String> lines, CallbackInfoReturnable<CommandFunction<T>> cir) {
		CommandFunction<T> function = cir.getReturnValue();
		((CommandFunctionExtension)function).ide$setOriginalLines(lines);
	}
}
