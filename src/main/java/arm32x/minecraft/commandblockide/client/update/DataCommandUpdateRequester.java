package arm32x.minecraft.commandblockide.client.update;

import static arm32x.minecraft.commandblockide.client.CommandChainTracer.isCommandBlock;
import arm32x.minecraft.commandblockide.client.gui.screen.CommandBlockIDEScreen;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataCommandUpdateRequester {
	private static @Nullable DataCommandUpdateRequester INSTANCE = null;

	private final Map<BlockPos, CommandBlockEntity> blocksToUpdate = new HashMap<>();

	private DataCommandUpdateRequester() { }

	public static DataCommandUpdateRequester getInstance() {
		if (INSTANCE == null) {
			return INSTANCE = new DataCommandUpdateRequester();
		} else {
			return INSTANCE;
		}
	}

	public void requestUpdate(LocalPlayer player, CommandBlockEntity blockEntity) {
		BlockPos position = blockEntity.getBlockPos();
		blocksToUpdate.put(position, blockEntity);

		String command = String.format("data get block %d %d %d", position.getX(), position.getY(), position.getZ());
		player.connection.sendCommand(command);
	}

	public boolean handleFeedback(Minecraft client, TranslatableContents message) {
		Object[] args = message.getArgs();
		LOGGER.trace("Handling feedback for message {} with args {}.", message, args);

		@Nullable BlockPos position;
		try {
			position = new BlockPos((int)args[0], (int)args[1], (int)args[2]);
		} catch (ClassCastException ex1) {
			try {
				position = new BlockPos(Integer.parseInt(getStringFromText(args[0])), Integer.parseInt(getStringFromText(args[1])), Integer.parseInt(getStringFromText(args[2])));
			} catch (ClassCastException | NumberFormatException ex2) {
				LOGGER.error("Could not get block position from command feedback.");
				return false;
			}
		}
		if (!blocksToUpdate.containsKey(position)) {
			LOGGER.debug("Block {} not queued for update.", position);
			return false;
		}

		if (client.level == null) {
			LOGGER.warn("Client is outside of a world.");
			return false;
		}
		BlockState blockState = client.level.getBlockState(position);
		if (!isCommandBlock(blockState)) {
			LOGGER.debug("Block {} is not a command block.", position);
			return false;
		}

		String stringifiedTag = ((Component)args[3]).getString();
		@Nullable CompoundTag tag;
		try {
			tag = TagParser.parseCompoundFully(stringifiedTag);
		} catch (CommandSyntaxException ex) {
			LOGGER.error("Error parsing feedback from data command.", ex);
			return false;
		}

		@Nullable CommandBlockEntity blockEntity = blocksToUpdate.get(position);
		if (blockEntity == null) {
			LOGGER.debug("Block entity {} not queued for update.", position);
			return false;
		}

        try (var errorReporter = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
            blockEntity.loadWithComponents(TagValueInput.create(errorReporter, client.level.registryAccess(), tag));
        }
//		blockEntity.setNeedsUpdatePacket(false);
		if (client.screen instanceof CommandBlockIDEScreen) {
			((CommandBlockIDEScreen)client.screen).update(position);
		}
		blocksToUpdate.remove(position);

		return true;
	}

	private static String getStringFromText(Object object) {
		if (object instanceof Component text) {
			return text.getString();
		} else {
			return object.toString();
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DataCommandUpdateRequester.class);
}
