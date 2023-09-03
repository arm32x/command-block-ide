package arm32x.minecraft.commandblockide.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.minecraft.text.OrderedText;

public final class OrderedTexts {
	public static OrderedText skip(int count, OrderedText text) {
		return visitor -> {
			AtomicInteger counter = new AtomicInteger(count);
			return text.accept((index, style, codePoint) -> {
				if (counter.getAndDecrement() > 0) {
					return true;
				} else {
					return visitor.accept(index, style, codePoint);
				}
			});
		};
	}

	public static OrderedText limit(int count, OrderedText text) {
		return visitor -> {
			AtomicInteger counter = new AtomicInteger(count);
			return text.accept((index, style, codePoint) -> {
				if (counter.getAndDecrement() > 0) {
					return visitor.accept(index, style, codePoint);
				} else {
					return false;
				}
			});
		};
	}

	private OrderedTexts() { }
}
