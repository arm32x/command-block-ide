package arm32x.minecraft.commandblockide.client.update;

import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public final class InteractUpdateRequester implements UpdateRequester {
	private static @Nullable InteractUpdateRequester INSTANCE = null;

	private InteractUpdateRequester() { }

	public static InteractUpdateRequester getInstance() {
		if (INSTANCE == null) {
			return INSTANCE = new InteractUpdateRequester();
		} else {
			return INSTANCE;
		}
	}

	@Override
	public void requestUpdate(ClientPlayNetworkHandler networkHandler, CommandBlockBlockEntity blockEntity) {
		BlockHitResult blockHitResult = new BlockHitResult(blockEntity.getCommandExecutor().getPos(), Direction.UP, blockEntity.getPos(), false);
		PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult);
		networkHandler.sendPacket(packet);
	}
}
