package arm32x.minecraft.commandblockide.mixin.server;

import arm32x.minecraft.commandblockide.mixinextensions.server.CommandFunctionExtension;
import com.mojang.brigadier.CommandDispatcher;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
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

	@Inject(method = "create(Lnet/minecraft/util/Identifier;Lcom/mojang/brigadier/CommandDispatcher;Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/List;)Lnet/minecraft/server/function/CommandFunction;", at = @At("RETURN"))
	private static void create(Identifier id, CommandDispatcher<ServerCommandSource> commandDispatcher, ServerCommandSource serverCommandSource, List<String> list, CallbackInfoReturnable<CommandFunction> cir) {
		CommandFunction function = cir.getReturnValue();
		((CommandFunctionExtension)function).ide$setOriginalLines(list);
	}
}
