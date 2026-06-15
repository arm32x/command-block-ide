package arm32x.minecraft.commandblockide.payloads;

import arm32x.minecraft.commandblockide.Packets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EditFunctionPayload(Identifier id, int lineCount) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, EditFunctionPayload> CODEC = StreamCodec.ofMember(EditFunctionPayload::send, EditFunctionPayload::new);

    public EditFunctionPayload(FriendlyByteBuf buf) {
        this(buf.readIdentifier(), buf.readVarInt());
    }

    private void send(FriendlyByteBuf buf) {
        buf.writeIdentifier(id);
        buf.writeVarInt(lineCount);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Packets.EDIT_FUNCTION;
    }
}
