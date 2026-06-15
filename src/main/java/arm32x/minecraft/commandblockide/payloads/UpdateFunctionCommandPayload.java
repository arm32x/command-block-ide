package arm32x.minecraft.commandblockide.payloads;

import arm32x.minecraft.commandblockide.Packets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateFunctionCommandPayload(int index, String line) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, UpdateFunctionCommandPayload> CODEC = StreamCodec.ofMember(UpdateFunctionCommandPayload::send, UpdateFunctionCommandPayload::new);

    public UpdateFunctionCommandPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readUtf());
    }

    private void send(FriendlyByteBuf buf) {
        buf.writeVarInt(index);
        buf.writeUtf(line); // TODO: Make sure this doesn’t exceed size limits.
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Packets.UPDATE_FUNCTION_COMMAND;
    }
}
