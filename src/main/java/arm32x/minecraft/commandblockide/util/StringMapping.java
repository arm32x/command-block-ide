package arm32x.minecraft.commandblockide.util;

import java.util.NavigableMap;
import java.util.TreeMap;
import org.jetbrains.annotations.Nullable;

public final class StringMapping {
	private final NavigableMap<Integer, Integer> indexMap;

	public StringMapping(NavigableMap<Integer, Integer> indexMap) {
		this.indexMap = indexMap;
	}

	public int mapIndex(int index) {
		@Nullable var entry = indexMap.floorEntry(index);
		if (entry != null) {
			return entry.getValue() - entry.getKey() + index;
		} else {
			return index;
		}
	}

	public StringMapping inverted() {
		NavigableMap<Integer, Integer> map = new TreeMap<>();
		for (var entry : indexMap.entrySet()) {
			map.put(entry.getValue(), entry.getKey());
		}
		return new StringMapping(map);
	}
}
