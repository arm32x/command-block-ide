package arm32x.minecraft.commandblockide.client;

import java.util.*;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class CommandChainTracer {
	private final ClientLevel world;

	public CommandChainTracer(ClientLevel world) {
		this.world = world;
	}

	/**
	 * Traces a command block chain forwards until the end.
	 * @return An {@link Iterable} returning the positions of the blocks in the chain.
	 * @see CommandChainTracer#traceBackwards
	 */
	public Iterable<BlockPos> traceForwards(BlockPos startPosition) {
		return () -> new Forwards(startPosition);
	}

	/**
	 * Traces a command block chain backwards until the start. Stops early if
	 * multiple command blocks point to the same block.
	 * @return An {@link Iterable} returning the positions of the blocks in the chain.
	 * @see CommandChainTracer#traceForwards
	 */
	public Iterable<BlockPos> traceBackwards(BlockPos startPosition) {
		return () -> new Backwards(startPosition);
	}

	private final class Forwards implements Iterator<BlockPos> {
		private BlockPos position;
		private final Set<BlockPos> visited = new HashSet<>();

		private Forwards(BlockPos startPosition) {
			position = startPosition;
			visited.add(startPosition);
		}

		@Override
		public boolean hasNext() {
			BlockState blockState = world.getBlockState(position);
			if (isCommandBlock(blockState)) {
				Direction facing = blockState.getValue(CommandBlock.FACING);
				BlockPos nextPosition = position.relative(facing);
				BlockState nextBlockState = world.getBlockState(nextPosition);
				return Stream.of(
					Blocks.COMMAND_BLOCK,
					Blocks.REPEATING_COMMAND_BLOCK,
					Blocks.CHAIN_COMMAND_BLOCK
				).anyMatch(nextBlockState::is) && !visited.contains(nextPosition);
			}
			return false;
		}

		@Override
		public BlockPos next() {
			BlockState blockState = world.getBlockState(position);
			if (isCommandBlock(blockState)) {
				Direction facing = blockState.getValue(CommandBlock.FACING);
				BlockPos nextPosition = position.relative(facing);
				BlockState nextBlockState = world.getBlockState(nextPosition);
				if (Stream.of(
						Blocks.COMMAND_BLOCK,
						Blocks.REPEATING_COMMAND_BLOCK,
						Blocks.CHAIN_COMMAND_BLOCK
					).anyMatch(nextBlockState::is) && !visited.contains(nextPosition)) {
					position = nextPosition;
					visited.add(position);
					return position;
				}
			}
			throw new NoSuchElementException();
		}
	}

	private final class Backwards implements Iterator<BlockPos> {
		private BlockPos position;
		private final Set<BlockPos> visited = new HashSet<>();

		private Backwards(BlockPos startPosition) {
			position = startPosition;
			visited.add(startPosition);
		}

		@Override
		public boolean hasNext() {
			BlockState blockState = world.getBlockState(position);
			if (Stream.of(
					Blocks.COMMAND_BLOCK,
					Blocks.REPEATING_COMMAND_BLOCK,
					Blocks.CHAIN_COMMAND_BLOCK
				).anyMatch(blockState::is)) {
				long resultCount = getStream(blockState).count();
				return resultCount == 1;
			}
			return false;
		}

		@Override
		public BlockPos next() {
			BlockState blockState = world.getBlockState(position);
			if (Stream.of(
					Blocks.COMMAND_BLOCK,
					Blocks.REPEATING_COMMAND_BLOCK,
					Blocks.CHAIN_COMMAND_BLOCK
				).anyMatch(blockState::is)) {
				List<BlockPos> results = getStream(blockState).toList();
				if (results.size() != 1) {
					throw new NoSuchElementException();
				}
				position = results.getFirst();
				visited.add(position);
				return position;
			}
			throw new NoSuchElementException();
		}

		private Stream<BlockPos> getStream(BlockState blockState) {
			return Stream.of(Direction.values())
				.filter((direction) -> direction != blockState.getValue(CommandBlock.FACING))
				.map((direction) -> position.relative(direction))
				.filter((pos) -> isCommandBlock(world.getBlockState(pos)) && !visited.contains(pos))
				.filter((pos) -> pos.relative(world.getBlockState(pos).getValue(CommandBlock.FACING)).equals(position));
		}
	}

	// TODO: Move to a proper utility class.
	public static boolean isCommandBlock(BlockState blockState) {
		return blockState.is(Blocks.COMMAND_BLOCK) || blockState.is(Blocks.REPEATING_COMMAND_BLOCK) || blockState.is(Blocks.CHAIN_COMMAND_BLOCK);
	}
}
