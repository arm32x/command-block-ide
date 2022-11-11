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

	// A: 'execute as @a run say hello!'
	//     0       8     14
	//     │       └───┐ └───────┐
	// B: 'execute␊    as @a␊    run say hello!'
	//     0       ░░░░12    ░░░░22
	public OptionalInt mapIndex(int index) {
		@Nullable var entry = indexMap.floorEntry(index);
		int mapped;
		if (entry != null) {
			mapped = entry.getValue() - entry.getKey() + index;
		} else {
			mapped = index;
		}
		var nextEntry = indexMap.higherEntry(index);
		if (nextEntry != null) {
			if (mapped >= nextEntry.getValue() && index < nextEntry.getKey()) {
				return OptionalInt.empty();
			}
		}
		return OptionalInt.of(mapped);
	}

	public int mapIndexOrAfter(int index) {
		OptionalInt mapped;
		do {
			mapped = mapIndex(index++);
		} while (mapped.isEmpty());
		return mapped.getAsInt();
	}

	public static int mapIndexOrAfter(@Nullable StringMapping mapping, boolean inverted, int index) {
		if (mapping != null) {
			if (inverted) {
				return mapping.inverted().mapIndexOrAfter(index);
			} else {
				return mapping.mapIndexOrAfter(index);
			}
		} else {
			return index;
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
