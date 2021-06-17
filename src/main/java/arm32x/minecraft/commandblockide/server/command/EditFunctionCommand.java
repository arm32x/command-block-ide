package arm32x.minecraft.commandblockide.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import static net.minecraft.command.argument.FunctionArgumentType.function;
import static net.minecraft.server.command.CommandManager.argument;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public final class EditFunctionCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("editfunction")
			.requires(source -> source.hasPermissionLevel(2))
			.then(argument("name", function())
				.suggests(FunctionCommand.SUGGESTION_PROVIDER)
				.executes(ctx -> {
					ctx.getSource().sendFeedback(new LiteralText("Not yet implemented.").formatted(Formatting.RED), false);
					return 0;
				})
			)
		);
	}
}
