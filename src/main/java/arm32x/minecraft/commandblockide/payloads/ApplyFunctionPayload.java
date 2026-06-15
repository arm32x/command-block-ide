package arm32x.minecraft.commandblockide.payloads;

import arm32x.minecraft.commandblockide.Packets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ApplyFunctionPayload(ByteBuf bytes) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ApplyFunctionPayload> CODEC = StreamCodec.ofMember(ApplyFunctionPayload::send, ApplyFunctionPayload::new);

    public ApplyFunctionPayload(FriendlyByteBuf buf) {
        this(buf.readBytes(buf.readableBytes()));
    }

    private void send(FriendlyByteBuf buf) {
        buf.writeBytes(bytes, bytes.readableBytes());
    }

    public FriendlyByteBuf toBuf() {
        return new FriendlyByteBuf(bytes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Packets.APPLY_FUNCTION;
    }
}
