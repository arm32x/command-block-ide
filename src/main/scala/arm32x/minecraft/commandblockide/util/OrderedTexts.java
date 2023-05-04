package arm32x.minecraft.commandblockide.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.minecraft.text.OrderedText;

public final class OrderedTexts {
	public static OrderedText until(Predicate<Integer> codePointPredicate, OrderedText text) {
		return visitor -> {
			AtomicBoolean reached = new AtomicBoolean(false);
			return text.accept((index, style, codePoint) -> {
				if (reached.get() || codePointPredicate.test(codePoint)) {
					reached.set(true);
					return false;
				} else {
					return visitor.accept(index, style, codePoint);
				}
			});
		};
	}

	public static OrderedText until(int codePoint, OrderedText text) {
		return until(point -> point == codePoint, text);
	}

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

	public static List<OrderedText> split(Predicate<Integer> codePointPredicate, OrderedText text) {
		AtomicInteger previous = new AtomicInteger(-1);
		AtomicInteger counter = new AtomicInteger(0);
		List<OrderedText> split = new ArrayList<>();
		text.accept((index, style, codePoint) -> {
			int count = counter.getAndIncrement();
			if (codePointPredicate.test(codePoint)) {
				int prev = previous.getAndSet(count);
				split.add(limit(count - prev - 1, skip(prev + 1, text)));
			}
			return true;
		});
		int prev = previous.get();
		if (prev < counter.get() - 1) {
			split.add(skip(prev + 1, text));
		}
		return split;
	}

	public static List<OrderedText> split(int codePoint, OrderedText text) {
		return split(point -> point == codePoint, text);
	}

	private OrderedTexts() { }
}
