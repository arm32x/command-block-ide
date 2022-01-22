package arm32x.minecraft.commandblockide.client.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class StatusTexts {
	public static OrderedText commandBlock(MinecraftClient client, BlockPos pos) {
		return new TranslatableText(
			"commandBlockIDE.statusText.commandBlock",
			pos.getX(), pos.getY(), pos.getZ(),
			getWorldOrServerName(client)
		).setStyle(Style.EMPTY.withColor(Formatting.GRAY)).asOrderedText();
	}

	private static String getWorldOrServerName(MinecraftClient client) {
		@Nullable ServerInfo info = client.getCurrentServerEntry();
		if (info != null) {
			return info.name;
		}
		@Nullable IntegratedServer server = client.getServer();
		if (server != null) {
			return server.getSaveProperties().getLevelName();
		}
		return "Unknown";
	}
}
