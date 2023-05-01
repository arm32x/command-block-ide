package arm32x.minecraft.commandblockide.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Splits a {@link PacketByteBuf} into several chunks to avoid size limitations.
 */
public final class PacketSplitter implements Iterable<PacketByteBuf> {
	public static final int CHUNK_SIZE = 32500;
	public static final int HEADER_MAGIC = 1397771337 /* SPLI */;

	private final PacketByteBuf source;

	public PacketSplitter(PacketByteBuf source) {
		this.source = source;
	}

	/**
	 * Writes a {@code PacketSplitter} header to the provided {@link
	 * PacketByteBuf}. This should be written to the {@code PacketByteBuf}
	 * first, before any other data.
	 * @param buf The {@code PacketByteBuf} to write the header to.
	 */
	public static void writeHeader(PacketByteBuf buf) {
		buf.writeInt(HEADER_MAGIC);
		buf.writeInt(0);
	}

	/**
	 * Updates the chunk count in the {@code PacketSplitter} header of the
	 * provided {@link PacketByteBuf}.
	 * @param buf The {@code PacketByteBuf} to update the chunk count of. It
	 *            must contain a header written by {@link
	 *            PacketSplitter#writeHeader(PacketByteBuf)}.
	 * @throws MissingHeaderException if {@code buf} does not contain a {@code
	 *         PacketSplitter} header.
	 */
	public static void updateChunkCount(PacketByteBuf buf) {
		int start = buf.readerIndex();
		if (buf.readInt() != HEADER_MAGIC) {
			throw new MissingHeaderException();
		}
		int chunkCount = (int)Math.ceil(buf.readableBytes() / (double)CHUNK_SIZE);
		buf.setInt(start + 4, chunkCount);
		buf.readerIndex(start);
	}

	@Override
	public @NotNull Iterator<PacketByteBuf> iterator() {
		if (source.getInt(source.readerIndex()) != HEADER_MAGIC) {
			throw new MissingHeaderException();
		}
		return new BufferIterator();
	}

	private class BufferIterator implements Iterator<PacketByteBuf> {
		@Override
		public boolean hasNext() {
			return source.readableBytes() > 0;
		}

		@Override
		public PacketByteBuf next() {
			int length = Math.min(CHUNK_SIZE, source.readableBytes());
			if (length <= 0) {
				throw new NoSuchElementException();
			}
			return new PacketByteBuf(source.readSlice(length));
		}
	}

	public static class MissingHeaderException extends RuntimeException {
		@Override
		public String getMessage() {
			return "Missing PacketSplitter header.";
		}
	}
}
