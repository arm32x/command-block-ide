package arm32x.minecraft.commandblockide.util;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

public final class OrderedTexts {
    public static OrderedText skip(int count, OrderedText text) {
        return visitor -> text.accept(new CharacterVisitor() {
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

    public static OrderedText limit(int count, OrderedText text) {
        return visitor -> text.accept(new CharacterVisitor() {
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
