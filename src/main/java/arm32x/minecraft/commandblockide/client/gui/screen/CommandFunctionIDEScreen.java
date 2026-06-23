package arm32x.minecraft.commandblockide.client.gui.screen;

import arm32x.minecraft.commandblockide.client.gui.editor.CommandEditor;
import arm32x.minecraft.commandblockide.client.gui.editor.CommandFunctionEditor;
import arm32x.minecraft.commandblockide.payloads.ApplyFunctionPayload;
import arm32x.minecraft.commandblockide.util.PacketSplitter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class CommandFunctionIDEScreen extends CommandIDEScreen<CommandFunctionEditor> {
	private final Identifier functionId;
	private final int startingLineCount;

	public CommandFunctionIDEScreen(Identifier functionId, int lineCount) {
		this.functionId = functionId;
		this.startingLineCount = lineCount;
	}

	@Override
	protected void firstInit() {
		for (int index = 0; index < startingLineCount; index++) {
			CommandFunctionEditor editor = new CommandFunctionEditor(this, font, 8, 20 * index + 8, width - 16, 16, index);
			if (index == 0) {
				setFocusedEditor(editor);
			}
			addEditor(editor);
		}

		statusText = Component.literal(functionId.toString()).withStyle(ChatFormatting.GRAY);

		super.firstInit();
	}

	public void update(int index, String command) {
		var editor = editors.get(index);
		editor.update(functionId, command);
		setLoaded(true);
		if (getFocused() == editor) {
			setFocusedEditor(editor);
		}
	}

	@Override
	public void save() {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		PacketSplitter.writeHeader(buf);
		buf.writeIdentifier(functionId);
		buf.writeVarInt(editors.size());
		for (CommandEditor editor : editors) {
			buf.writeUtf(editor.getSingleLineCommand(), Integer.MAX_VALUE >> 2);
			if (editor instanceof CommandFunctionEditor functionEditor) {
				functionEditor.saveMultilineCommand(functionId);
			}
		}
		PacketSplitter.updateChunkCount(buf);

		PacketSplitter splitter = new PacketSplitter(buf);
		for (ByteBuf splitBuf : splitter) {
			ClientPlayNetworking.send(new ApplyFunctionPayload(splitBuf));
		}

		super.save();
	}
}
