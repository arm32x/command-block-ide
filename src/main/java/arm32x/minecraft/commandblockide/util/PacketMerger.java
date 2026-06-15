package arm32x.minecraft.commandblockide.util;

import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Merges the {@link FriendlyByteBuf}s created by a {@link PacketSplitter} back
 * into one {@code PacketByteBuf}.
 */
public final class PacketMerger {
	private FriendlyByteBuf destination = FriendlyByteBufs.create();
	private int chunksRemaining = -1;

	public PacketMerger() { }

	/**
	 * Appends the provided {@link FriendlyByteBuf} to the result and returns the
	 * result if it is complete. The {@code PacketMerger} will return to its
	 * original state once the merged packet is complete.
	 * @param buf The {@code PacketByteBuf} to append.
	 * @return The merged {@code PacketByteBuf} if the appended packet was the
	 *         last, otherwise an empty {@link Optional}.
	 * @throws InvalidSplitPacketException if the header of the first packet is
	 * 		   invalid, or if the first packet is missing a {@link
	 * 		   PacketSplitter} header.
	 * @throws IllegalStateException if all chunks have already been merged and
	 * 	       the {@code PacketByteBuf} has been returned.
	 */
	public Optional<FriendlyByteBuf> append(FriendlyByteBuf buf) throws InvalidSplitPacketException {
		if (chunksRemaining == -1) {
			int header = buf.readInt();
			if (header != PacketSplitter.HEADER_MAGIC) {
				throw new InvalidSplitPacketException("Missing PacketSplitter header.");
			}
			int declaredChunks = buf.readInt();
			if (declaredChunks <= 0) {
				throw new InvalidSplitPacketException("Invalid chunk count " + chunksRemaining + ".");
			}
			chunksRemaining = declaredChunks;
		}
		destination.writeBytes(buf);
		if (--chunksRemaining == 0) {
			FriendlyByteBuf merged = destination;
			destination = FriendlyByteBufs.create();
			chunksRemaining = -1;
			return Optional.of(merged);
		} else {
			return Optional.empty();
		}
	}

	public static class InvalidSplitPacketException extends Exception {
		public InvalidSplitPacketException(String message) {
			super(message);
		}
	}
}
