package arm32x.minecraft.commandblockide.client.processor;

import java.util.NavigableMap;
import java.util.OptionalInt;
import java.util.TreeMap;
import org.jetbrains.annotations.Nullable;

public final class StringMapping {
	private final NavigableMap<Integer, Integer> indexMap;

	public StringMapping(NavigableMap<Integer, Integer> indexMap) {
		this.indexMap = indexMap;
	}

	public OptionalInt mapIndex(int index) {
		@Nullable var entry = indexMap.floorEntry(index);
		if (entry != null) {
			int mapped = entry.getValue() - entry.getKey() + index;
			var nextEntry = indexMap.higherEntry(index);
			if (nextEntry != null) {
				int nextValue = nextEntry.getValue();
				if (nextValue <= mapped) {
					return OptionalInt.empty();
				}
			}
			return OptionalInt.of(mapped);
		} else {
			return OptionalInt.of(index);
		}
	}

	public NavigableMap<Integer, Integer> getIndexMap() {
		return indexMap;
	}

	public StringMapping inverted() {
		NavigableMap<Integer, Integer> map = new TreeMap<>();
		for (var entry : indexMap.entrySet()) {
			map.put(entry.getValue(), entry.getKey());
		}
		return new StringMapping(map);
	}
}
