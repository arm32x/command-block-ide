package arm32x.minecraft.commandblockide.util;

import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;

public final class OrderedTexts {
    public static FormattedCharSequence skip(int count, FormattedCharSequence text) {
        return visitor -> text.accept(new FormattedCharSink() {
            private int remaining = count;

            @Override
            public boolean accept(int index, Style style, int codePoint) {
                if (remaining-- > 0) {
                    return true;
                } else {
                    return visitor.accept(index, style, codePoint);
                }
            }
        });
    }

    public static FormattedCharSequence limit(int count, FormattedCharSequence text) {
        return visitor -> text.accept(new FormattedCharSink() {
            private int remaining = count;

            @Override
            public boolean accept(int index, Style style, int codePoint) {
                if (remaining-- > 0) {
                    return visitor.accept(index, style, codePoint);
                } else {
                    return false;
                }
            }
        });
    }

    private OrderedTexts() {
    }
}
