package arm32x.minecraft.commandblockide.server.command;

import arm32x.minecraft.commandblockide.Packets;
import arm32x.minecraft.commandblockide.payloads.EditFunctionPayload;
import arm32x.minecraft.commandblockide.payloads.UpdateFunctionCommandPayload;
import arm32x.minecraft.commandblockide.server.function.FunctionIO;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import static net.minecraft.commands.arguments.item.FunctionArgument.functions;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class EditFunctionCommand {
	/**
	 * Like {@link FunctionCommand#SUGGEST_FUNCTION}, but only returns
	 * functions and not tags.
	 */
	public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (ctx, builder) -> {
		ServerFunctionManager functionManager = ctx.getSource().getServer().getFunctions();
		return SharedSuggestionProvider.suggestResource(functionManager.getFunctionNames(), builder);
	};

	private static final SimpleCommandExceptionType EDIT_TAG_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("arguments.editfunction.tag.unsupported"));
	private static final SimpleCommandExceptionType MOD_NOT_INSTALLED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.editfunction.failed.modNotInstalled"));
	// loadFunction returns a Text on failure, so we use that.
	private static final DynamicCommandExceptionType FUNCTION_LOAD_FAILED_EXCEPTION = new DynamicCommandExceptionType(text -> (Message)text);

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("editfunction")
				.requires(source ->
						source.permissions().hasPermission(
								new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)
						)
				)
			.then(argument("name", functions())
				.suggests(SUGGESTION_PROVIDER)
				.executes(ctx -> {
					Optional<CommandFunction<CommandSourceStack>> function = FunctionArgument.getFunctionOrTag(ctx, "name").getSecond().left();
					if (function.isPresent()) {
						return execute(ctx.getSource(), function.get());
					} else {
						throw EDIT_TAG_EXCEPTION.create();
					}
				})
			)
		);
	}

	private static int execute(CommandSourceStack source, CommandFunction<CommandSourceStack> function) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayer();
		if (player == null || !ServerPlayNetworking.canSend(player, Packets.EDIT_FUNCTION)) {
			throw MOD_NOT_INSTALLED_EXCEPTION.create();
		}

        Either<List<String>, Component> loadResult = FunctionIO.loadFunction(source.getServer(), function.id());
		if (loadResult.right().isPresent()) {
			throw FUNCTION_LOAD_FAILED_EXCEPTION.create(loadResult.right().get());
		}
		var lines = loadResult.left().orElseThrow(); // Should be present

		ServerPlayNetworking.send(player, new EditFunctionPayload(function.id(), lines.size()));

		for (int index = 0; index < lines.size(); index++) {
			ServerPlayNetworking.send(player, new UpdateFunctionCommandPayload(index, lines.get(index)));
		}

		return 1;
	}
}
