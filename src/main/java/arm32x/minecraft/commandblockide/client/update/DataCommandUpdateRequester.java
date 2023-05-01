package arm32x.minecraft.commandblockide.client.update;

import static arm32x.minecraft.commandblockide.client.CommandChainTracer.isCommandBlock;
import arm32x.minecraft.commandblockide.client.gui.screen.CommandBlockIDEScreen;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class DataCommandUpdateRequester {
	private static @Nullable DataCommandUpdateRequester INSTANCE = null;

	private final Map<BlockPos, CommandBlockBlockEntity> blocksToUpdate = new HashMap<>();

	private DataCommandUpdateRequester() { }

	public static DataCommandUpdateRequester getInstance() {
		if (INSTANCE == null) {
			return INSTANCE = new DataCommandUpdateRequester();
		} else {
			return INSTANCE;
		}
	}

	public void requestUpdate(ClientPlayerEntity player, CommandBlockBlockEntity blockEntity) {
		BlockPos position = blockEntity.getPos();
		blocksToUpdate.put(position, blockEntity);

		String command = String.format("data get block %d %d %d", position.getX(), position.getY(), position.getZ());
		player.networkHandler.sendCommand(command);
	}

	public boolean handleFeedback(MinecraftClient client, TranslatableTextContent message) {
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

		if (client.world == null) {
			LOGGER.warn("Client is outside of a world.");
			return false;
		}
		BlockState blockState = client.world.getBlockState(position);
		if (!isCommandBlock(blockState)) {
			LOGGER.debug("Block {} is not a command block.", position);
			return false;
		}

		String stringifiedTag = ((Text)args[3]).getString();
		@Nullable NbtCompound tag;
		try {
			tag = StringNbtReader.parse(stringifiedTag);
		} catch (CommandSyntaxException ex) {
			LOGGER.error("Error parsing feedback from data command.", ex);
			return false;
		}

		@Nullable CommandBlockBlockEntity blockEntity = blocksToUpdate.get(position);
		if (blockEntity == null) {
			LOGGER.debug("Block entity {} not queued for update.", position);
			return false;
		}

		blockEntity.readNbt(tag);
//		blockEntity.setNeedsUpdatePacket(false);
		if (client.currentScreen instanceof CommandBlockIDEScreen) {
			((CommandBlockIDEScreen)client.currentScreen).update(position);
		}
		blocksToUpdate.remove(position);

		return true;
	}

	private static String getStringFromText(Object object) {
		if (object instanceof Text text) {
			return text.getString();
		} else {
			return object.toString();
		}
	}

	private static final Logger LOGGER = LogManager.getLogger();
}
