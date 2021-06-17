package arm32x.minecraft.commandblockide.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import static net.minecraft.command.argument.FunctionArgumentType.function;
import static net.minecraft.server.command.CommandManager.argument;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public final class EditFunctionCommand {
	/**
	 * Like {@link FunctionCommand#SUGGESTION_PROVIDER}, but only returns
	 * functions and not tags.
	 */
	public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (ctx, builder) -> {
		CommandFunctionManager functionManager = ctx.getSource().getMinecraftServer().getCommandFunctionManager();
		return CommandSource.suggestIdentifiers(functionManager.method_29463(), builder);
	};

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("editfunction")
			.requires(source -> source.hasPermissionLevel(2))
			.then(argument("name", function())
				.suggests(SUGGESTION_PROVIDER)
				.executes(ctx -> {
					ctx.getSource().sendFeedback(new LiteralText("Not yet implemented.").formatted(Formatting.RED), false);
					return 0;
				})
			)
		);
	}
}
